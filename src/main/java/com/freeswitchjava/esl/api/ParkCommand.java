package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_park} API command — parks a channel (no hold music, just waits).
 *
 * <pre>{@code
 * client.api(new ParkCommand("uuid-abc"));
 * }</pre>
 */
public final class ParkCommand implements EslApiCommand {

    private final String uuid;

    public ParkCommand(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toApiString() {
        return "uuid_park " + uuid;
    }
}
