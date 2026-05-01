package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when two channel legs are bridged together.
 * Event-Name: {@code CHANNEL_BRIDGE}
 */
public final class ChannelBridgeEvent extends ChannelEvent {

    public ChannelBridgeEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** UUID of the other channel leg that was bridged. */
    public String getOtherLegUniqueId() {
        return getHeader("other-leg-unique-id");
    }

    /** Caller ID name of the other leg. */
    public String getOtherLegCallerIdName() {
        return getHeader("other-leg-caller-id-name");
    }

    /** Caller ID number of the other leg. */
    public String getOtherLegCallerIdNumber() {
        return getHeader("other-leg-caller-id-number");
    }

    /** Destination number of the other leg. */
    public String getOtherLegDestinationNumber() {
        return getHeader("other-leg-destination-number");
    }

    /** Direction of the bridge: {@code inbound} or {@code outbound}. */
    public String getBridgeDirection() {
        return getHeader("bridge-direction");
    }
}
