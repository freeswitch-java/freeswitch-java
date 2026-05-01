package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired when a SIP endpoint unregisters or its registration expires.
 * Event-Name: {@code UNREGISTER}
 */
public final class UnregisterEvent extends EslEvent {

    public UnregisterEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** SIP username that unregistered. */
    public String getSipUser() {
        return getHeader("from-user");
    }

    /** SIP domain. */
    public String getSipHost() {
        return getHeader("from-host");
    }

    /** Profile the registration was on. */
    public String getProfile() {
        return getHeader("profile-name");
    }

    /** Reason for unregistration. */
    public String getReason() {
        return getHeader("reason");
    }
}
