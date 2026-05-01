package com.freeswitchjava.esl.outbound;

import com.freeswitchjava.esl.command.SendMsg;
import com.freeswitchjava.esl.dialplan.DpApp;
import com.freeswitchjava.esl.dialplan.DpTools;
import com.freeswitchjava.esl.event.EventBus;
import com.freeswitchjava.esl.inbound.BgapiJobTracker;
import com.freeswitchjava.esl.inbound.PendingCommandQueue;
import com.freeswitchjava.esl.model.ApiResponse;
import com.freeswitchjava.esl.model.CommandReply;
import com.freeswitchjava.esl.model.EslEvent;
import com.freeswitchjava.esl.model.HangupCause;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Represents a single outbound ESL session — one active call leg from FreeSWITCH.
 *
 * <p>Provides the full dialplan application set as a typed async API.
 * All operations are non-blocking and return {@link CompletableFuture}.
 * Use {@code .join()} only on a non-I/O thread (the session factory runs on a virtual thread
 * so blocking is safe there).
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * OutboundServer.create(config, session -> {
 *     session.connect().join();
 *     session.answer().join();
 *     session.playback("/var/lib/freeswitch/sounds/en/us/callie/ivr/ivr-welcome_to_freeswitch.wav").join();
 *     session.say("en", "CURRENT_DATE_TIME", "").join();
 *     session.hangup(HangupCause.NORMAL_CLEARING).join();
 * });
 * }</pre>
 */
public final class OutboundSession {

    private static final Logger log = LoggerFactory.getLogger(OutboundSession.class);

    private final Channel channel;
    private final PendingCommandQueue pendingQueue;
    private final PendingCommandQueue apiQueue;
    private final BgapiJobTracker bgapiTracker;
    private final EventBus eventBus;

    private volatile EslEvent channelData;

    OutboundSession(Channel channel,
                    PendingCommandQueue pendingQueue,
                    PendingCommandQueue apiQueue,
                    BgapiJobTracker bgapiTracker,
                    EventBus eventBus) {
        this.channel      = channel;
        this.pendingQueue = pendingQueue;
        this.apiQueue     = apiQueue;
        this.bgapiTracker = bgapiTracker;
        this.eventBus     = eventBus;
    }

    // ── Connection ────────────────────────────────────────────────────────────

    /**
     * Sends the {@code connect} command and returns the initial channel variable event.
     *
     * <p>Idempotent — if already connected, returns the cached channel data immediately
     * without sending another command to FreeSWITCH. This means user handlers can safely
     * call {@code connect()} even when the server has already auto-connected for routing.
     */
    public CompletableFuture<EslEvent> connect() {
        if (channelData != null) {
            return CompletableFuture.completedFuture(channelData);
        }
        CompletableFuture<CommandReply> future = new CompletableFuture<>();
        channel.eventLoop().execute(() -> {
            pendingQueue.enqueue(future);
            channel.writeAndFlush("connect");
        });
        return future.thenApply(reply -> {
            channelData = EslEvent.fromPlainMessage(reply.getMessage());
            sendCommand("myevents");
            log.debug("[SESSION] [CONNECT] Outbound session ready — uuid=[{}]", getUniqueId());
            return channelData;
        });
    }

    // ── Core execute ──────────────────────────────────────────────────────────

    /**
     * Executes any dialplan application on the connected channel.
     * Blocks (async) until {@code CHANNEL_EXECUTE_COMPLETE} is received.
     *
     * @param app  application name (see {@link DpTools})
     * @param args application argument string
     */
    public CompletableFuture<CommandReply> execute(String app, String args) {
        return execute(app, args, false);
    }

    /**
     * Executes a dialplan application.
     *
     * @param async if {@code true}, does not wait for {@code CHANNEL_EXECUTE_COMPLETE}
     */
    public CompletableFuture<CommandReply> execute(String app, String args, boolean async) {
        return sendRaw(SendMsg.execute(app).arg(args).async(async).build().toFrame());
    }

    /**
     * Executes a dialplan application with a large argument body (uses Content-Length).
     * Use when {@code args} is multiline or very long.
     */
    public CompletableFuture<CommandReply> executeWithBody(String app, String body) {
        return sendRaw(SendMsg.execute(app).body(body).build().toFrame());
    }

