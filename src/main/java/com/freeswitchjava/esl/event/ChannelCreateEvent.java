package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a new channel is created (call leg allocated).
 * Event-Name: {@code CHANNEL_CREATE}
 */
public final class ChannelCreateEvent extends ChannelEvent {

    public ChannelCreateEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** The initial channel state, typically {@code CS_NEW}. */
    public String getInitialState() {
        return getChannelState();
    }
}
