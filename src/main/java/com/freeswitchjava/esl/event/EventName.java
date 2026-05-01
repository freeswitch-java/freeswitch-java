package com.freeswitchjava.esl.event;

/**
 * Complete set of FreeSWITCH core event names (Event-Name header values).
 *
 * <p>Use with {@code client.subscribe(EventName.CHANNEL_ANSWER)} and
 * {@code client.addEventListener(EventName.CHANNEL_HANGUP, listener)}.
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Introduction/Event-System/Event-List_7143557/">
 *     FreeSWITCH Event List</a>
 */
public enum EventName {

    // ── Scheduler ─────────────────────────────────────────────────────────────
    ADD_SCHEDULE,
    DEL_SCHEDULE,
    EXE_SCHEDULE,
    RE_SCHEDULE,

    // ── API ───────────────────────────────────────────────────────────────────
    API,

    // ── Background jobs ───────────────────────────────────────────────────────
    BACKGROUND_JOB,

    // ── Call detail / CDR ─────────────────────────────────────────────────────
    CALL_DETAIL,
    CDR,

    // ── Call control ──────────────────────────────────────────────────────────
    CALL_SECURE,
    CALL_SETUP_REQ,
    CALL_UPDATE,

    // ── Channel lifecycle ─────────────────────────────────────────────────────
    CHANNEL_ANSWER,
    CHANNEL_APPLICATION,
    CHANNEL_BRIDGE,
    CHANNEL_CALLSTATE,
    CHANNEL_CREATE,
    CHANNEL_DATA,
    CHANNEL_DESTROY,
    CHANNEL_EXECUTE,
    CHANNEL_EXECUTE_COMPLETE,
    CHANNEL_GLOBAL,
    CHANNEL_HANGUP,
    CHANNEL_HANGUP_COMPLETE,
    CHANNEL_HOLD,
    CHANNEL_ORIGINATE,
    CHANNEL_OUTGOING,
    CHANNEL_PARK,
    CHANNEL_PROGRESS,
    CHANNEL_PROGRESS_MEDIA,
    CHANNEL_STATE,
    CHANNEL_UNBRIDGE,
    CHANNEL_UNHOLD,
    CHANNEL_UNPARK,
    CHANNEL_UUID,

    // ── Clone ─────────────────────────────────────────────────────────────────
    CLONE,

    // ── Codec ─────────────────────────────────────────────────────────────────
    CODEC,

    // ── Command ───────────────────────────────────────────────────────────────
    COMMAND,

    // ── Conference ───────────────────────────────────────────────────────────
    CONFERENCE_DATA,
    CONFERENCE_DATA_QUERY,

    // ── Custom (subclass events) ──────────────────────────────────────────────
    /**
     * Generic class for module/application-specific events.
     * Subscribe with: {@code client.subscribeRaw("CUSTOM myapp::myevent")}
     * Identify subclass via {@code event.getHeader("event-subclass")}.
     */
    CUSTOM,
    SUBCLASS_ANY,

    // ── Speech / Tone detection ───────────────────────────────────────────────
    DETECTED_SPEECH,
    DETECTED_TONE,

    // ── Device state ─────────────────────────────────────────────────────────
    DEVICE_STATE,

    // ── DTMF ─────────────────────────────────────────────────────────────────
    DTMF,

    // ── Failure ───────────────────────────────────────────────────────────────
    FAILURE,

    // ── General ───────────────────────────────────────────────────────────────
    GENERAL,

    // ── Heartbeat ────────────────────────────────────────────────────────────
    HEARTBEAT,
    SESSION_HEARTBEAT,

    // ── Logging ───────────────────────────────────────────────────────────────
    LOG,

    // ── Media bugs ───────────────────────────────────────────────────────────
    MEDIA_BUG_START,
    MEDIA_BUG_STOP,

    // ── Messaging ────────────────────────────────────────────────────────────
    MESSAGE,
    MESSAGE_QUERY,
    MESSAGE_WAITING,
    RECV_MESSAGE,
    SEND_MESSAGE,

    // ── Module lifecycle ──────────────────────────────────────────────────────
    MODULE_LOAD,
    MODULE_UNLOAD,

    // ── NAT ───────────────────────────────────────────────────────────────────
    NAT,

    // ── Talk detection ───────────────────────────────────────────────────────
    NOTALK,
    TALK,

    // ── SIP NOTIFY ───────────────────────────────────────────────────────────
    NOTIFY,
    NOTIFY_IN,

    // ── Phone features ────────────────────────────────────────────────────────
    PHONE_FEATURE,
    PHONE_FEATURE_SUBSCRIBE,

    // ── Playback ─────────────────────────────────────────────────────────────
    PLAYBACK_START,
    PLAYBACK_STOP,

    // ── Presence ─────────────────────────────────────────────────────────────
    PRESENCE_IN,
    PRESENCE_OUT,
    PRESENCE_PROBE,

    // ── Private / internal ────────────────────────────────────────────────────
    PRIVATE_COMMAND,

    // ── Publish / subscribe ───────────────────────────────────────────────────
    PUBLISH,
    UNPUBLISH,

    // ── Queue ─────────────────────────────────────────────────────────────────
    QUEUE_LEN,

    // ── Recording ────────────────────────────────────────────────────────────
    RECORD_START,
    RECORD_STOP,

    // ── SIP INFO ─────────────────────────────────────────────────────────────
    RECV_INFO,
    SEND_INFO,

    // ── RTCP ─────────────────────────────────────────────────────────────────
    RECV_RTCP_MESSAGE,

    // ── Recycle ───────────────────────────────────────────────────────────────
    RECYCLE,

    // ── Config reload ────────────────────────────────────────────────────────
    RELOADXML,

    // ── Request params ───────────────────────────────────────────────────────
    REQUEST_PARAMS,

    // ── Roster ────────────────────────────────────────────────────────────────
    ROSTER,

    // ── System lifecycle ─────────────────────────────────────────────────────
    SHUTDOWN,
    STARTUP,
    SESSION_CRASH,

    // ── Trap ─────────────────────────────────────────────────────────────────
    TRAP,

    // ── Inbound/outbound channel (internal) ───────────────────────────────────
    INBOUND_CHAN,
    OUTBOUND_CHAN,

    // ── Registration ─────────────────────────────────────────────────────────
    REGISTER,
    REGISTER_ATTEMPT,
    UNREGISTER,

    // ── Subscribe to ALL events ───────────────────────────────────────────────
    ALL;

    /**
     * Returns the wire-format name sent to FreeSWITCH (same as {@link #name()}).
     */
    public String wireValue() {
        return name();
    }

    /**
     * Looks up an EventName by its wire-format string, case-insensitive.
     * Returns {@code null} for unknown/custom subclass event names.
     */
    public static EventName fromWire(String value) {
        if (value == null) return null;
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
