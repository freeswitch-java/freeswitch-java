package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code domain_exists} API command — checks if a domain is configured.
 *
 * <p>Returns {@code "true"} or {@code "false"}.
 *
 * <pre>{@code
 * boolean exists = "true".equals(
 *     client.api(new DomainExistsCommand("domain.com")).join().getBody().trim());
 * }</pre>
 */
public final class DomainExistsCommand implements EslApiCommand {

    private final String domain;

    public DomainExistsCommand(String domain) {
        this.domain = domain;
    }

    @Override
    public String toApiString() {
        return "domain_exists " + domain;
    }
}
