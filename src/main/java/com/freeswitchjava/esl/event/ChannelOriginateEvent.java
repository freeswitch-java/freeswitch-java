package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when an outbound call leg is originated.
 * Event-Name: {@code CHANNEL_ORIGINATE}
 */
public final class ChannelOriginateEvent extends ChannelEvent {

    public ChannelOriginateEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** The originate string used to create the call. */
    public String getOriginateString() {
        return getHeader("variable_originate_string");
    }
}