    /**
     * Executes a typed dialplan application builder.
     *
     * <pre>{@code
     * session.execute(new PlayAndGetDigitsApp()
     *     .min(1).max(1).tries(3).timeout(5000)
     *     .prompt("/tmp/press-1.wav").variable("dtmf_result")).join();
     * }</pre>
     */
    public CompletableFuture<CommandReply> execute(DpApp app) {
        return execute(app.appName(), app.toArg());
    }

    // ── Call control ─────────────────────────────────────────────────────────

    /** Answer the call. */
    public CompletableFuture<CommandReply> answer() {
        return execute(DpTools.ANSWER, "");
    }

    /** Answer in early-media mode. */
    public CompletableFuture<CommandReply> preAnswer() {
        return execute(DpTools.PRE_ANSWER, "");
    }

    /** Indicate ringing without answering. */
    public CompletableFuture<CommandReply> ringReady() {
        return execute(DpTools.RING_READY, "");
    }

    /** Hangup with {@link HangupCause#NORMAL_CLEARING}. */
    public CompletableFuture<CommandReply> hangup() {
        return hangup(HangupCause.NORMAL_CLEARING);
    }

    /** Hangup with a specific cause. */
    public CompletableFuture<CommandReply> hangup(HangupCause cause) {
        return execute(DpTools.HANGUP, cause.name());
    }

    /** Redirect (SIP 3xx). @param sipUri the redirect target URI. */
    public CompletableFuture<CommandReply> redirect(String sipUri) {
        return execute(DpTools.REDIRECT, sipUri);
    }

    /** Send a SIP REFER. */
    public CompletableFuture<CommandReply> deflect(String sipUri) {
        return execute(DpTools.DEFLECT, sipUri);
    }

    // ── Routing ──────────────────────────────────────────────────────────────

    /**
     * Bridge to another endpoint.
     * @param dialString e.g. {@code "sofia/default/1001@domain.com"}
     */
    public CompletableFuture<CommandReply> bridge(String dialString) {
        return execute(DpTools.BRIDGE, dialString);
    }

    /**
     * Transfer to an extension.
     * @param destination extension number
     */
    public CompletableFuture<CommandReply> transfer(String destination) {
        return execute(DpTools.TRANSFER, destination);
    }

    /**
     * Transfer with dialplan and context.
     */
    public CompletableFuture<CommandReply> transfer(String destination, String dialplan, String context) {
        return execute(DpTools.TRANSFER, destination + " " + dialplan + " " + context);
    }

    /**
     * Execute another extension and return.
     */
    public CompletableFuture<CommandReply> executeExtension(String destination, String dialplan, String context) {
        return execute(DpTools.EXECUTE_EXTENSION, destination + " " + dialplan + " " + context);
    }

    // ── Hold / Park ───────────────────────────────────────────────────────────

    /** Park the call. */
    public CompletableFuture<CommandReply> park() {
        return execute(DpTools.PARK, "");
    }

    /** Put on hold with optional MOH path. Pass {@code null} for default hold. */
    public CompletableFuture<CommandReply> hold(String mohPath) {
        return execute(DpTools.HOLD, mohPath != null ? mohPath : "");
    }

    public CompletableFuture<CommandReply> hold() {
        return hold(null);
    }

    /** Soft-hold (hold while bridged). */
    public CompletableFuture<CommandReply> softHold(String mohPath) {
        return execute(DpTools.SOFT_HOLD, mohPath != null ? mohPath : "");
    }

    // ── Playback ─────────────────────────────────────────────────────────────

    /** Play an audio file. */
    public CompletableFuture<CommandReply> playback(String path) {
        return execute(DpTools.PLAYBACK, path);
    }

    /**
     * Speak text via TTS.
     * @param engine TTS engine name (e.g. {@code "flite"})
     * @param voice  voice name (e.g. {@code "kal"})
     * @param text   text to speak
     */
    public CompletableFuture<CommandReply> speak(String engine, String voice, String text) {
        return execute(DpTools.SPEAK, engine + "|" + voice + "|" + text);
    }

    /**
     * Say a phrase using pre-recorded prompts.
     * @param module  language module (e.g. {@code "en"})
     * @param type    say type (e.g. {@code "CURRENT_DATE_TIME"}, {@code "NUMBER"})
     * @param method  say method (e.g. {@code "pronounced"}, {@code "iterated"})
     * @param value   value to say
     */
    public CompletableFuture<CommandReply> say(String module, String type, String method, String value) {
        return execute(DpTools.SAY, module + " " + type + " " + method + " " + value);
    }

