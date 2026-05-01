package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.HangupCause;

import java.util.Map;

/**
 * Fired when a channel starts hanging up (before resources are released).
 * Event-Name: {@code CHANNEL_HANGUP}
 *
 * <p>Use {@link ChannelHangupCompleteEvent} if you need to read channel variables
 * that are only available after the call is fully torn down.
 */
public final class ChannelHangupEvent extends ChannelEvent {

    public ChannelHangupEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /**
     * Hangup cause as a typed enum.
     * Returns {@link HangupCause#UNSPECIFIED} if the cause is missing or unknown.
     */
    public HangupCause getHangupCauseEnum() {
        String cause = getHangupCause();
        if (cause == null) return HangupCause.UNSPECIFIED;
        HangupCause hc = HangupCause.fromName(cause);
        return hc != null ? hc : HangupCause.UNSPECIFIED;
    }

    /** Hangup cause string, e.g. {@code "NORMAL_CLEARING"}. */
    @Override
    public String getHangupCause() {
        return getHeader("hangup-cause");
    }

    /** Call duration in seconds (from answer to hangup), if available. */
    public String getBillSec() {
        return getHeader("variable_billsec");
    }

    /** Total call duration in seconds (from create to hangup). */
    public String getDuration() {
        return getHeader("variable_duration");
    }
}
