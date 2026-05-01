package com.freeswitchjava.esl.dialplan;

/**
 * Application name constants for all {@code mod_dptools} dialplan applications.
 *
 * <p>Use with {@link com.freeswitchjava.esl.command.SendMsg#execute(String)} and
 * {@link com.freeswitchjava.esl.outbound.OutboundSession#execute(String, String)}.
 * For complex applications with many parameters, prefer the typed builder classes
 * in {@code com.freeswitchjava.esl.dialplan.app}.
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Modules/mod_dptools_1970333/">
 *     mod_dptools documentation</a>
 */
public final class DpTools {

    // ── Call control ─────────────────────────────────────────────────────────

    /** Answer the call. */
    public static final String ANSWER               = "answer";
    /** Answer in early-media mode (183 Session Progress). */
    public static final String PRE_ANSWER           = "pre_answer";
    /** Send 200 OK in early media without bridging. */
    public static final String EARLY_OK             = "early_ok";
    /** Indicate ringing (180) without answering. */
    public static final String RING_READY           = "ring_ready";
    /** Hangup the channel. Arg: optional cause name. */
    public static final String HANGUP               = "hangup";
    /** Redirect (SIP 3xx). Arg: SIP URI. */
    public static final String REDIRECT             = "redirect";
    /** Send SIP REFER. Arg: SIP URI. */
    public static final String DEFLECT              = "deflect";
    /** Attended transfer. Arg: UUID of channel to transfer to. */
    public static final String ATT_XFER             = "att_xfer";
    /** Cancel the currently running application on this channel. */
    public static final String BREAK                = "break";
    /** Send early hangup signal. */
    public static final String EARLY_HANGUP         = "early_hangup";
    /** Send a SIP respond message. Arg: response code + optional reason. */
    public static final String RESPOND              = "respond";

    // ── Routing ──────────────────────────────────────────────────────────────

    /** Bridge to another endpoint. Arg: dial string. */
    public static final String BRIDGE               = "bridge";
    /** Export a variable across any bridge. Arg: {@code "name=value"}. */
    public static final String BRIDGE_EXPORT        = "bridge_export";
    /** Transfer to an extension. Arg: {@code "dest [dialplan] [context]"}. */
    public static final String TRANSFER             = "transfer";
    /** Execute another extension and return. Arg: {@code "dest [dialplan] [context]"}. */
    public static final String EXECUTE_EXTENSION    = "execute_extension";
    /** Loopback (create a synthetic call leg). Arg: dial string. */
    public static final String LOOPBACK             = "loopback";
    /** Perform E.164 ENUM lookup. Arg: ENUM suffix. */
    public static final String ENUM                 = "enum";
    /** Number translation via mod_translate. */
    public static final String TRANSLATE            = "translate";

    // ── Hold / Park ───────────────────────────────────────────────────────────

    /** Park the call. */
    public static final String PARK                 = "park";
    /** Park state. */
    public static final String PARK_STATE           = "park_state";
    /** Put channel on hold. Arg: optional MOH path. */
    public static final String HOLD                 = "hold";
    /** Take the channel off hold. */
    public static final String UNHOLD               = "unhold";
    /** Put a bridged channel on hold. Arg: optional MOH path. */
    public static final String SOFT_HOLD            = "soft_hold";
    /** Pickup a parked call. Arg: pickup group or UUID. */
    public static final String PICKUP               = "pickup";

    // ── Audio playback ───────────────────────────────────────────────────────

    /** Play an audio file. Arg: path. */
    public static final String PLAYBACK             = "playback";
    /** Loop a file continuously. Arg: path. */
    public static final String ENDLESS_PLAYBACK     = "endless_playback";
    /** Loop a file a limited number of times. Arg: {@code "path [loop_count]"}. */
    public static final String LOOP_PLAYBACK        = "loop_playback";
    /** Speak with TTS. Arg: {@code "engine|voice|text"}. */
    public static final String SPEAK                = "speak";
    /** Say a phrase using pre-recorded prompts. Arg: {@code "module type [method] value"}. */
    public static final String SAY                  = "say";
    /** Say a named phrase. Arg: {@code "phrase_name[,args]"}. */
    public static final String PHRASE               = "phrase";
    /** Generate TGML tones. Arg: TGML string. */
    public static final String GENTONES             = "gentones";
    /** Play file while doing ASR. Arg: {@code "file detect:engine {params}grammar"}. */
    public static final String PLAY_AND_DETECT_SPEECH = "play_and_detect_speech";
    /** Play file and collect DTMF. See {@code PlayAndGetDigitsApp} builder. */
    public static final String PLAY_AND_GET_DIGITS  = "play_and_get_digits";
    /** Read DTMF digits into a channel variable. See {@code ReadApp} builder. */
    public static final String READ                 = "read";
    /** Play audio page to a list of channels. Arg: dial string. */
    public static final String PAGE                 = "page";
    /** Play an FSV (FS Video) file. Arg: path. */
    public static final String PLAY_FSV             = "play_fsv";
    /** Delayed audio echo loopback. Arg: delay in milliseconds. */
    public static final String DELAY_ECHO           = "delay_echo";