    /** Shorthand say without explicit method. */
    public CompletableFuture<CommandReply> say(String module, String type, String value) {
        return execute(DpTools.SAY, module + " " + type + " " + value);
    }

    /** Say a phrase by name. */
    public CompletableFuture<CommandReply> phrase(String phraseName, String args) {
        return execute(DpTools.PHRASE, phraseName + (args != null ? "," + args : ""));
    }

    /** Generate TGML tones. */
    public CompletableFuture<CommandReply> gentones(String tgmlString) {
        return execute(DpTools.GENTONES, tgmlString);
    }

    // ── Recording ────────────────────────────────────────────────────────────

    /**
     * Record from channel input.
     * @param path           file path
     * @param maxSeconds     max recording duration (0 = unlimited)
     * @param silenceThresh  silence threshold (0 = default)
     * @param silenceHits    number of silence hits before stopping (0 = default)
     */
    public CompletableFuture<CommandReply> record(String path, int maxSeconds,
                                                   int silenceThresh, int silenceHits) {
        StringBuilder args = new StringBuilder(path);
        if (maxSeconds > 0)    args.append(' ').append(maxSeconds);
        if (silenceThresh > 0) args.append(' ').append(silenceThresh);
        if (silenceHits > 0)   args.append(' ').append(silenceHits);
        return execute(DpTools.RECORD, args.toString());
    }

    public CompletableFuture<CommandReply> record(String path) {
        return record(path, 0, 0, 0);
    }

    /** Record the entire session (both legs) to a file. */
    public CompletableFuture<CommandReply> recordSession(String path) {
        return execute(DpTools.RECORD_SESSION, path);
    }

    // ── DTMF ─────────────────────────────────────────────────────────────────

    /** Queue DTMF digits to be sent after bridge. */
    public CompletableFuture<CommandReply> queueDtmf(String digits) {
        return execute(DpTools.QUEUE_DTMF, digits);
    }

    /** Send DTMF immediately. */
    public CompletableFuture<CommandReply> sendDtmf(String digits) {
        return execute(DpTools.SEND_DTMF, digits);
    }

    /** Start tone detection. @param toneSpec tone specification string. */
    public CompletableFuture<CommandReply> toneDetect(String toneSpec) {
        return execute(DpTools.TONE_DETECT, toneSpec);
    }

    /** Stop tone detection. */
    public CompletableFuture<CommandReply> stopToneDetect() {
        return execute(DpTools.STOP_TONE_DETECT, "");
    }

    // ── Speech recognition ────────────────────────────────────────────────────

    /**
     * Start speech recognition.
     * @param engine  ASR engine name
     * @param grammar grammar name or path
     */
    public CompletableFuture<CommandReply> detectSpeech(String engine, String grammar) {
        return execute(DpTools.DETECT_SPEECH, engine + " " + grammar);
    }

    /** Stop speech recognition. */
    public CompletableFuture<CommandReply> stopSpeech() {
        return execute(DpTools.DETECT_SPEECH, "stop");
    }

    // ── Waiting ───────────────────────────────────────────────────────────────

    /** Pause for {@code ms} milliseconds. */
    public CompletableFuture<CommandReply> sleep(int ms) {
        return execute(DpTools.SLEEP, String.valueOf(ms));
    }

    /**
     * Wait for silence.
     * @param threshold    energy level for silence detection (500 typical)
     * @param seconds      seconds of silence required
     * @param hits         consecutive hits required (optional, 0 = default)
     */
    public CompletableFuture<CommandReply> waitForSilence(int threshold, int seconds, int hits) {
        String args = threshold + " " + seconds + (hits > 0 ? " " + hits : "");
        return execute(DpTools.WAIT_FOR_SILENCE, args);
    }

    // ── Conference / Queue ────────────────────────────────────────────────────

    /**
     * Enter a conference room.
     * @param room    conference room name
     * @param profile conference profile name (e.g. {@code "default"})
     * @param flags   optional flags (e.g. {@code "mute|deaf"})
     */
    public CompletableFuture<CommandReply> conference(String room, String profile, String flags) {
        StringBuilder args = new StringBuilder(room).append('@').append(profile);
        if (flags != null && !flags.isBlank()) args.append('+').append(flags);
        return execute(DpTools.CONFERENCE, args.toString());
    }

