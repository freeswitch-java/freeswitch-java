package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when two bridged channel legs are separated.
 * Event-Name: {@code CHANNEL_UNBRIDGE}
 */
public final class ChannelUnbridgeEvent extends ChannelEvent {

    public ChannelUnbridgeEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** UUID of the other channel leg that was previously bridged. */
    public String getOtherLegUniqueId() {
        return getHeader("other-leg-unique-id");
    }

    /** Hangup cause that triggered the unbridge, if applicable. */
    public String getHangupCause() {
        return getHeader("hangup-cause");
    }
}
