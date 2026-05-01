package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when an outbound channel is dialling out.
 * Event-Name: {@code CHANNEL_OUTGOING}
 */
public final class ChannelOutgoingEvent extends ChannelEvent {

    public ChannelOutgoingEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }
}