    public CompletableFuture<CommandReply> conference(String room) {
        return execute(DpTools.CONFERENCE, room);
    }

    /** Enter a FIFO queue. */
    public CompletableFuture<CommandReply> fifo(String fifoName, String direction, String announceSound) {
        StringBuilder args = new StringBuilder(fifoName);
        if (direction != null) args.append(' ').append(direction);
        if (announceSound != null) args.append(' ').append(announceSound);
        return execute(DpTools.FIFO, args.toString());
    }

    // ── Echo / test ───────────────────────────────────────────────────────────

    /** Echo audio and video back to caller. */
    public CompletableFuture<CommandReply> echo() {
        return execute(DpTools.ECHO, "");
    }

    // ── Interception ─────────────────────────────────────────────────────────

    /** Intercept a channel by its UUID. */
    public CompletableFuture<CommandReply> intercept(String targetUuid) {
        return execute(DpTools.INTERCEPT, targetUuid);
    }

    /** Eavesdrop on a channel. */
    public CompletableFuture<CommandReply> eavesdrop(String targetUuid) {
        return execute(DpTools.EAVESDROP, targetUuid);
    }

    // ── Channel variables ─────────────────────────────────────────────────────

    /** Set a channel variable. */
    public CompletableFuture<CommandReply> setVar(String name, String value) {
        return execute(DpTools.SET, name + "=" + value);
    }

    /**
     * Export a variable to the B-leg.
     * @param noLocal if {@code true}, sets only on B-leg (not locally)
     */
    public CompletableFuture<CommandReply> exportVar(String name, String value, boolean noLocal) {
        String prefix = noLocal ? "nolocal:" : "";
        return execute(DpTools.EXPORT, prefix + name + "=" + value);
    }

    public CompletableFuture<CommandReply> exportVar(String name, String value) {
        return exportVar(name, value, false);
    }

    /** Unset a channel variable. */
    public CompletableFuture<CommandReply> unsetVar(String name) {
        return execute(DpTools.UNSET, name);
    }

    /**
     * Set multiple variables at once.
     * @param vars map of name → value
     */
    public CompletableFuture<CommandReply> multisetVar(Map<String, String> vars) {
        StringJoiner sj = new StringJoiner(" ");
        vars.forEach((k, v) -> sj.add(k + "=" + v));
        return execute(DpTools.MULTISET, sj.toString());
    }

    // ── Attended transfer ─────────────────────────────────────────────────────

    /**
     * Attended transfer — bridge this channel to {@code transferToUuid} while
     * keeping the original A-leg on hold, then complete the transfer.
     * @param transferToUuid UUID of the channel to transfer to
     */
    public CompletableFuture<CommandReply> attendedTransfer(String transferToUuid) {
        return execute(DpTools.ATT_XFER, transferToUuid);
    }

    // ── Digit collection ─────────────────────────────────────────────────────

    /**
     * Play a file and collect DTMF digits.
     *
     * @param minDigits    minimum digits to collect
     * @param maxDigits    maximum digits to collect
     * @param tries        number of tries if not enough digits received
     * @param timeoutMs    inter-digit timeout in milliseconds
     * @param terminators  terminator digits (e.g. {@code "#"}) or {@code ""}
     * @param file         prompt file to play
     * @param varName      channel variable to store the result in
     * @param regexp       optional validation regex, or {@code ""}
     * @param digitTimeout initial digit timeout in milliseconds (0 = use timeoutMs)
     */
    public CompletableFuture<CommandReply> playAndGetDigits(int minDigits, int maxDigits,
                                                             int tries, int timeoutMs,
                                                             String terminators, String file,
                                                             String varName, String regexp,
                                                             int digitTimeout) {
        StringBuilder args = new StringBuilder()
                .append(minDigits).append(' ')
                .append(maxDigits).append(' ')
                .append(tries).append(' ')
                .append(timeoutMs).append(' ')
                .append(terminators != null ? terminators : "").append(' ')
                .append(file).append(' ')
                .append(varName);
        if (regexp != null && !regexp.isBlank()) args.append(' ').append(regexp);
        if (digitTimeout > 0) args.append(' ').append(digitTimeout);
        return execute(DpTools.PLAY_AND_GET_DIGITS, args.toString());
    }

