package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Base class for scheduler events: {@code ADD_SCHEDULE}, {@code DEL_SCHEDULE},
 * {@code EXE_SCHEDULE}, {@code RE_SCHEDULE}.
 */
public class ScheduleEvent extends EslEvent {

    public ScheduleEvent(Map<String, String> headers, String body) {
        super(headers, body);  // EslEvent constructor is protected — accessible from subclass
    }

    /** Unique task ID assigned by the scheduler. */
    public String getTaskId() {
        return getHeader("task-id");
    }

    /** Description of the scheduled task. */
    public String getTaskDesc() {
        return getHeader("task-desc");
    }

    /** Group the task belongs to. */
    public String getTaskGroup() {
        return getHeader("task-group");
    }

    /** Time the task is scheduled to run (Unix timestamp). */
    public String getTaskRuntime() {
        return getHeader("task-runtime");
    }
}
