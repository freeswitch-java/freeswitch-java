package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.HangupCause;

/**
 * FreeSWITCH {@code hupall} API command — hangs up all active calls.
 *
 * <pre>{@code
 * client.api(new HupAllCommand());
 * client.api(new HupAllCommand(HangupCause.MANAGER_REQUEST));
 * }</pre>
 */
public final class HupAllCommand implements EslApiCommand {

    private final String cause;

    public HupAllCommand() {
        this(HangupCause.NORMAL_CLEARING);
    }

    public HupAllCommand(HangupCause cause) {
        this(cause.name());
    }

    public HupAllCommand(String cause) {
        this.cause = cause;
    }

    @Override
    public String toApiString() {
        return "hupall " + cause;
    }
}
