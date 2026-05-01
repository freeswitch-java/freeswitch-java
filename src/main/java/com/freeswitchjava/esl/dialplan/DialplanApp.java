package com.freeswitchjava.esl.dialplan;

/**
 * Constants for all standard FreeSWITCH dialplan application names.
 *
 * <p>Use with {@link com.freeswitchjava.esl.command.SendMsg#execute(String)} and
 * {@link com.freeswitchjava.esl.outbound.OutboundSession#execute(String, String)}.
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Modules/mod_dptools_1970333/">
 *     mod_dptools documentation</a>
 */
public final class DialplanApp {

    // ── Call control ─────────────────────────────────────────────────────────

    /** Answer the call. No args required. */
    public static final String ANSWER            = "answer";
    /** Answer in early-media mode (183 Session Progress). */
    public static final String PRE_ANSWER        = "pre_answer";
    /** Send 200 OK while in early media without bridging. */
    public static final String EARLY_OK          = "early_ok";
    /** Indicate ringing (180 Ringing) without answering. */
    public static final String RING_READY        = "ring_ready";
    /** Hangup the channel. Arg: optional {@link com.freeswitchjava.esl.model.HangupCause} name. */
    public static final String HANGUP            = "hangup";
    /** Redirect the call (SIP 3xx). Arg: SIP URI. */
    public static final String REDIRECT          = "redirect";
    /** Send a SIP REFER. Arg: SIP URI. */
    public static final String DEFLECT           = "deflect";
    /** Attended transfer. Arg: UUID of channel to transfer to. */
    public static final String ATT_XFER          = "att_xfer";

    // ── Routing ──────────────────────────────────────────────────────────────

    /**
     * Bridge to another channel. Arg: dial string
     * (e.g. {@code "sofia/default/1001@domain.com"}).
     */
    public static final String BRIDGE            = "bridge";
    /**
     * Transfer to an extension. Arg: {@code "dest [dialplan] [context]"}.
     */
    public static final String TRANSFER          = "transfer";
    /**
     * Execute another extension and return. Arg: {@code "dest [dialplan] [context]"}.
     */
    public static final String EXECUTE_EXTENSION = "execute_extension";

    // ── Hold / Park ───────────────────────────────────────────────────────────

    /** Park the call (waits in parking lot). */
    public static final String PARK              = "park";
    /** Put channel on hold with optional music. Arg: path to hold music or empty. */
    public static final String HOLD              = "hold";
    /** Take the channel off hold. */
    public static final String UNHOLD            = "unhold";
    /** Put a bridged channel on hold. Arg: optional moh path. */
    public static final String SOFT_HOLD         = "soft_hold";

    // ── Audio playback ───────────────────────────────────────────────────────

    /** Play an audio file. Arg: path to file. */
    public static final String PLAYBACK          = "playback";
    /**
     * Speak with TTS. Arg: {@code "tts_engine|tts_voice|text to speak"}.
     */
    public static final String SPEAK             = "speak";
    /**
     * Say a phrase using recorded prompts. Arg: {@code "module [lang] [gender] value"}.
     */
    public static final String SAY               = "say";
    /** Play back a phrase by name. Arg: phrase name + optional args. */
    public static final String PHRASE            = "phrase";
    /** Generate TGML tones. Arg: TGML tone string. */
    public static final String GENTONES          = "gentones";

    // ── Audio playback (digit collection) ────────────────────────────────────

    /** Play file and collect DTMF digits. */
    public static final String PLAY_AND_GET_DIGITS = "play_and_get_digits";
    /** Read DTMF digits into a channel variable. */
    public static final String READ              = "read";

    // ── Recording ────────────────────────────────────────────────────────────

    /**
     * Record to a file from channel input. Arg: {@code "file [<max_seconds>] [<silence_hits>] [<silence_threshold>]"}.
     */
    public static final String RECORD            = "record";
    /** Record the entire session (both legs). Arg: path to file. */
    public static final String RECORD_SESSION    = "record_session";
    /** Stop session recording. Arg: path to file. */
    public static final String STOP_RECORD_SESSION = "stop_record_session";

    // ── Audio displacement ────────────────────────────────────────────────────

    /** Overlay audio on a channel. Arg: {@code "file [flags] [limit]"}. */
    public static final String DISPLACE_SESSION      = "displace_session";
    /** Stop audio displacement. Arg: file path. */
    public static final String STOP_DISPLACE_SESSION = "stop_displace_session";

    // ── DTMF ─────────────────────────────────────────────────────────────────

