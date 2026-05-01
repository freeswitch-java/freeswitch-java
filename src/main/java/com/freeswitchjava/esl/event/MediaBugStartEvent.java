package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a media bug (tap) is attached to a channel.
 * Event-Name: {@code MEDIA_BUG_START}
 *
 * <p>Media bugs are used by modules that need to intercept audio/video,
 * such as recording, speech recognition, and fax detection.
 */
public final class MediaBugStartEvent extends ChannelEvent {

    public MediaBugStartEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** The media bug function/purpose, e.g. {@code "session_recording"}. */
    public String getBugFunction() {
        return getHeader("media-bug-function");
    }

    /** Target of the media tap, e.g. a file path or stream URL. */
    public String getBugTarget() {
        return getHeader("media-bug-target");
    }
}
