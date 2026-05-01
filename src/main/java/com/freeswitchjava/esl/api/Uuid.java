package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;
import com.freeswitchjava.esl.model.HangupCause;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Fluent API for all FreeSWITCH {@code uuid_*} channel-control commands.
 *
 * <p>Obtain an instance via {@code client.uuid(channelUuid)}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * client.uuid("abc-123")
 *       .hold()
 *       .thenCompose(r -> client.uuid("abc-123").setVar("my_var", "hello"))
 *       .join();
 * }</pre>
 */
public final class Uuid {

    private final String uuid;
    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public Uuid(String uuid, Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.apiExecutor = Objects.requireNonNull(apiExecutor);
    }

    // ── Call control ─────────────────────────────────────────────────────────

    /** {@code uuid_answer <uuid>} — Answer the channel. */
    public CompletableFuture<ApiResponse> answer() {
        return api("uuid_answer " + uuid);
    }

    /** {@code uuid_pre_answer <uuid>} — Early media / pre-answer. */
    public CompletableFuture<ApiResponse> preAnswer() {
        return api("uuid_pre_answer " + uuid);
    }

    /** {@code uuid_early_ok <uuid>} — Send 200 OK in early media. */
    public CompletableFuture<ApiResponse> earlyOk() {
        return api("uuid_early_ok " + uuid);
    }

    /** {@code uuid_kill <uuid>} — Hangup with NORMAL_CLEARING. */
    public CompletableFuture<ApiResponse> kill() {
        return api("uuid_kill " + uuid);
    }

    /** {@code uuid_kill <uuid> <cause>} — Hangup with a specific cause. */
    public CompletableFuture<ApiResponse> kill(HangupCause cause) {
        return api("uuid_kill " + uuid + " " + cause.name());
    }

    // ── Hold / Park ───────────────────────────────────────────────────────────

    /** {@code uuid_hold <uuid>} — Put channel on hold. */
    public CompletableFuture<ApiResponse> hold() {
        return api("uuid_hold " + uuid);
    }

    /** {@code uuid_hold off <uuid>} — Take channel off hold. */
    public CompletableFuture<ApiResponse> unhold() {
        return api("uuid_hold off " + uuid);
    }

    /** {@code uuid_hold toggle <uuid>} — Toggle hold state. */
    public CompletableFuture<ApiResponse> toggleHold() {
        return api("uuid_hold toggle " + uuid);
    }

    /** {@code uuid_park <uuid>} — Park the channel. */
    public CompletableFuture<ApiResponse> park() {
        return api("uuid_park " + uuid);
    }

    // ── Bridge / Transfer ─────────────────────────────────────────────────────

    /**
     * {@code uuid_bridge <uuid> <other_uuid>} — Bridge two call legs together.
     */
    public CompletableFuture<ApiResponse> bridge(String otherUuid) {
        return api("uuid_bridge " + uuid + " " + otherUuid);
    }

    /**
     * {@code uuid_transfer <uuid> <dest>} — Transfer to extension in default dialplan/context.
     */
    public CompletableFuture<ApiResponse> transfer(String destination) {
        return api("uuid_transfer " + uuid + " " + destination);
    }

    /**
     * {@code uuid_transfer <uuid> [-bleg|-both] <dest> [dialplan] [context]} — Full transfer syntax.
     *
     * @param leg      {@code null}, {@code "-bleg"}, or {@code "-both"}
     * @param dialplan dialplan name (e.g. {@code "XML"}) or null for default
     * @param context  dialplan context or null for default
     */
    public CompletableFuture<ApiResponse> transfer(String destination, String leg,
                                                   String dialplan, String context) {
        StringBuilder sb = new StringBuilder("uuid_transfer ").append(uuid);
        if (leg != null) sb.append(' ').append(leg);
        sb.append(' ').append(destination);
        if (dialplan != null) sb.append(' ').append(dialplan);
        if (context != null)  sb.append(' ').append(context);
        return api(sb.toString());
    }

    /** {@code uuid_deflect <uuid> <sip_url>} — SIP REFER redirect. */
    public CompletableFuture<ApiResponse> deflect(String sipUrl) {
        return api("uuid_deflect " + uuid + " " + sipUrl);
    }

    // ── Media ─────────────────────────────────────────────────────────────────

    /**
     * {@code uuid_broadcast <uuid> <path> [aleg|bleg|both]} — Play file to channel leg(s).
     */
    public CompletableFuture<ApiResponse> broadcast(String path, String leg) {
        return api("uuid_broadcast " + uuid + " " + path + (leg != null ? " " + leg : ""));
    }

