package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code url_encode} API command — URL-encodes a string.
 *
 * <pre>{@code
 * ApiResponse r = client.api(new UrlEncodeCommand("hello world")).join();
 * System.out.println(r.getBody()); // "hello%20world"
 * }</pre>
 */
public final class UrlEncodeCommand implements EslApiCommand {

    private final String value;

    public UrlEncodeCommand(String value) {
        this.value = value;
    }

    @Override
    public String toApiString() {
        return "url_encode " + value;
    }
}
