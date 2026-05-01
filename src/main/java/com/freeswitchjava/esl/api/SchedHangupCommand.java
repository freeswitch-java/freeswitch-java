package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.HangupCause;

/**
 * FreeSWITCH {@code sched_hangup} API command — schedules a channel hangup.
 *
 * <p>The time can be absolute (Unix epoch) or relative (seconds from now using {@code +} prefix).
 *
 * <pre>{@code
 * // Hang up in 30 seconds
 * client.api(new SchedHangupCommand("+30", "uuid-abc"));
 *
 * // Hang up in 30 seconds with a specific cause
 * client.api(new SchedHangupCommand("+30", "uuid-abc", HangupCause.ALLOTTED_TIMEOUT));
 * }</pre>
 */
public final class SchedHangupCommand implements EslApiCommand {

    private final String time;
    private final String uuid;
    private final String cause;

    public SchedHangupCommand(String time, String uuid) {
        this(time, uuid, HangupCause.ALLOTTED_TIMEOUT);
    }

    public SchedHangupCommand(String time, String uuid, HangupCause cause) {
        this(time, uuid, cause.name());
    }

    public SchedHangupCommand(String time, String uuid, String cause) {
        this.time  = time;
        this.uuid  = uuid;
        this.cause = cause;
    }

    @Override
    public String toApiString() {
        return "sched_hangup " + time + " " + uuid + " " + cause;
    }
}