    /**
     * {@code uuid_broadcast <uuid> app::args [aleg|bleg|both]} — Execute app on leg(s).
     */
    public CompletableFuture<ApiResponse> broadcastApp(String app, String args, String leg) {
        return api("uuid_broadcast " + uuid + " " + app + "::" + args + (leg != null ? " " + leg : ""));
    }

    /**
     * {@code uuid_record <uuid> start <path> [<limit_seconds>]} — Start recording.
     */
    public CompletableFuture<ApiResponse> recordStart(String path) {
        return api("uuid_record " + uuid + " start " + path);
    }

    /** {@code uuid_record <uuid> start <path> <limit>} — Start recording with time limit. */
    public CompletableFuture<ApiResponse> recordStart(String path, int limitSeconds) {
        return api("uuid_record " + uuid + " start " + path + " " + limitSeconds);
    }

    /** {@code uuid_record <uuid> stop <path>} — Stop recording. */
    public CompletableFuture<ApiResponse> recordStop(String path) {
        return api("uuid_record " + uuid + " stop " + path);
    }

    /** {@code uuid_record <uuid> mask <path>} — Mask (mute) recording at path. */
    public CompletableFuture<ApiResponse> recordMask(String path) {
        return api("uuid_record " + uuid + " mask " + path);
    }

    /** {@code uuid_record <uuid> unmask <path>} — Unmask recording at path. */
    public CompletableFuture<ApiResponse> recordUnmask(String path) {
        return api("uuid_record " + uuid + " unmask " + path);
    }

    /** {@code uuid_displace <uuid> start <file>} — Start audio displacement (overlay). */
    public CompletableFuture<ApiResponse> displaceStart(String file, String flags) {
        return api("uuid_displace " + uuid + " start " + file + (flags != null ? " " + flags : ""));
    }

    /** {@code uuid_displace <uuid> stop <file>} */
    public CompletableFuture<ApiResponse> displaceStop(String file) {
        return api("uuid_displace " + uuid + " stop " + file);
    }

    /**
     * {@code uuid_media <uuid>} — Force media on channel (off proxy mode).
     * {@code uuid_media off <uuid>} — Switch to no-media / proxy mode.
     */
    public CompletableFuture<ApiResponse> media(boolean on) {
        return on ? api("uuid_media " + uuid) : api("uuid_media off " + uuid);
    }

    // ── DTMF ──────────────────────────────────────────────────────────────────

    /**
     * {@code uuid_send_dtmf <uuid> <digits>[@<duration_ms>]} — Send DTMF to channel.
     *
     * @param digits   digits to send (0-9, *, #, A-D)
     * @param durationMs tone duration in milliseconds, or 0 for default
     */
    public CompletableFuture<ApiResponse> sendDtmf(String digits, int durationMs) {
        String cmd = "uuid_send_dtmf " + uuid + " " + digits;
        if (durationMs > 0) cmd += "@" + durationMs;
        return api(cmd);
    }

    /** {@code uuid_send_dtmf <uuid> <digits>} with default tone duration. */
    public CompletableFuture<ApiResponse> sendDtmf(String digits) {
        return sendDtmf(digits, 0);
    }

    /** {@code uuid_recv_dtmf <uuid> <dtmf>} — Inject DTMF as if received from remote end. */
    public CompletableFuture<ApiResponse> receiveDtmf(String digits) {
        return api("uuid_recv_dtmf " + uuid + " " + digits);
    }

    // ── Variables ─────────────────────────────────────────────────────────────

    /** {@code uuid_getvar <uuid> <name>} — Get a channel variable. */
    public CompletableFuture<ApiResponse> getVar(String name) {
        return api("uuid_getvar " + uuid + " " + name);
    }

    /** {@code uuid_setvar <uuid> <name> <value>} — Set a channel variable. */
    public CompletableFuture<ApiResponse> setVar(String name, String value) {
        return api("uuid_setvar " + uuid + " " + name + " " + value);
    }

    /** {@code uuid_setvar <uuid> <name>} — Unset (clear) a channel variable. */
    public CompletableFuture<ApiResponse> unsetVar(String name) {
        return api("uuid_setvar " + uuid + " " + name);
    }

    /**
     * {@code uuid_setvar_multi <uuid> var1=val1;var2=val2;...} — Set multiple variables atomically.
     */
    public CompletableFuture<ApiResponse> setVarMulti(Map<String, String> vars) {
        StringJoiner sj = new StringJoiner(";");
        vars.forEach((k, v) -> sj.add(k + "=" + v));
        return api("uuid_setvar_multi " + uuid + " " + sj);
    }

    /**
     * {@code uuid_dump <uuid> [format]} — Dump all session variables.
     *
     * @param format {@code "txt"}, {@code "xml"}, {@code "json"}, or {@code "plain"}
     */
    public CompletableFuture<ApiResponse> dump(String format) {
        return api("uuid_dump " + uuid + (format != null ? " " + format : ""));
    }

