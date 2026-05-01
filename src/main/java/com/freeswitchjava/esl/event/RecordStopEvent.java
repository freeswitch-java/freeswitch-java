package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when recording stops on a channel.
 * Event-Name: {@code RECORD_STOP}
 */
public final class RecordStopEvent extends ChannelEvent {

    public RecordStopEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** Full path of the recording file. */
    public String getRecordFilePath() {
        return getHeader("record-file-path");
    }

    /** Duration of the recording in seconds. */
    public String getRecordSeconds() {
        return getHeader("record-seconds");
    }

    /** Duration of the recording in milliseconds. */
    public String getRecordMilliseconds() {
        return getHeader("record-ms");
    }

    /** Number of samples recorded. */
    public String getRecordSamples() {
        return getHeader("record-samples");
    }
}