    /**
     * Read DTMF digits into a channel variable.
     *
     * @param minDigits  minimum digits required
     * @param maxDigits  maximum digits allowed
     * @param file       prompt file to play
     * @param varName    channel variable to store result in
     * @param timeoutMs  inter-digit timeout in milliseconds
     * @param terminators terminator digits (e.g. {@code "#"})
     */
    public CompletableFuture<CommandReply> read(int minDigits, int maxDigits,
                                                 String file, String varName,
                                                 int timeoutMs, String terminators) {
        String args = minDigits + " " + maxDigits + " " + file + " " + varName
                + " " + timeoutMs + (terminators != null ? " " + terminators : "");
        return execute(DpTools.READ, args);
    }

    // ── DTMF control ─────────────────────────────────────────────────────────

    /** Flush any queued DTMF digits. */
    public CompletableFuture<CommandReply> flushDtmf() {
        return execute(DpTools.FLUSH_DTMF, "");
    }

    /** Start inband DTMF detection. */
    public CompletableFuture<CommandReply> startDtmf() {
        return execute(DpTools.START_DTMF, "");
    }

    /** Stop inband DTMF detection. */
    public CompletableFuture<CommandReply> stopDtmf() {
        return execute(DpTools.STOP_DTMF, "");
    }

    /** Start inband DTMF generation. */
    public CompletableFuture<CommandReply> startDtmfGenerate() {
        return execute(DpTools.START_DTMF_GENERATE, "");
    }

    /** Stop inband DTMF generation. */
    public CompletableFuture<CommandReply> stopDtmfGenerate() {
        return execute(DpTools.STOP_DTMF_GENERATE, "");
    }

    /** Block DTMF from being sent or received. @param block {@code true} to block, {@code false} to unblock. */
    public CompletableFuture<CommandReply> blockDtmf(boolean block) {
        return execute(DpTools.BLOCK_DTMF, block ? "block" : "unblock");
    }

    // ── Audio displacement ────────────────────────────────────────────────────

    /**
     * Displace (overlay) audio on the channel.
     * @param file   audio file to play as overlay
     * @param flags  optional flags (e.g. {@code "m"} for mux) or {@code null}
     * @param limit  max seconds, 0 for unlimited
     */
    public CompletableFuture<CommandReply> displace(String file, String flags, int limit) {
        StringBuilder args = new StringBuilder(file);
        if (flags != null && !flags.isBlank()) args.append(' ').append(flags);
        if (limit > 0) args.append(' ').append(limit);
        return execute(DpTools.DISPLACE_SESSION, args.toString());
    }

    /** Stop audio displacement. @param file the same file path used in {@link #displace}. */
    public CompletableFuture<CommandReply> stopDisplace(String file) {
        return execute(DpTools.STOP_DISPLACE_SESSION, file);
    }

    // ── Recording control ─────────────────────────────────────────────────────

    /** Stop recording the session that was started with {@link #recordSession(String)}. */
    public CompletableFuture<CommandReply> stopRecordSession(String path) {
        return execute(DpTools.STOP_RECORD_SESSION, path);
    }

    // ── Hold / unhold ─────────────────────────────────────────────────────────

    /** Take the channel off hold. */
    public CompletableFuture<CommandReply> unhold() {
        return execute(DpTools.UNHOLD, "");
    }

    // ── Waiting ───────────────────────────────────────────────────────────────

    /**
     * Pause execution until the call is answered.
     * @param timeoutMs max wait in milliseconds (0 = wait indefinitely)
     */
    public CompletableFuture<CommandReply> waitForAnswer(int timeoutMs) {
        return execute(DpTools.WAIT_FOR_ANSWER,
                timeoutMs > 0 ? String.valueOf(timeoutMs) : "");
    }

    // ── Audio level ───────────────────────────────────────────────────────────

    /**
     * Adjust read or write audio level.
     * @param direction {@code "read"} (inbound from caller) or {@code "write"} (outbound to caller)
     * @param level     gain level: positive to amplify, negative to attenuate (e.g. {@code 4})
     */
    public CompletableFuture<CommandReply> setAudioLevel(String direction, int level) {
        return execute(DpTools.SET_AUDIO_LEVEL, direction + " " + level);
    }

    // ── Scheduled actions ─────────────────────────────────────────────────────

