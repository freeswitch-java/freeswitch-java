package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_phone_event} API command — injects a phone event into a channel.
 *
 * <pre>{@code
 * client.api(new PhoneEventCommand("uuid-abc", "talk"));
 * client.api(new PhoneEventCommand("uuid-abc", "notalk"));
 * }</pre>
 */
public final class PhoneEventCommand implements EslApiCommand {

    private final String uuid;
    private final String event;

    public PhoneEventCommand(String uuid, String event) {
        this.uuid  = uuid;
        this.event = event;
    }

    @Override
    public String toApiString() {
        return "uuid_phone_event " + uuid + " " + event;
    }
}
