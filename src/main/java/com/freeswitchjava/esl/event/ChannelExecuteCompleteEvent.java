package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a dialplan application finishes executing on a channel.
 * Event-Name: {@code CHANNEL_EXECUTE_COMPLETE}
 */
public final class ChannelExecuteCompleteEvent extends ChannelEvent {

    public ChannelExecuteCompleteEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** Name of the application that completed, e.g. {@code "playback"}. */
    @Override
    public String getApplication() {
        return getHeader("application");
    }

    /** Arguments the application was called with. */
    @Override
    public String getApplicationData() {
        return getHeader("application-data");
    }

    /** UUID of this execute instance (matches {@link ChannelExecuteEvent#getApplicationUuid()}). */
    public String getApplicationUuid() {
        return getHeader("application-uuid");
    }

    /** Application response/result, if any (e.g. DTMF collected, speech result). */
    public String getApplicationResponse() {
        return getHeader("application-response");
    }
}