    // ── Recording ────────────────────────────────────────────────────────────

    /** Record from channel input. See {@code RecordApp} builder. */
    public static final String RECORD               = "record";
    /** Record the entire session (both legs). Arg: path. */
    public static final String RECORD_SESSION       = "record_session";
    /** Stop session recording. Arg: path. */
    public static final String STOP_RECORD_SESSION  = "stop_record_session";
    /** Record an FSV video file. Arg: path. */
    public static final String RECORD_FSV           = "record_fsv";

    // ── Audio displacement ────────────────────────────────────────────────────

    /** Overlay audio on a channel. Arg: {@code "file [flags] [limit]"}. */
    public static final String DISPLACE_SESSION     = "displace_session";
    /** Stop audio displacement. Arg: file path. */
    public static final String STOP_DISPLACE_SESSION = "stop_displace_session";

    // ── DTMF ─────────────────────────────────────────────────────────────────

    /** Queue DTMF digits to send after bridge. Arg: digits. */
    public static final String QUEUE_DTMF           = "queue_dtmf";
    /** Send DTMF (inband / RFC 2833 / SIP Info). Arg: digits. */
    public static final String SEND_DTMF            = "send_dtmf";
    /** Flush any queued DTMF digits. */
    public static final String FLUSH_DTMF           = "flush_dtmf";
    /** Start inband DTMF detection. */
    public static final String START_DTMF           = "start_dtmf";
    /** Stop inband DTMF detection. */
    public static final String STOP_DTMF            = "stop_dtmf";
    /** Start inband DTMF generation. */
    public static final String START_DTMF_GENERATE  = "start_dtmf_generate";
    /** Stop inband DTMF generation. */
    public static final String STOP_DTMF_GENERATE   = "stop_dtmf_generate";
    /** Block DTMF. Arg: {@code "block"} or {@code "unblock"}. */
    public static final String BLOCK_DTMF           = "block_dtmf";
    /** Detect a tone and run a command. Arg: tone spec + command. */
    public static final String TONE_DETECT          = "tone_detect";
    /** Stop tone detection. */
    public static final String STOP_TONE_DETECT     = "stop_tone_detect";

    // ── Digit binding ─────────────────────────────────────────────────────────

    /**
     * Bind a DTMF key sequence or regex to an action.
     * Arg: {@code "realm key sequence|regex action"}.
     */
    public static final String BIND_DIGIT_ACTION    = "bind_digit_action";
    /** Clear all digit bindings. Arg: optional realm. */
    public static final String CLEAR_DIGIT_ACTION   = "clear_digit_action";
    /** Change digit binding realm. Arg: realm name. */
    public static final String DIGIT_ACTION_SET_REALM = "digit_action_set_realm";
    /**
     * Bind a DTMF key to execute an app during a bridge.
     * Arg: {@code "key [a|b|ab] app [args]"}.
     */
    public static final String BIND_META_APP        = "bind_meta_app";
    /** Unbind a meta-app key. Arg: DTMF key. */
    public static final String UNBIND_META_APP      = "unbind_meta_app";
    /** Capture digit input into a channel variable array. Arg: {@code "varname regex"}. */
    public static final String CAPTURE              = "capture";

    // ── Speech recognition ────────────────────────────────────────────────────

    /** Start/stop speech recognition. Arg: {@code "engine grammar"} or {@code "stop"}. */
    public static final String DETECT_SPEECH        = "detect_speech";
    /** Clear ASR speech handle cache. */
    public static final String CLEAR_SPEECH_CACHE   = "clear_speech_cache";

