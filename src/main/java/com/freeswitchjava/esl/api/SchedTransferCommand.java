package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code sched_transfer} API command — schedules a channel transfer.
 *
 * <pre>{@code
 * // Transfer in 60 seconds to extension 1002
 * client.api(new SchedTransferCommand("+60", "uuid-abc", "1002"));
 *
 * // With explicit dialplan and context
 * client.api(new SchedTransferCommand("+60", "uuid-abc", "1002", "XML", "default"));
 * }</pre>
 */
public final class SchedTransferCommand implements EslApiCommand {

    private final String time;
    private final String uuid;
    private final String extension;
    private String dialplan = "XML";
    private String context  = "default";

    public SchedTransferCommand(String time, String uuid, String extension) {
        this.time      = time;
        this.uuid      = uuid;
        this.extension = extension;
    }

    public SchedTransferCommand(String time, String uuid, String extension,
                                String dialplan, String context) {
        this.time      = time;
        this.uuid      = uuid;
        this.extension = extension;
        this.dialplan  = dialplan;
        this.context   = context;
    }

    @Override
    public String toApiString() {
        return "sched_transfer " + time + " " + uuid
             + " " + extension + " " + dialplan + " " + context;
    }
}
