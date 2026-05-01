package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_break} API command — stops currently playing media on a channel.
 *
 * <pre>{@code
 * client.api(new BreakCommand("uuid-abc"));
 * client.api(new BreakCommand("uuid-abc").all()); // stop all queued playback
 * }</pre>
 */
public final class BreakCommand implements EslApiCommand {

    private final String uuid;
    private boolean all = false;

    public BreakCommand(String uuid) {
        this.uuid = uuid;
    }

    /** Stop all queued media, not just the current file. */
    public BreakCommand all() {
        this.all = true;
        return this;
    }

    @Override
    public String toApiString() {
        return all ? "uuid_break " + uuid + " all" : "uuid_break " + uuid;
    }
}
