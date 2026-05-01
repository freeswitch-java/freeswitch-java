package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code status} API command — returns system status.
 *
 * <pre>{@code
 * ApiResponse r = client.api(new StatusCommand()).join();
 * System.out.println(r.getBody());
 * }</pre>
 */
public final class StatusCommand implements EslApiCommand {

    @Override
    public String toApiString() {
        return "status";
    }
}