    // ── Waiting / silence ─────────────────────────────────────────────────────

    /** Pause for N milliseconds. Arg: milliseconds. */
    public static final String SLEEP                = "sleep";
    /** Wait for silence. Arg: {@code "threshold seconds [hits]"}. */
    public static final String WAIT_FOR_SILENCE     = "wait_for_silence";
    /** Pause until the call is answered. Arg: optional timeout in milliseconds. */
    public static final String WAIT_FOR_ANSWER      = "wait_for_answer";

    // ── Conference / Queue ────────────────────────────────────────────────────

    /** Enter a conference room. See {@code ConferenceApp} builder. */
    public static final String CONFERENCE           = "conference";
    /** Enter a FIFO queue. Arg: {@code "fifo_name [in|out] [announce]"}. */
    public static final String FIFO                 = "fifo";
    /** Count this call in the FIFO manual_calls queue. */
    public static final String FIFO_TRACK_CALL      = "fifo_track_call";
    /** Route caller into an ACD callcenter queue. */
    public static final String CALLCENTER           = "callcenter";
    /** Three-way call with a UUID. Arg: UUID. */
    public static final String THREE_WAY            = "three_way";

    // ── Interception / monitoring ─────────────────────────────────────────────

    /** Intercept a call by UUID. Arg: UUID. */
    public static final String INTERCEPT            = "intercept";
    /** Eavesdrop on a channel. Arg: UUID. */
    public static final String EAVESDROP            = "eavesdrop";
    /** Persistent eavesdrop on all channels bridged to a user. Arg: user. */
    public static final String USERSPY              = "userspy";

    // ── Echo / test ───────────────────────────────────────────────────────────

    /** Echo audio and video back to the originator. */
    public static final String ECHO                 = "echo";
    /** Analyze audio and report sound levels. */
    public static final String SOUND_TEST           = "sound_test";

    // ── Channel variables ─────────────────────────────────────────────────────

    /** Set a channel variable. Arg: {@code "name=value"}. */
    public static final String SET                  = "set";
    /** Set multiple channel variables. Arg: {@code "name=value name2=value2 ..."}. */
    public static final String MULTISET             = "multiset";
    /** Export a variable to the B-leg. Arg: {@code "[nolocal:]name=value"}. */
    public static final String EXPORT               = "export";
    /** Unset a channel variable. Arg: variable name. */
    public static final String UNSET                = "unset";
    /** Set a global variable. Arg: {@code "name=value"}. */
    public static final String SET_GLOBAL           = "set_global";
    /** Set the channel name. Arg: name string. */
    public static final String SET_NAME             = "set_name";
    /** Set a caller profile variable. Arg: {@code "name value"}. */
    public static final String SET_PROFILE_VAR      = "set_profile_var";
    /** Set user parameters from directory. Arg: {@code "user@domain"}. */
    public static final String SET_USER             = "set_user";
    /** Set zombie execution flag. */
    public static final String SET_ZOMBIE_EXEC      = "set_zombie_exec";
    /** Evaluate a string/variable expression. Arg: expression. */
    public static final String EVAL                 = "eval";
    /** Perform a regex match. Arg: {@code "subject /regex/ [replacement]"}. */
    public static final String REGEX                = "regex";

    // ── Audio level ───────────────────────────────────────────────────────────

    /** Adjust read or write audio level. Arg: {@code "read|write level"}. */
    public static final String SET_AUDIO_LEVEL      = "set_audio_level";

    // ── Media ─────────────────────────────────────────────────────────────────

    /** Reset all bypass/proxy media flags. */
    public static final String MEDIA_RESET          = "media_reset";
    /** Remove media bugs from the channel. */
    public static final String REMOVE_BUGS          = "remove_bugs";
    /** Enable media heartbeat. Arg: interval in milliseconds. */
    public static final String ENABLE_HEARTBEAT     = "enable_heartbeat";
    /** Detect FAX CNG tone. */
    public static final String FAX_DETECT           = "fax_detect";
    /** Receive a fax as a TIFF file. Arg: output path. */
    public static final String RXFAX                = "rxfax";
    /** Set jitter buffer size. Arg: milliseconds (0 = disable). */
    public static final String JITTERBUFFER         = "jitterbuffer";
    /** Packet loss concealment + comfort noise generation. */
    public static final String CNG_PLC              = "cng_plc";

    // ── Scheduled actions ─────────────────────────────────────────────────────