    /** Queue DTMF digits to send after bridge. Arg: digits. */
    public static final String QUEUE_DTMF        = "queue_dtmf";
    /** Send inband / RFC 2833 / SIP Info DTMF now. Arg: digits. */
    public static final String SEND_DTMF         = "send_dtmf";
    /** Flush any queued DTMF digits. */
    public static final String FLUSH_DTMF        = "flush_dtmf";
    /** Start inband DTMF detection. */
    public static final String START_DTMF        = "start_dtmf";
    /** Stop inband DTMF detection. */
    public static final String STOP_DTMF         = "stop_dtmf";
    /** Start inband DTMF generation. */
    public static final String START_DTMF_GENERATE = "start_dtmf_generate";
    /** Stop inband DTMF generation. */
    public static final String STOP_DTMF_GENERATE  = "stop_dtmf_generate";
    /** Block DTMF from being sent or received. Arg: {@code "block"} or {@code "unblock"}. */
    public static final String BLOCK_DTMF        = "block_dtmf";
    /** Detect a tone and run a command. Arg: tone spec + command. */
    public static final String TONE_DETECT       = "tone_detect";
    /** Stop tone detection. */
    public static final String STOP_TONE_DETECT  = "stop_tone_detect";

    // ── Speech recognition ────────────────────────────────────────────────────

    /** Start speech recognition. Arg: {@code "engine grammar"} or {@code "stop"}. */
    public static final String DETECT_SPEECH     = "detect_speech";

    // ── Waiting / silence ─────────────────────────────────────────────────────

    /** Pause for N milliseconds. Arg: milliseconds. */
    public static final String SLEEP             = "sleep";
    /** Wait until silence detected. Arg: {@code "<threshold> <seconds> [<hits>]"}. */
    public static final String WAIT_FOR_SILENCE  = "wait_for_silence";
    /** Pause until call is answered. Arg: optional timeout in milliseconds. */
    public static final String WAIT_FOR_ANSWER   = "wait_for_answer";

    // ── Conference / Queue ────────────────────────────────────────────────────

    /** Enter a conference room. Arg: {@code "<room>@<profile>[+flags]"}. */
    public static final String CONFERENCE        = "conference";
    /** Enter a FIFO queue. Arg: {@code "<fifo_name>[@<domain>] [in|out] [<anouce_sound>]"}. */
    public static final String FIFO              = "fifo";
    /** Join an ACD call-center queue. */
    public static final String CALLCENTER        = "callcenter";

    // ── Interception ─────────────────────────────────────────────────────────

    /** Intercept a call by UUID. Arg: UUID of channel to intercept. */
    public static final String INTERCEPT         = "intercept";
    /** Eavesdrop on a channel. Arg: UUID of channel. */
    public static final String EAVESDROP         = "eavesdrop";
    /** Loopback to self (create a loop for testing). Arg: dial string. */
    public static final String LOOPBACK          = "loop_back";

    // ── Echo / test ───────────────────────────────────────────────────────────

    /** Echo audio and video back to originator. No args. */
    public static final String ECHO             = "echo";

    // ── Channel variables ─────────────────────────────────────────────────────

    /** Set a channel variable. Arg: {@code "name=value"}. */
    public static final String SET              = "set";
    /** Set multiple channel variables. Arg: {@code "name=value name2=value2 ..."}. */
    public static final String MULTISET         = "multiset";
    /**
     * Export a variable to the B-leg of a bridge. Arg: {@code "name=value"} or
     * {@code "nolocal:name=value"} to set only on B-leg.
     */
    public static final String EXPORT           = "export";
    /** Unset a channel variable. Arg: variable name. */
    public static final String UNSET            = "unset";

    // ── Audio level ───────────────────────────────────────────────────────────

    /** Adjust read or write audio level. Arg: {@code "<read|write> <level>"}. */
    public static final String SET_AUDIO_LEVEL  = "set_audio_level";

    // ── Media ─────────────────────────────────────────────────────────────────

    /** Reset all bypass/proxy media flags. */
    public static final String MEDIA_RESET      = "media_reset";

    // ── Scheduled actions ─────────────────────────────────────────────────────

    /** Schedule a hangup. Arg: {@code "<time> [<cause>]"}. */
    public static final String SCHED_HANGUP     = "sched_hangup";
    /** Schedule a transfer. Arg: {@code "<time> <dest> [<dialplan>] [<context>]"}. */
    public static final String SCHED_TRANSFER   = "sched_transfer";
    /** Schedule a broadcast. Arg: {@code "<time> <path> [<leg>]"}. */
    public static final String SCHED_BROADCAST  = "sched_broadcast";
    /** Cancel a scheduled action. Arg: task ID or UUID. */
    public static final String SCHED_CANCEL     = "sched_cancel";

    // ── Meta / DTMF binding ───────────────────────────────────────────────────

    /**
     * Execute an application when a DTMF sequence is received during a bridge.
     * Arg: {@code "<dtmf_key> [a|b|ab] [<application>] [<args>]"}.
     */
    public static final String BIND_META_APP    = "bind_meta_app";
    /** Unbind a previously bound meta-app key. Arg: DTMF key. */
    public static final String UNBIND_META_APP  = "unbind_meta_app";

    // ── Logging / debugging ───────────────────────────────────────────────────

    /** Log a message. Arg: {@code "level message"}. */
    public static final String LOG              = "log";
    /** Log call info. No args. */
    public static final String INFO             = "info";
    /** Make all events verbose for this channel. */
    public static final String VERBOSE_EVENTS   = "verbose_events";
    /** Override the system log level for this channel. Arg: level name. */
    public static final String SESSION_LOGLEVEL = "session_loglevel";

    private DialplanApp() {}
}