    /** {@code uuid_dump <uuid>} in plain text format. */
    public CompletableFuture<ApiResponse> dump() {
        return dump(null);
    }

    // ── Pause / Resume ────────────────────────────────────────────────────────

    /** {@code uuid_pause <uuid> on} — Pause media on the channel. */
    public CompletableFuture<ApiResponse> pause() {
        return api("uuid_pause " + uuid + " on");
    }

    /** {@code uuid_pause <uuid> off} — Resume media on the channel. */
    public CompletableFuture<ApiResponse> resumeMedia() {
        return api("uuid_pause " + uuid + " off");
    }

    // ── DTMF ──────────────────────────────────────────────────────────────────

    /** {@code uuid_flush_dtmf <uuid>} — Discard any queued DTMF digits. */
    public CompletableFuture<ApiResponse> flushDtmf() {
        return api("uuid_flush_dtmf " + uuid);
    }

    // ── Audio control ─────────────────────────────────────────────────────────

    /**
     * {@code uuid_audio <uuid> start <direction> <level>} — Adjust audio gain.
     *
     * @param direction {@code "read"} (inbound from caller) or {@code "write"} (outbound to caller)
     * @param level     gain level, positive to amplify, negative to attenuate (e.g. {@code 4})
     */
    public CompletableFuture<ApiResponse> audioStart(String direction, int level) {
        return api("uuid_audio " + uuid + " start " + direction + " " + level);
    }

    /** {@code uuid_audio <uuid> stop} — Stop audio gain adjustment. */
    public CompletableFuture<ApiResponse> audioStop() {
        return api("uuid_audio " + uuid + " stop");
    }

    // ── Jitter buffer ─────────────────────────────────────────────────────────

    /**
     * {@code uuid_jitterbuffer <uuid> <ms>} — Set jitter buffer size in milliseconds.
     * Pass {@code 0} to disable.
     */
    public CompletableFuture<ApiResponse> jitterbuffer(int ms) {
        return api("uuid_jitterbuffer " + uuid + " " + ms);
    }

    // ── Video ─────────────────────────────────────────────────────────────────

    /** {@code uuid_video_refresh <uuid>} — Request a video keyframe from the remote end. */
    public CompletableFuture<ApiResponse> videoRefresh() {
        return api("uuid_video_refresh " + uuid);
    }

    // ── Simplify ─────────────────────────────────────────────────────────────

    /** {@code uuid_simplify <uuid>} — Simplify a bridged call to peer-to-peer (bypass FreeSWITCH media). */
    public CompletableFuture<ApiResponse> simplify() {
        return api("uuid_simplify " + uuid);
    }

    // ── Resource limits ────────────────────────────────────────────────────────

    /**
     * {@code uuid_limit <uuid> <backend> <realm> <resource> [<max>] [<transfer-dest>]} —
     * Enforce resource limits on the channel.
     *
     * @param backend  limit backend: {@code "db"} or {@code "hash"}
     * @param realm    namespace for the limit
     * @param resource resource name
     * @param max      maximum concurrent usage (-1 = no limit)
     */
    public CompletableFuture<ApiResponse> limit(String backend, String realm, String resource, int max) {
        return api("uuid_limit " + uuid + " " + backend + " " + realm + " " + resource + " " + max);
    }

    /** {@code uuid_limit_release <uuid> [<backend> <realm> <resource>]} — Release a resource limit. */
    public CompletableFuture<ApiResponse> limitRelease(String backend, String realm, String resource) {
        return api("uuid_limit_release " + uuid + " " + backend + " " + realm + " " + resource);
    }

    /** {@code uuid_limit_release <uuid>} — Release all resource limits on this channel. */
    public CompletableFuture<ApiResponse> limitReleaseAll() {
        return api("uuid_limit_release " + uuid);
    }

    // ── Phone event ───────────────────────────────────────────────────────────

    /**
     * {@code uuid_phone_event <uuid> <event>} — Inject a phone event (e.g. off-hook, on-hook).
     *
     * @param event event string, e.g. {@code "talk"} or {@code "notalk"}
     */
    public CompletableFuture<ApiResponse> phoneEvent(String event) {
        return api("uuid_phone_event " + uuid + " " + event);
    }

    // ── Misc ──────────────────────────────────────────────────────────────────

    /** {@code uuid_chat <uuid> <text>} — Send a chat message to the channel's SIP endpoint. */
    public CompletableFuture<ApiResponse> chat(String message) {
        return api("uuid_chat " + uuid + " " + message);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private CompletableFuture<ApiResponse> api(String command) {
        return apiExecutor.apply(command);
    }
}
