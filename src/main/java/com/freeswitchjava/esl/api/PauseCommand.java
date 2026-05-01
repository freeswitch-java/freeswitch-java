package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_pause} API command — pauses or resumes media processing on a channel.
 *
 * <pre>{@code
 * client.api(new PauseCommand("uuid-abc").on());   // pause
 * client.api(new PauseCommand("uuid-abc").off());  // resume
 * }</pre>
 */
public final class PauseCommand implements EslApiCommand {

    private final String uuid;
    private boolean pause = true;

    public PauseCommand(String uuid) {
        this.uuid = uuid;
    }

    public PauseCommand on()  { this.pause = true;  return this; }
    public PauseCommand off() { this.pause = false; return this; }

    @Override
    public String toApiString() {
        return "uuid_pause " + uuid + (pause ? " on" : " off");
    }
}
