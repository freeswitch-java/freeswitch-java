package com.freeswitchjava.esl.event;

import java.util.Map;

/**
 * Fired when a dialplan application begins executing on a channel.
 * Event-Name: {@code CHANNEL_EXECUTE}
 */
public final class ChannelExecuteEvent extends ChannelEvent {

    public ChannelExecuteEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** Name of the application being executed, e.g. {@code "playback"}. */
    @Override
    public String getApplication() {
        return getHeader("application");
    }

    /** Arguments passed to the application. */
    @Override
    public String getApplicationData() {
        return getHeader("application-data");
    }

    /** UUID identifying this specific execute instance (used to correlate with CHANNEL_EXECUTE_COMPLETE). */
    public String getApplicationUuid() {
        return getHeader("application-uuid");
    }
}
