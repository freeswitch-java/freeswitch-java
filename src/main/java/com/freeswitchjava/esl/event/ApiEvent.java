package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired when an API command is executed on the FreeSWITCH instance.
 * Event-Name: {@code API}
 */
public final class ApiEvent extends EslEvent {

    public ApiEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** The API command that was executed. */
    public String getApiCommand() {
        return getHeader("api-command");
    }

    /** Arguments passed to the command. */
    public String getApiCommandArgument() {
        return getHeader("api-command-argument");
    }
}
