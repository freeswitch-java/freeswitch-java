package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code sched_api} API command — schedules any API command for future execution.
 *
 * <pre>{@code
 * // Run reloadxml in 5 seconds
 * client.api(new SchedApiCommand("+5", "my-task", new ReloadXmlCommand()));
 *
 * // Raw command string
 * client.api(new SchedApiCommand("+30", "cleanup-task", "hupall NORMAL_CLEARING"));
 * }</pre>
 */
public final class SchedApiCommand implements EslApiCommand {

    private final String time;
    private final String groupName;
    private final String command;

    public SchedApiCommand(String time, String groupName, EslApiCommand command) {
        this(time, groupName, command.toApiString());
    }

    public SchedApiCommand(String time, String groupName, String command) {
        this.time      = time;
        this.groupName = groupName;
        this.command   = command;
    }

    @Override
    public String toApiString() {
        return "sched_api " + time + " " + groupName + " " + command;
    }
}
