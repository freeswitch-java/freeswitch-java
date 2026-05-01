package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired when a background job ({@code bgapi}) completes.
 * Event-Name: {@code BACKGROUND_JOB}
 *
 * <p>When using {@link com.freeswitchjava.esl.inbound.InboundClient#bgapi(String)},
 * the result is delivered directly to the returned {@code CompletableFuture} —
 * you don't need to listen to this event manually. It is available for cases where
 * you want to observe all bgapi results on a single listener.
 */
public final class BackgroundJobEvent extends EslEvent {

    public BackgroundJobEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** The Job-UUID that identifies this background job. */
    public String getJobUuid() {
        return getHeader("job-uuid");
    }

    /** The command that was executed, e.g. {@code "originate"}. */
    public String getJobCommand() {
        return getHeader("job-command");
    }

    /** Command arguments. */
    public String getJobCommandArg() {
        return getHeader("job-command-arg");
    }

    /** The result output of the command (same as the body). */
    public String getResult() {
        return getEventBody();
    }

    /** {@code true} if the job result starts with {@code +OK}. */
    public boolean isSuccess() {
        String result = getResult();
        return result != null && result.startsWith("+OK");
    }
}
