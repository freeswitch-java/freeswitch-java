package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code user_data} API command — retrieves a field from a directory user entry.
 *
 * <pre>{@code
 * // Get a user parameter
 * ApiResponse r = client.api(new UserDataCommand("1001", "domain.com", "param", "password")).join();
 *
 * // Get a user variable
 * ApiResponse r2 = client.api(new UserDataCommand("1001", "domain.com", "var", "accountcode")).join();
 * }</pre>
 */
public final class UserDataCommand implements EslApiCommand {

    private final String user;
    private final String domain;
    private final String type;   // "param", "var", "attr"
    private final String name;

    public UserDataCommand(String user, String domain, String type, String name) {
        this.user   = user;
        this.domain = domain;
        this.type   = type;
        this.name   = name;
    }

    @Override
    public String toApiString() {
        return "user_data " + user + "@" + domain + " " + type + " " + name;
    }
}
