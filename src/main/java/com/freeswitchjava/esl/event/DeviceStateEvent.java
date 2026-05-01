package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired when a device (e.g. SIP phone) changes presence/state.
 * Event-Name: {@code DEVICE_STATE}
 */
public final class DeviceStateEvent extends EslEvent {

    public DeviceStateEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** Device identifier (e.g. SIP AOR). */
    public String getDeviceId() {
        return getHeader("device-id");
    }

    /** New device state string, e.g. {@code "ACTIVE"}, {@code "IDLE"}. */
    public String getDeviceState() {
        return getHeader("device-state");
    }
}
