package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code md5} API command — returns the MD5 hash of a string.
 *
 * <pre>{@code
 * ApiResponse r = client.api(new Md5Command("ClueCon")).join();
 * System.out.println(r.getBody()); // MD5 hex string
 * }</pre>
 */
public final class Md5Command implements EslApiCommand {

    private final String value;

    public Md5Command(String value) {
        this.value = value;
    }

    @Override
    public String toApiString() {
        return "md5 " + value;
    }
}
