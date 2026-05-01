package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_exists} API command — checks whether a channel UUID is active.
 *
 * <p>Returns {@code "true"} or {@code "false"} in the response body.
 *
 * <pre>{@code
 * boolean active = "true".equals(
 *     client.api(new UuidExistsCommand("uuid-abc")).join().getBody().trim());
 * }</pre>
 */
public final class UuidExistsCommand implements EslApiCommand {

    private final String uuid;

    public UuidExistsCommand(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toApiString() {
        return "uuid_exists " + uuid;
    }
}
