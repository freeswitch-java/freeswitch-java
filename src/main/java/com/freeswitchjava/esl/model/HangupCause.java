package com.freeswitchjava.esl.model;

/**
 * FreeSWITCH hangup cause codes.
 *
 * <p>Combines Q.850 standard codes (0–127) with FreeSWITCH-internal codes (>127).
 * The {@link #q850Code()} method returns the numeric code; {@link #fromName(String)}
 * and {@link #fromCode(int)} provide reverse lookups.
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Troubleshooting-Debugging/Hangup-Cause-Code-Table_3964945/">
 *     FreeSWITCH Hangup Cause Code Table</a>
 */
public enum HangupCause {

    // ── Q.850 Standard Codes ──────────────────────────────────────────────────

    UNSPECIFIED(0, "No applicable cause codes"),
    UNALLOCATED_NUMBER(1, "Called number not currently assigned"),
    NO_ROUTE_TRANSIT_NET(2, "Unknown transit network"),
    NO_ROUTE_DESTINATION(3, "Network doesn't serve destination"),
    CHANNEL_UNACCEPTABLE(6, "Channel not acceptable"),
    CALL_AWARDED_DELIVERED(7, "Call awarded on established channel"),
    NORMAL_CLEARING(16, "Normal call clearing"),
    USER_BUSY(17, "Called party unavailable (busy)"),
    NO_USER_RESPONSE(18, "No response within prescribed time"),
    NO_ANSWER(19, "Alerted but no connect indication"),
    SUBSCRIBER_ABSENT(20, "Mobile station logged off or unavailable"),
    CALL_REJECTED(21, "Equipment rejecting call"),
    NUMBER_CHANGED(22, "Called number reassigned"),
    REDIRECTION_TO_NEW_DESTINATION(23, "Call redirected elsewhere"),
    EXCHANGE_ROUTING_ERROR(25, "Hop counter limit reached"),
    DESTINATION_OUT_OF_ORDER(27, "Destination interface nonfunctional"),
    INVALID_NUMBER_FORMAT(28, "Number format incomplete or invalid"),
    FACILITY_REJECTED(29, "Service request unavailable"),
    RESPONSE_TO_STATUS_ENQUIRY(30, "Status inquiry response"),
    NORMAL_UNSPECIFIED(31, "Normal event, other class"),
    NORMAL_CIRCUIT_CONGESTION(34, "No available circuit or channel"),
    NETWORK_OUT_OF_ORDER(38, "Network dysfunction likely long-term"),
    NORMAL_TEMPORARY_FAILURE(41, "Temporary network dysfunction"),
    SWITCH_CONGESTION(42, "High traffic experienced"),
    ACCESS_INFO_DISCARDED(43, "Access info not deliverable"),
    REQUESTED_CHAN_UNAVAIL(44, "Requested channel unavailable"),
    PRE_EMPTED(45, "Pre-empted"),
    FACILITY_NOT_SUBSCRIBED(50, "Service available but unauthorized"),
    OUTGOING_CALL_BARRED(52, "Outgoing calls restricted"),
    INCOMING_CALL_BARRED(54, "Incoming calls restricted"),
    BEARERCAPABILITY_NOTAUTH(57, "Bearer capability unauthorized"),
    BEARERCAPABILITY_NOTAVAIL(58, "Bearer capability unavailable now"),
    SERVICE_UNAVAILABLE(63, "Service or option not available"),
    BEARERCAPABILITY_NOTIMPL(65, "Bearer capability unsupported"),
    CHAN_NOT_IMPLEMENTED(66, "Channel type unsupported"),
    FACILITY_NOT_IMPLEMENTED(69, "Supplementary services unsupported"),
    SERVICE_NOT_IMPLEMENTED(79, "Service or option not implemented"),
    INVALID_CALL_REFERENCE(81, "Call reference not in use"),
    INCOMPATIBLE_DESTINATION(88, "Incompatible compatibility attributes"),
    INVALID_MSG_UNSPECIFIED(95, "Invalid message, other class"),
    MANDATORY_IE_MISSING(96, "Required info element absent"),
    MESSAGE_TYPE_NONEXIST(97, "Message type undefined or unimplemented"),
    WRONG_MESSAGE(98, "Message incompatible with call state"),
    IE_NONEXIST(99, "Info element or parameter undefined"),
    INVALID_IE_CONTENTS(100, "Info element content invalid"),
    WRONG_CALL_STATE(101, "Message incompatible with call state"),
    RECOVERY_ON_TIMER_EXPIRE(102, "Timer expiry error recovery"),
    MANDATORY_IE_LENGTH_ERROR(103, "Parameter not recognized"),
    PROTOCOL_ERROR(111, "Protocol error, other class"),
    INTERWORKING(127, "Interworking call ended"),

    // ── FreeSWITCH-Internal Codes ─────────────────────────────────────────────

    ORIGINATOR_CANCEL(487, "Originating party cancelled"),
    CRASH(500, "System crash"),
    SYSTEM_SHUTDOWN(501, "System shutdown"),
    LOSE_RACE(502, "Race condition lost"),
    MANAGER_REQUEST(503, "API command termination (e.g. uuid_kill)"),
    BLIND_TRANSFER(600, "Blind transfer executed"),
    ATTENDED_TRANSFER(601, "Attended transfer executed"),
    ALLOTTED_TIMEOUT(602, "Destination channel timeout"),
    USER_CHALLENGE(603, "User challenge issued"),
    MEDIA_TIMEOUT(604, "Media stream timeout"),
    PICKED_OFF(605, "Call intercepted from another extension"),
    USER_NOT_REGISTERED(606, "SIP user unregistered"),
    PROGRESS_TIMEOUT(607, "Progress timeout occurred"),
    GATEWAY_DOWN(609, "Gateway unresponsive");

    private final int code;
    private final String description;

    HangupCause(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /** Q.850 (or FreeSWITCH internal) numeric code. */
    public int q850Code() {
        return code;
    }

    public String description() {
        return description;
    }

    /**
     * Looks up a cause by its string name (case-insensitive).
     *
     * @return the matching cause, or {@link #UNSPECIFIED} if not found
     */
    public static HangupCause fromName(String name) {
        if (name == null) return UNSPECIFIED;
        for (HangupCause c : values()) {
            if (c.name().equalsIgnoreCase(name.trim())) return c;
        }
        return UNSPECIFIED;
    }

    /**
     * Looks up a cause by its numeric code.
     *
     * @return the matching cause, or {@link #UNSPECIFIED} if not found
     */
    public static HangupCause fromCode(int code) {
        for (HangupCause c : values()) {
            if (c.code == code) return c;
        }
        return UNSPECIFIED;
    }
}
