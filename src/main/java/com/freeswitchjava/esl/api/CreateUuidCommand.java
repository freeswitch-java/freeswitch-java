package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code create_uuid} API command — generates a new UUID.
 *
 * <pre>{@code
 * String uuid = client.api(new CreateUuidCommand()).join().getBody().trim();
 * }</pre>
 */
public final class CreateUuidCommand implements EslApiCommand {

    @Override
    public String toApiString() {
        return "create_uuid";
    }
}