    /**
     * Schedule a hangup.
     * @param when  relative time (e.g. {@code "+30"} for 30 seconds) or absolute epoch
     * @param cause hangup cause, or {@code null} for {@code ALLOTTED_TIMEOUT}
     */
    public CompletableFuture<CommandReply> schedHangup(String when, HangupCause cause) {
        return execute(DpTools.SCHED_HANGUP,
                when + (cause != null ? " " + cause.name() : ""));
    }

    /** Schedule a hangup with default cause ({@code ALLOTTED_TIMEOUT}). */
    public CompletableFuture<CommandReply> schedHangup(String when) {
        return schedHangup(when, null);
    }

    /**
     * Schedule a transfer.
     * @param when        relative time (e.g. {@code "+60"})
     * @param destination extension to transfer to
     * @param dialplan    dialplan name (e.g. {@code "XML"}) or {@code null}
     * @param context     dialplan context or {@code null}
     */
    public CompletableFuture<CommandReply> schedTransfer(String when, String destination,
                                                          String dialplan, String context) {
        StringBuilder args = new StringBuilder(when).append(' ').append(destination);
        if (dialplan != null) args.append(' ').append(dialplan);
        if (context  != null) args.append(' ').append(context);
        return execute(DpTools.SCHED_TRANSFER, args.toString());
    }

    /** Schedule a transfer using default dialplan/context. */
    public CompletableFuture<CommandReply> schedTransfer(String when, String destination) {
        return schedTransfer(when, destination, null, null);
    }

    /**
     * Schedule a broadcast (play file) on this channel.
     * @param when time spec (e.g. {@code "+30"})
     * @param path audio file to play
     * @param leg  {@code "aleg"}, {@code "bleg"}, or {@code "both"} — or {@code null}
     */
    public CompletableFuture<CommandReply> schedBroadcast(String when, String path, String leg) {
        String args = when + " " + path + (leg != null ? " " + leg : "");
        return execute(DpTools.SCHED_BROADCAST, args);
    }

    /** Cancel a scheduled action by task ID or UUID. */
    public CompletableFuture<CommandReply> schedCancel(String taskIdOrUuid) {
        return execute(DpTools.SCHED_CANCEL, taskIdOrUuid);
    }

    // ── Media ─────────────────────────────────────────────────────────────────

    /** Reset all bypass/proxy media flags on the channel. */
    public CompletableFuture<CommandReply> mediaReset() {
        return execute(DpTools.MEDIA_RESET, "");
    }

    // ── Logging / debug ───────────────────────────────────────────────────────

    /**
     * Log a message at the specified level.
     * @param level   log level (e.g. {@code "DEBUG"}, {@code "INFO"}, {@code "WARNING"})
     * @param message message to log
     */
    public CompletableFuture<CommandReply> log(String level, String message) {
        return execute(DpTools.LOG, level + " " + message);
    }

    /** Dump call info to the log. */
    public CompletableFuture<CommandReply> info() {
        return execute(DpTools.INFO, "");
    }

    /** Make all events verbose for this channel (includes all variables in every event). */
    public CompletableFuture<CommandReply> verboseEvents() {
        return execute(DpTools.VERBOSE_EVENTS, "");
    }

    /**
     * Override the log level for this channel.
     * @param level log level name (e.g. {@code "DEBUG"})
     */
    public CompletableFuture<CommandReply> sessionLogLevel(String level) {
        return execute(DpTools.SESSION_LOGLEVEL, level);
    }

    // ── Callcenter queue ─────────────────────────────────────────────────────

    /**
     * Route the caller into an ACD call-center queue.
     * @param queue queue name (e.g. {@code "support_queue@default"})
     */
    public CompletableFuture<CommandReply> callcenter(String queue) {
        return execute(DpTools.CALLCENTER, queue);
    }

    // ── Meta app ─────────────────────────────────────────────────────────────

    /**
     * Bind a DTMF key to execute an application during a bridge.
     * @param dtmfKey  DTMF digit (0-9, *, #)
     * @param leg      {@code "a"}, {@code "b"}, or {@code "ab"}
     * @param app      application to execute
     * @param args     application arguments
     */
    public CompletableFuture<CommandReply> bindMetaApp(String dtmfKey, String leg,
                                                        String app, String args) {
        return execute(DpTools.BIND_META_APP,
                dtmfKey + " " + leg + " " + app + (args != null ? " " + args : ""));
    }

