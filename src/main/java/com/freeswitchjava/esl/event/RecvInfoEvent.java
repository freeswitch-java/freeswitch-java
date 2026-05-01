package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired when a SIP INFO message is received.
 * Event-Name: {@code RECV_INFO}
 */
public final class RecvInfoEvent extends EslEvent {

    public RecvInfoEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** Content-Type of the SIP INFO body. */
    public String getSipInfoContentType() {
        return getHeader("info-type");
    }

    /** Body of the SIP INFO message. */
    public String getSipInfoBody() {
        return getEventBody();
    }
}
