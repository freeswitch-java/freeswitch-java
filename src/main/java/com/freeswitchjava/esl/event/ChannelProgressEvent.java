package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a call generates a 183 Session Progress (ringing signal without audio).
 * Event-Name: {@code CHANNEL_PROGRESS}
 */
public final class ChannelProgressEvent extends ChannelEvent {

    public ChannelProgressEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }
}
