package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired when a DTMF digit is received on a channel.
 * Event-Name: {@code DTMF}
 *
 * <pre>{@code
 * client.addEventListener(EventName.DTMF, evt -> {
 *     DtmfEvent dtmf = (DtmfEvent) evt;
 *     System.out.println("Digit: " + dtmf.getDtmfDigit()
 *         + " duration=" + dtmf.getDtmfDuration());
 * });
 * }</pre>
 */
public final class DtmfEvent extends EslEvent {

    public DtmfEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** The DTMF digit received: {@code 0-9}, {@code *}, {@code #}, {@code A-D}. */
    public String getDtmfDigit() {
        return getHeader("dtmf-digit");
    }

    /** Duration of the tone in milliseconds. */
    public String getDtmfDuration() {
        return getHeader("dtmf-duration");
    }

    /**
     * Source of the DTMF: {@code CHANNEL_DTMF}, {@code RECV_INFO}, {@code inband},
     * {@code RTP}, {@code SIP_INFO}, etc.
     */
    public String getDtmfSource() {
        return getHeader("dtmf-source");
    }
}
