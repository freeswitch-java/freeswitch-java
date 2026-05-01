package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a channel is parked.
 * Event-Name: {@code CHANNEL_PARK}
 */
public final class ChannelParkEvent extends ChannelEvent {

    public ChannelParkEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }
}
