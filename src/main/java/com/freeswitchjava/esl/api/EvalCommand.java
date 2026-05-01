package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code eval} API command — evaluates a FreeSWITCH expression.
 *
 * <p>Can optionally be scoped to a specific channel UUID to evaluate channel variables.
 *
 * <pre>{@code
 * // Evaluate a global expression
 * client.api(new EvalCommand("${domain_name}"));
 *
 * // Evaluate a channel variable
 * client.api(new EvalCommand("${caller_id_number}", "uuid-abc"));
 * }</pre>
 */
public final class EvalCommand implements EslApiCommand {

    private final String expression;
    private final String uuid;

    public EvalCommand(String expression) {
        this(expression, null);
    }

    public EvalCommand(String expression, String uuid) {
        this.expression = expression;
        this.uuid       = uuid;
    }

    @Override
    public String toApiString() {
        return uuid != null
            ? "eval uuid:" + uuid + " " + expression
            : "eval " + expression;
    }
}
