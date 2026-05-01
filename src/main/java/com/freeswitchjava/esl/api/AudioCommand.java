package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_audio} API command — adjusts or mutes audio gain on a channel.
 *
 * <pre>{@code
 * client.api(new AudioCommand("uuid-abc").startRead(4));   // boost inbound audio
 * client.api(new AudioCommand("uuid-abc").startWrite(-4)); // attenuate outbound audio
 * client.api(new AudioCommand("uuid-abc").muteRead());     // mute caller's mic
 * client.api(new AudioCommand("uuid-abc").stop());         // remove all audio adjustments
 * }</pre>
 */
public final class AudioCommand implements EslApiCommand {

    private final String uuid;
    private String subcommand;

    public AudioCommand(String uuid) {
        this.uuid = uuid;
    }

    /** Boost/attenuate audio coming from the caller (inbound RTP). Level: -4 to 4. */
    public AudioCommand startRead(int level) {
        this.subcommand = "start read level " + level;
        return this;
    }

    /** Boost/attenuate audio sent to the caller (outbound RTP). Level: -4 to 4. */
    public AudioCommand startWrite(int level) {
        this.subcommand = "start write level " + level;
        return this;
    }

    /** Mute audio coming from the caller. */
    public AudioCommand muteRead() {
        this.subcommand = "start read mute";
        return this;
    }

    /** Mute audio sent to the caller. */
    public AudioCommand muteWrite() {
        this.subcommand = "start write mute";
        return this;
    }

    /** Remove all audio gain/mute adjustments. */
    public AudioCommand stop() {
        this.subcommand = "stop";
        return this;
    }

    @Override
    public String toApiString() {
        if (subcommand == null) throw new IllegalStateException("AudioCommand: call start/stop before building");
        return "uuid_audio " + uuid + " " + subcommand;
    }
}
