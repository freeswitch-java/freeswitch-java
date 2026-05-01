package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code sched_del} API command — cancels a scheduled task.
 *
 * <p>Pass a task ID (from {@code sched_api} response) or a channel UUID to cancel
 * all tasks scheduled for that channel.
 *
 * <pre>{@code
 * client.api(new SchedDelCommand("task-id-or-uuid"));
 * }</pre>
 */
public final class SchedDelCommand implements EslApiCommand {

    private final String taskIdOrUuid;

    public SchedDelCommand(String taskIdOrUuid) {
        this.taskIdOrUuid = taskIdOrUuid;
    }

    @Override
    public String toApiString() {
        return "sched_del " + taskIdOrUuid;
    }
}
