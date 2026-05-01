package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code sched_broadcast} API command — schedules an audio broadcast to a channel.
 *
 * <pre>{@code
 * // Play a file in 10 seconds
 * client.api(new SchedBroadcastCommand("+10", "uuid-abc", "/tmp/warning.wav"));
 *
 * // Play to both legs in 10 seconds
 * client.api(new SchedBroadcastCommand("+10", "uuid-abc", "/tmp/warning.wav").leg("both"));
 * }</pre>
 */
public final class SchedBroadcastCommand implements EslApiCommand {

    private final String time;
    private final String uuid;
    private final String file;
    private String leg;  // aleg (default), bleg, both

    public SchedBroadcastCommand(String time, String uuid, String file) {
        this.time = time;
        this.uuid = uuid;
        this.file = file;
    }

    /** Which leg: {@code aleg} (default), {@code bleg}, or {@code both}. */
    public SchedBroadcastCommand leg(String leg) {
        this.leg = leg;
        return this;
    }

    @Override
    public String toApiString() {
        StringBuilder sb = new StringBuilder("sched_broadcast ")
                .append(time).append(" ").append(uuid);
        if (leg != null) sb.append(" ").append(leg);
        sb.append(" ").append(file);
        return sb.toString();
    }
}
