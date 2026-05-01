package com.freeswitchjava.esl.codec;

/**
 * Well-known ESL header name constants (normalized to lowercase for consistent lookup).
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Introduction/Event-System/Event-header-values_7143Related/">
 *     FreeSWITCH Event Headers</a>
 */
public final class EslHeaders {

    // ── Outer frame headers ───────────────────────────────────────────────────
    public static final String CONTENT_TYPE   = "content-type";
    public static final String CONTENT_LENGTH = "content-length";
    public static final String REPLY_TEXT     = "reply-text";
    public static final String JOB_UUID       = "job-uuid";

    // ── Content-Type values ───────────────────────────────────────────────────
    public static final String CT_AUTH_REQUEST      = "auth/request";
    public static final String CT_COMMAND_REPLY     = "command/reply";
    public static final String CT_API_RESPONSE      = "api/response";
    public static final String CT_EVENT_PLAIN       = "text/event-plain";
    public static final String CT_EVENT_JSON        = "text/event-json";
    public static final String CT_EVENT_XML         = "text/event-xml";
    public static final String CT_DISCONNECT_NOTICE = "text/disconnect-notice";

    // ── Common event headers (present on ALL events) ──────────────────────────
    public static final String EVENT_NAME              = "event-name";
    public static final String CORE_UUID               = "core-uuid";
    public static final String EVENT_DATE_LOCAL        = "event-date-local";
    public static final String EVENT_DATE_GMT          = "event-date-gmt";
    public static final String EVENT_DATE_TIMESTAMP    = "event-date-timestamp";
    public static final String EVENT_SEQUENCE          = "event-sequence";
    public static final String EVENT_CALLING_FILE      = "event-calling-file";
    public static final String EVENT_CALLING_FUNCTION  = "event-calling-function";
    public static final String EVENT_CALLING_LINE      = "event-calling-line-number";

    // ── FreeSWITCH instance headers (present on ALL events) ───────────────────
    public static final String FREESWITCH_IPV4       = "freeswitch-ipv4";
    public static final String FREESWITCH_IPV6       = "freeswitch-ipv6";
    public static final String FREESWITCH_HOSTNAME   = "freeswitch-hostname";
    public static final String FREESWITCH_SWITCHNAME = "freeswitch-switchname";

    // ── Channel / call headers ────────────────────────────────────────────────
    public static final String UNIQUE_ID              = "unique-id";
    public static final String OTHER_LEG_UNIQUE_ID    = "other-leg-unique-id";
    public static final String CHANNEL_NAME           = "channel-name";
    public static final String CHANNEL_STATE          = "channel-state";
    public static final String ANSWER_STATE           = "answer-state";
    public static final String CALL_DIRECTION         = "call-direction";
    public static final String HANGUP_CAUSE           = "hangup-cause";

    // ── Caller headers ────────────────────────────────────────────────────────
    public static final String CALLER_ID_NAME         = "caller-caller-id-name";
    public static final String CALLER_ID_NUMBER       = "caller-caller-id-number";
    public static final String CALLER_DEST_NUMBER     = "caller-destination-number";
    public static final String CALLER_NETWORK_ADDR    = "caller-network-addr";
    public static final String CALLER_CONTEXT         = "caller-context";

    // ── bgapi / job headers ───────────────────────────────────────────────────
    public static final String VARIABLE_JOB_UUID      = "variable_job_uuid";
    public static final String JOB_COMMAND            = "job-command";
    public static final String JOB_COMMAND_ARG        = "job-command-arg";

    // ── Event subclass (for CUSTOM events) ───────────────────────────────────
    public static final String EVENT_SUBCLASS         = "event-subclass";

    private EslHeaders() {}
}
