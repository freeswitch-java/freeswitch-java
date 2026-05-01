package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Base class for all channel-related ESL events.
 * Provides getters for headers present on most channel events.
 */
public class ChannelEvent extends EslEvent {

    public ChannelEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** SIP/channel state machine state, e.g. {@code CS_EXECUTE}. */
    public String getChannelStateName() {
        return getHeader("channel-state");
    }

    /** Application last executed on this channel. */
    public String getApplication() {
        return getHeader("application");
    }

    /** Arguments passed to the last application. */
    public String getApplicationData() {
        return getHeader("application-data");
    }

    /** Indicates if the channel is ready for media. */
    public boolean isAnswered() {
        return "answered".equalsIgnoreCase(getAnswerState());
    }

    /** Presence ID (SIP user@domain). */
    public String getPresenceId() {
        return getHeader("channel-presence-id");
    }

    /** The profile name used by mod_sofia. */
    public String getSofiaProfile() {
        return getHeader("variable_sofia_profile_name");
    }

    /** SIP call-ID header. */
    public String getSipCallId() {
        return getHeader("variable_sip_call_id");
    }

    /** The leg type: {@code "A"} for originating, {@code "B"} for answered. */
    public String getLegType() {
        return getHeader("leg-type");
    }
}
