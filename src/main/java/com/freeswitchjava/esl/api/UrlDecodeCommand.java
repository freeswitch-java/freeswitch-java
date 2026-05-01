package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code url_decode} API command — URL-decodes a string.
 *
 * <pre>{@code
 * ApiResponse r = client.api(new UrlDecodeCommand("hello%20world")).join();
 * System.out.println(r.getBody()); // "hello world"
 * }</pre>
 */
public final class UrlDecodeCommand implements EslApiCommand {

    private final String value;

    public UrlDecodeCommand(String value) {
        this.value = value;
    }

    @Override
    public String toApiString() {
        return "url_decode " + value;
    }
}
