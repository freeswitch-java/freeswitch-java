package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_ring_ready} API command — sends 180 Ringing to the channel.
 *
 * <pre>{@code
 * client.api(new RingReadyCommand("uuid-abc"));
 * }</pre>
 */
public final class RingReadyCommand implements EslApiCommand {

    private final String uuid;

    public RingReadyCommand(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toApiString() {
        return "uuid_ring_ready " + uuid;
    }
}
