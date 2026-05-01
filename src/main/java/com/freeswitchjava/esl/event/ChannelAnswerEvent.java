package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a channel is answered (200 OK sent or received).
 * Event-Name: {@code CHANNEL_ANSWER}
 */
public final class ChannelAnswerEvent extends ChannelEvent {

    public ChannelAnswerEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** Wall-clock time when the call was answered (local server time string). */
    public String getAnswerTime() {
        return getHeader("caller-channel-answered-time");
    }
}
