package com.freeswitchjava.esl.outbound;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Typed view of the initial channel data received when FreeSWITCH connects to the outbound server.
 *
 * <p>Available via {@link OutboundSession#getRequest()} after {@link OutboundSession#connect()} completes.
 * Provides named getters for all standard FreeSWITCH channel variables so you never have to
 * remember raw header strings.
 *
 * <pre>{@code
 * session.connect().join();
 * OutboundSessionRequest req = session.getRequest();
 *
 * String from    = req.getCallerIdNumber();
 * String to      = req.getDestinationNumber();
 * String context = req.getContext();
 * String dir     = req.getCallDirection();  // "inbound" / "outbound"
 * }</pre>
 */
public final class OutboundSessionRequest {

    private final EslEvent event;

    OutboundSessionRequest(EslEvent event) {
        this.event = event;
    }

    // ── Identity ──────────────────────────────────────────────────────────────

    public String getUniqueId()            { return event.getUniqueId(); }
    public String getChannelName()         { return event.getChannelName(); }
    public String getCallDirection()       { return event.getCallDirection(); }

    // ── Caller / Callee ───────────────────────────────────────────────────────

    public String getCallerIdName()        { return event.getCallerIdName(); }
    public String getCallerIdNumber()      { return event.getCallerIdNumber(); }
    public String getDestinationNumber()   { return event.getDestinationNumber(); }
    public String getNetworkAddr()         { return event.getNetworkAddr(); }

    // ── Dialplan context ──────────────────────────────────────────────────────

    public String getContext()             { return event.getContext(); }
    public String getDialplan()            { return event.getHeader("Channel-Dialplan"); }

    // ── Channel state ─────────────────────────────────────────────────────────

    public String getChannelState()        { return event.getChannelState(); }
    public String getAnswerState()         { return event.getAnswerState(); }

    // ── SIP-specific ──────────────────────────────────────────────────────────

    public String getSipFromUser()         { return event.getHeader("variable_sip_from_user"); }
    public String getSipToUser()           { return event.getHeader("variable_sip_to_user"); }
    public String getSipCallId()           { return event.getHeader("variable_sip_call_id"); }
    public String getSipRemoteIp()         { return event.getHeader("variable_sip_network_ip"); }
    public String getSipUserAgent()        { return event.getHeader("variable_sip_user_agent"); }
    public String getSipProfile()          { return event.getHeader("variable_sofia_profile_name"); }

    // ── Channel variables ─────────────────────────────────────────────────────

    /**
     * Returns any channel variable by name.
     * For variables set in dialplan use the {@code variable_} prefix is included automatically
     * by FreeSWITCH in the connect event.
     *
     * @param name raw header name, e.g. {@code "variable_my_custom_var"} or {@code "Channel-State"}
     */
    public String getVar(String name)      { return event.getHeader(name); }

    /** Returns all headers from the initial connect event. */
    public Map<String, String> getHeaders() { return event.getHeaders(); }

    /** Returns the underlying raw event for advanced access. */
    public EslEvent getRawEvent()           { return event; }

    @Override
    public String toString() {
        return "OutboundSessionRequest{uuid=" + getUniqueId()
                + ", from=" + getCallerIdNumber()
                + ", to=" + getDestinationNumber()
                + ", context=" + getContext() + "}";
    }
}