    /** Unbind a previously bound meta-app DTMF key. */
    public CompletableFuture<CommandReply> unbindMetaApp(String dtmfKey) {
        return execute(DpTools.UNBIND_META_APP, dtmfKey);
    }

    // ── API commands ─────────────────────────────────────────────────────────

    /**
     * Executes a synchronous FreeSWITCH API command from within an outbound session.
     *
     * @param command e.g. {@code "status"} or {@code "uuid_kill " + getUniqueId()}
     */
    public CompletableFuture<ApiResponse> api(String command) {
        CompletableFuture<CommandReply> future = new CompletableFuture<>();
        channel.eventLoop().execute(() -> {
            apiQueue.enqueue(future);
            channel.writeAndFlush("api " + command);
        });
        return future.thenApply(reply -> ApiResponse.of(reply.getMessage()));
    }

    /**
     * Executes a background API command. The result future completes when the
     * {@code BACKGROUND_JOB} event arrives.
     */
    public CompletableFuture<ApiResponse> bgapi(String command) {
        CompletableFuture<ApiResponse> resultFuture = new CompletableFuture<>();
        CompletableFuture<CommandReply> replyFuture = new CompletableFuture<>();
        channel.eventLoop().execute(() -> {
            pendingQueue.enqueue(replyFuture);
            channel.writeAndFlush("bgapi " + command);
        });
        replyFuture.thenAccept(reply -> {
            String uuid = reply.getJobUuid();
            if (uuid == null || uuid.isBlank()) {
                resultFuture.completeExceptionally(
                        new IllegalStateException("bgapi reply missing Job-UUID: " + reply));
                return;
            }
            bgapiTracker.register(uuid, resultFuture);
        }).exceptionally(ex -> { resultFuture.completeExceptionally(ex); return null; });
        return resultFuture;
    }

    // ── Linger ───────────────────────────────────────────────────────────────

    /**
     * Sends {@code linger}: keep socket open after channel hangup to receive remaining events.
     */
    public CompletableFuture<CommandReply> linger() {
        return sendCommand("linger");
    }

    /** Sends {@code nolinger}. */
    public CompletableFuture<CommandReply> noLinger() {
        return sendCommand("nolinger");
    }

    // ── Sub-API accessors ─────────────────────────────────────────────────────

    /** Returns a {@link com.freeswitchjava.esl.api.Callcenter} for queue management. */
    public com.freeswitchjava.esl.api.Callcenter callcenter() {
        return new com.freeswitchjava.esl.api.Callcenter(this::api);
    }

    // ── Event listeners ───────────────────────────────────────────────────────

    public EventBus.EventRegistration addEventListener(String eventName, Consumer<EslEvent> listener) {
        return eventBus.register(eventName, listener);
    }

    public void removeEventListener(EventBus.EventRegistration registration) {
        eventBus.unregister(registration);
    }

    // ── State accessors ───────────────────────────────────────────────────────

    /** Returns the channel UUID, available after {@link #connect()} completes. */
    public String getUniqueId() {
        return channelData != null ? channelData.getUniqueId() : null;
    }

    /** Returns a specific channel variable from the initial connect response. */
    public String getChannelVar(String name) {
        return channelData != null ? channelData.getHeader(name) : null;
    }

    /** Returns the full channel data event from the initial connect response. */
    public EslEvent getChannelData() {
        return channelData;
    }

    /**
     * Returns a typed view of the initial channel data for easy access to caller ID,
     * context, SIP headers, and other channel variables — no raw header strings needed.
     * Available after {@link #connect()} completes.
     */
    public OutboundSessionRequest getRequest() {
        return channelData != null ? new OutboundSessionRequest(channelData) : null;
    }

    /** Returns a future that completes when the channel TCP connection closes. */
    public CompletableFuture<Void> awaitDisconnect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        channel.closeFuture().addListener(f -> future.complete(null));
        return future;
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    CompletableFuture<CommandReply> sendCommand(String command) {
        CompletableFuture<CommandReply> future = new CompletableFuture<>();
        channel.eventLoop().execute(() -> {
            pendingQueue.enqueue(future);
            channel.writeAndFlush(command);
        });
        return future;
    }

    private CompletableFuture<CommandReply> sendRaw(String frame) {
        CompletableFuture<CommandReply> future = new CompletableFuture<>();
        channel.eventLoop().execute(() -> {
            pendingQueue.enqueue(future);
            channel.writeAndFlush(frame);
        });
        return future;
    }
}