    /** Schedule a hangup. Arg: {@code "time [cause]"}. */
    public static final String SCHED_HANGUP         = "sched_hangup";
    /** Schedule a transfer. Arg: {@code "time dest [dialplan] [context]"}. */
    public static final String SCHED_TRANSFER       = "sched_transfer";
    /** Schedule a broadcast. Arg: {@code "time path [leg]"}. */
    public static final String SCHED_BROADCAST      = "sched_broadcast";
    /** Schedule a heartbeat. Arg: interval in seconds. */
    public static final String SCHED_HEARTBEAT      = "sched_heartbeat";
    /** Cancel a scheduled action. Arg: task ID or UUID. */
    public static final String SCHED_CANCEL         = "sched_cancel";

    // ── SIP messaging ─────────────────────────────────────────────────────────

    /** Send a text message to an IM/SIP client. Arg: {@code "to text"}. */
    public static final String CHAT                 = "chat";
    /** Send SIP INFO packet. */
    public static final String SEND_INFO            = "send_info";
    /** Send SIP INFO with sipfrag (display update). Arg: display text. */
    public static final String SEND_DISPLAY         = "send_display";
    /** Send presence notification. Arg: {@code "rpid status [message]"}. */
    public static final String PRESENCE             = "presence";
    /** Set caller privacy. Arg: privacy type. */
    public static final String PRIVACY              = "privacy";

    // ── ACL ───────────────────────────────────────────────────────────────────

    /** Check originating address against an ACL. Arg: {@code "list_name [pass_dialplan]"}. */
    public static final String CHECK_ACL            = "check_acl";

    // ── Resource limits ───────────────────────────────────────────────────────

    /** Set a limit on concurrent calls. See {@code LimitApp} builder. */
    public static final String LIMIT                = "limit";
    /** Set a limit and execute an app if exceeded. Arg: {@code "backend realm resource max app [args]"}. */
    public static final String LIMIT_EXECUTE        = "limit_execute";
    /** Hash-based limit. Arg: same as LIMIT. */
    public static final String LIMIT_HASH           = "limit_hash";
    /** Hash-based limit + execute. Arg: same as LIMIT_EXECUTE. */
    public static final String LIMIT_HASH_EXECUTE   = "limit_hash_execute";

    // ── Database ─────────────────────────────────────────────────────────────

    /** Insert/query the FreeSWITCH internal database. Arg: {@code "op db_name key [value]"}. */
    public static final String DB                   = "db";
    /** Add/remove from hash. Arg: {@code "insert|delete|select realm key [value]"}. */
    public static final String HASH                 = "hash";
    /** Insert/delete group members. Arg: {@code "insert|delete groupname:extension@domain"}. */
    public static final String GROUP                = "group";

    // ── IVR / dialplan flow ───────────────────────────────────────────────────

    /** Run an IVR menu. Arg: menu name. */
    public static final String IVR                  = "ivr";
    /** Outbound socket connection. Arg: {@code "host:port [async] [full]"}. */
    public static final String SOCKET               = "socket";
    /** Route via HTTAPI web server. Arg: URL. */
    public static final String HTTAPI               = "httapi";
    /** Block call flow, allowing only one at a time. Arg: mutex name. */
    public static final String MUTEX               = "mutex";
    /** Fire a custom event. Arg: {@code "Event-Name [header=value ...]"}. */
    public static final String EVENT                = "event";

    // ── System ────────────────────────────────────────────────────────────────

    /** Execute an OS command (blocking). Arg: shell command. */
    public static final String SYSTEM               = "system";
    /** Execute an OS command in background. Arg: shell command. */
    public static final String BGSYSTEM             = "bgsystem";
    /** Create a directory. Arg: path. */
    public static final String MKDIR                = "mkdir";
    /** Rename/move a file. Arg: {@code "source dest"}. */
    public static final String RENAME               = "rename";

    // ── Logging / debugging ───────────────────────────────────────────────────

    /** Log a message. Arg: {@code "level message"}. */
    public static final String LOG                  = "log";
    /** Log call info to the console. */
    public static final String INFO                 = "info";
    /** Make all events verbose for this channel. */
    public static final String VERBOSE_EVENTS       = "verbose_events";
    /** Override log level for this channel. Arg: level name. */
    public static final String SESSION_LOGLEVEL     = "session_loglevel";

    private DpTools() {}
}
