package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when early media begins (183 with SDP — audio before answer).
 * Event-Name: {@code CHANNEL_PROGRESS_MEDIA}
 */
public final class ChannelProgressMediaEvent extends ChannelEvent {

    public ChannelProgressMediaEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }
}
