package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a channel is placed on hold.
 * Event-Name: {@code CHANNEL_HOLD}
 */
public final class ChannelHoldEvent extends ChannelEvent {

    public ChannelHoldEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }
}
