package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a channel is fully destroyed and all resources are released.
 * Event-Name: {@code CHANNEL_DESTROY}
 */
public final class ChannelDestroyEvent extends ChannelEvent {

    public ChannelDestroyEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }
}
