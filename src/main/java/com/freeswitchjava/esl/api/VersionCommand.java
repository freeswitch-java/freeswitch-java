package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code version} API command — returns the FreeSWITCH version string.
 *
 * <pre>{@code
 * ApiResponse r = client.api(new VersionCommand()).join();
 * System.out.println(r.getBody()); // "FreeSWITCH Version 1.10.9 ..."
 * }</pre>
 */
public final class VersionCommand implements EslApiCommand {

    @Override
    public String toApiString() {
        return "version";
    }
}
