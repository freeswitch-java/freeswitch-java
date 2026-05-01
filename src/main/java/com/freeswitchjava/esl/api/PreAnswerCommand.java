package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_pre_answer} API command — sends early media (183 Session Progress).
 *
 * <pre>{@code
 * client.api(new PreAnswerCommand("uuid-abc"));
 * }</pre>
 */
public final class PreAnswerCommand implements EslApiCommand {

    private final String uuid;

    public PreAnswerCommand(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toApiString() {
        return "uuid_pre_answer " + uuid;
    }
}
