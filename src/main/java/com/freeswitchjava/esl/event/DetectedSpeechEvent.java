package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired when speech is detected or a speech recognition result is available.
 * Event-Name: {@code DETECTED_SPEECH}
 */
public final class DetectedSpeechEvent extends EslEvent {

    public DetectedSpeechEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /**
     * Speech event type: {@code "begin-speaking"}, {@code "stop-speaking"},
     * {@code "detected-speech"}, {@code "detected-silence"}.
     */
    public String getSpeechType() {
        return getHeader("speech-type");
    }

    /** ASR grammar that matched. */
    public String getGrammar() {
        return getHeader("speech-grammar");
    }

    /** Raw recognition result (XML or JSON depending on ASR engine). */
    public String getRecognitionResult() {
        return getEventBody();
    }
}
