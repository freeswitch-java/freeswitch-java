package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Call Detail Record event — fired at the end of a call with full billing/stats data.
 * Event-Name: {@code CDR}
 *
 * <p>The full CDR XML is available in the event body via {@link #getCdrXml()}.
 */
public final class CdrEvent extends EslEvent {

    public CdrEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** Full CDR XML document (in the event body). */
    public String getCdrXml() {
        return getEventBody();
    }

    /** Call UUID. */
    @Override
    public String getUniqueId() {
        return getHeader("unique-id");
    }

    /** Total call duration in seconds. */
    public String getDuration() {
        return getHeader("variable_duration");
    }

    /** Billable seconds (answered → hangup). */
    public String getBillSec() {
        return getHeader("variable_billsec");
    }

    /** Hangup cause. */
    @Override
    public String getHangupCause() {
        return getHeader("variable_hangup_cause");
    }
}
