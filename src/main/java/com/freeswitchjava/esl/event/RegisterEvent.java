package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired when a SIP endpoint registers with FreeSWITCH.
 * Event-Name: {@code REGISTER}
 */
public final class RegisterEvent extends EslEvent {

    public RegisterEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** SIP username that registered. */
    public String getSipUser() {
        return getHeader("from-user");
    }

    /** SIP domain of the registering endpoint. */
    public String getSipHost() {
        return getHeader("from-host");
    }

    /** Network address of the registering endpoint. */
    public String getNetworkIp() {
        return getHeader("network-ip");
    }

    /** Network port of the registering endpoint. */
    public String getNetworkPort() {
        return getHeader("network-port");
    }

    /** SIP profile the registration arrived on. */
    public String getProfile() {
        return getHeader("profile-name");
    }

    /** Registration expiry in seconds. */
    public String getExpires() {
        return getHeader("expires");
    }

    /** SIP User-Agent string of the registering device. */
    public String getUserAgent() {
        return getHeader("user-agent");
    }
}
