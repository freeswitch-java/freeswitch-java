package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when audio playback finishes on a channel.
 * Event-Name: {@code PLAYBACK_STOP}
 */
public final class PlaybackStopEvent extends ChannelEvent {

    public PlaybackStopEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** The file or TTS string that was played. */
    public String getPlaybackFile() {
        return getHeader("playback-file-path");
    }

    /** Terminator digit pressed during playback (if any), e.g. {@code "#"}. */
    public String getTerminator() {
        return getHeader("variable_playback_terminator_used");
    }
}
