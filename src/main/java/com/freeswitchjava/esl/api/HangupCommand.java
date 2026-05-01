package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.HangupCause;

/**
 * FreeSWITCH {@code uuid_kill} API command — hangs up a channel.
 *
 * <pre>{@code
 * client.api(new HangupCommand("uuid-abc"));
 * client.api(new HangupCommand("uuid-abc", HangupCause.USER_BUSY));
 * }</pre>
 */
public final class HangupCommand implements EslApiCommand {

    private final String uuid;
    private final String cause;

    public HangupCommand(String uuid) {
        this(uuid, HangupCause.NORMAL_CLEARING);
    }

    public HangupCommand(String uuid, HangupCause cause) {
        this(uuid, cause.name());
    }

    public HangupCommand(String uuid, String cause) {
        this.uuid  = uuid;
        this.cause = cause;
    }

    @Override
    public String toApiString() {
        return "uuid_kill " + uuid + " " + cause;
    }
}
