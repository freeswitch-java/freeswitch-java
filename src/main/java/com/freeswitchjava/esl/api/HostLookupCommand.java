package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code host_lookup} API command — resolves a hostname to an IP address via DNS.
 *
 * <pre>{@code
 * ApiResponse r = client.api(new HostLookupCommand("sip.example.com")).join();
 * System.out.println(r.getBody()); // "93.184.216.34"
 * }</pre>
 */
public final class HostLookupCommand implements EslApiCommand {

    private final String hostname;

    public HostLookupCommand(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toApiString() {
        return "host_lookup " + hostname;
    }
}
