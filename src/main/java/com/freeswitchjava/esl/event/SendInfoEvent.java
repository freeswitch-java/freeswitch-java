package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired when a SIP INFO message is sent.
 * Event-Name: {@code SEND_INFO}
 */
public final class SendInfoEvent extends EslEvent {

    public SendInfoEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** Content-Type of the SIP INFO body. */
    public String getSipInfoContentType() {
        return getHeader("info-type");
    }
}
