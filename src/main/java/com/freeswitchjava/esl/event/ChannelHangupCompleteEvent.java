package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.HangupCause;

import java.util.Map;

/**
 * Fired after a channel has fully hung up and all variables are available.
 * Event-Name: {@code CHANNEL_HANGUP_COMPLETE}
 *
 * <p>Prefer this event over {@link ChannelHangupEvent} when you need channel variables
 * such as billing seconds, recording paths, or other post-call data.
 */
public final class ChannelHangupCompleteEvent extends ChannelEvent {

    public ChannelHangupCompleteEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /**
     * Hangup cause as a typed enum.
     */
    public HangupCause getHangupCauseEnum() {
        String cause = getHangupCause();
        if (cause == null) return HangupCause.UNSPECIFIED;
        HangupCause hc = HangupCause.fromName(cause);
        return hc != null ? hc : HangupCause.UNSPECIFIED;
    }

    @Override
    public String getHangupCause() {
        return getHeader("hangup-cause");
    }

    /** Billable seconds (answered → hangup). */
    public String getBillSec() {
        return getHeader("variable_billsec");
    }

    /** Total call duration in seconds (create → hangup). */
    public String getDuration() {
        return getHeader("variable_duration");
    }

    /** The recording file path if the call was recorded. */
    public String getRecordingFilePath() {
        return getHeader("variable_record_file_path");
    }
}
