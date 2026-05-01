package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a parked channel is retrieved.
 * Event-Name: {@code CHANNEL_UNPARK}
 */
public final class ChannelUnparkEvent extends ChannelEvent {

    public ChannelUnparkEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }
}
