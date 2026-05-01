package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_broadcast} API command — plays a file or tone to a channel.
 *
 * <pre>{@code
 * client.api(new PlaybackCommand("uuid-abc", "/tmp/welcome.wav"));
 * client.api(new PlaybackCommand("uuid-abc", "/tmp/welcome.wav").leg("aleg"));
 * client.api(new PlaybackCommand("uuid-abc", "tone_stream://%(2000,4000,440,480)"));
 * }</pre>
 */
public final class PlaybackCommand implements EslApiCommand {

    private final String uuid;
    private final String file;
    private String leg = "aleg";   // aleg, bleg, both

    public PlaybackCommand(String uuid, String file) {
        this.uuid = uuid;
        this.file = file;
    }

    /** Which leg to play on: {@code aleg} (default), {@code bleg}, or {@code both}. */
    public PlaybackCommand leg(String leg) {
        this.leg = leg;
        return this;
    }

    @Override
    public String toApiString() {
        return "uuid_broadcast " + uuid + " " + file + " " + leg;
    }
}
