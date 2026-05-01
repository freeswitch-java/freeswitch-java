package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code regex} API command — evaluates a regular expression.
 *
 * <p>Without a substitution string, returns {@code "true"} or {@code "false"}.
 * With a substitution string, returns the substituted result.
 *
 * <pre>{@code
 * // Test match
 * boolean match = "true".equals(
 *     client.api(new RegexCommand("1001", "^100[0-9]$")).join().getBody().trim());
 *
 * // Substitution
 * ApiResponse r = client.api(new RegexCommand("1001@domain.com", "^(.*)@.*$", "$1")).join();
 * System.out.println(r.getBody()); // "1001"
 * }</pre>
 */
public final class RegexCommand implements EslApiCommand {

    private final String data;
    private final String pattern;
    private final String substitution;

    /** Match-only — returns {@code "true"} or {@code "false"}. */
    public RegexCommand(String data, String pattern) {
        this(data, pattern, null);
    }

    /** Match with substitution — returns the substituted string. */
    public RegexCommand(String data, String pattern, String substitution) {
        this.data         = data;
        this.pattern      = pattern;
        this.substitution = substitution;
    }

    @Override
    public String toApiString() {
        return substitution != null
            ? "regex " + data + "|" + pattern + "|" + substitution
            : "regex " + data + "|" + pattern;
    }
}
