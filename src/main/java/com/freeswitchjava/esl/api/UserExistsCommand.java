package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code user_exists} API command — checks if a user exists in the directory.
 *
 * <p>Returns {@code "true"} or {@code "false"}.
 *
 * <pre>{@code
 * boolean exists = "true".equals(
 *     client.api(new UserExistsCommand("id", "1001", "domain.com")).join().getBody().trim());
 * }</pre>
 */
public final class UserExistsCommand implements EslApiCommand {

    private final String key;
    private final String value;
    private final String domain;

    public UserExistsCommand(String key, String value, String domain) {
        this.key    = key;
        this.value  = value;
        this.domain = domain;
    }

    @Override
    public String toApiString() {
        return "user_exists " + key + " " + value + " " + domain;
    }
}
