package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a channel is taken off hold.
 * Event-Name: {@code CHANNEL_UNHOLD}
 */
public final class ChannelUnholdEvent extends ChannelEvent {

    public ChannelUnholdEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }
}
