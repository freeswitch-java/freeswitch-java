package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_answer} API command — answers an unanswered channel.
 *
 * <pre>{@code
 * client.api(new AnswerCommand("uuid-abc"));
 * }</pre>
 */
public final class AnswerCommand implements EslApiCommand {

    private final String uuid;

    public AnswerCommand(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toApiString() {
        return "uuid_answer " + uuid;
    }
}
