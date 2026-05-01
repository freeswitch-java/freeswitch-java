package com.freeswitchjava.esl.api;

/**
 * Sends any raw FreeSWITCH API command string.
 *
 * <p>Use this as a fallback for commands not covered by a typed class:
 *
 * <pre>{@code
 * client.api(new RawApiCommand("sofia status profile internal"));
 * client.api(new RawApiCommand("show channels as json"));
 * client.api(new RawApiCommand("hupall NORMAL_CLEARING"));
 * }</pre>
 */
public final class RawApiCommand implements EslApiCommand {

    private final String command;

    public RawApiCommand(String command) {
        this.command = command;
    }

    @Override
    public String toApiString() {
        return command;
    }
}
