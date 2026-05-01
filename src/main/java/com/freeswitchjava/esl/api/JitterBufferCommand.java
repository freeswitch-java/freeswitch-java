package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_jitterbuffer} API command — enables or disables a jitter buffer.
 *
 * <pre>{@code
 * client.api(new JitterBufferCommand("uuid-abc", 100));  // 100ms jitter buffer
 * client.api(new JitterBufferCommand("uuid-abc", 0));    // disable
 * }</pre>
 */
public final class JitterBufferCommand implements EslApiCommand {

    private final String uuid;
    private final int ms;

    /**
     * @param ms jitter buffer size in milliseconds; {@code 0} disables it
     */
    public JitterBufferCommand(String uuid, int ms) {
        this.uuid = uuid;
        this.ms   = ms;
    }

    @Override
    public String toApiString() {
        return "uuid_jitterbuffer " + uuid + " " + ms;
    }
}
