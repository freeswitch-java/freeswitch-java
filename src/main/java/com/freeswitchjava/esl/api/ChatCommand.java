package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_chat} API command — sends a chat/IM message to the channel's SIP endpoint.
 *
 * <pre>{@code
 * client.api(new ChatCommand("uuid-abc", "Your call is being transferred."));
 * }</pre>
 */
public final class ChatCommand implements EslApiCommand {

    private final String uuid;
    private final String message;

    public ChatCommand(String uuid, String message) {
        this.uuid    = uuid;
        this.message = message;
    }

    @Override
    public String toApiString() {
        return "uuid_chat " + uuid + " " + message;
    }
}
