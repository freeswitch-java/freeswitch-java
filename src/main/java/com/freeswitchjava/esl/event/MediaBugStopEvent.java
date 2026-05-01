package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a media bug (tap) is removed from a channel.
 * Event-Name: {@code MEDIA_BUG_STOP}
 */
public final class MediaBugStopEvent extends ChannelEvent {

    public MediaBugStopEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** The media bug function that was stopped. */
    public String getBugFunction() {
        return getHeader("media-bug-function");
    }

    /** Target of the media tap. */
    public String getBugTarget() {
        return getHeader("media-bug-target");
    }
}
