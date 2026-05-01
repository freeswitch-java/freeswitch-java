package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when audio playback starts on a channel.
 * Event-Name: {@code PLAYBACK_START}
 */
public final class PlaybackStartEvent extends ChannelEvent {

    public PlaybackStartEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** The file or TTS string being played. */
    public String getPlaybackFile() {
        return getHeader("playback-file-path");
    }
}
