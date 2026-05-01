package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired on every channel state machine transition.
 * Event-Name: {@code CHANNEL_STATE}
 *
 * <p>These events are very frequent. Use {@link ChannelAnswerEvent}, {@link ChannelHangupEvent},
 * etc. for specific lifecycle moments.
 */
public final class ChannelStateEvent extends ChannelEvent {

    public ChannelStateEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** New state the channel transitioned to, e.g. {@code CS_EXECUTE}. */
    public String getNewState() {
        return getChannelState();
    }
}
