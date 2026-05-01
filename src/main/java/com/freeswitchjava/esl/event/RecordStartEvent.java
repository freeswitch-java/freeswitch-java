package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when recording starts on a channel.
 * Event-Name: {@code RECORD_START}
 */
public final class RecordStartEvent extends ChannelEvent {

    public RecordStartEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** Full path of the recording file. */
    public String getRecordFilePath() {
        return getHeader("record-file-path");
    }
}
