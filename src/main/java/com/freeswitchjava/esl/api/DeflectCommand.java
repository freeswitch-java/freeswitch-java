package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_deflect} API command — redirects an answered SIP call via REFER.
 *
 * <pre>{@code
 * client.api(new DeflectCommand("uuid-abc", "sip:1002@domain.com"));
 * }</pre>
 */
public final class DeflectCommand implements EslApiCommand {

    private final String uuid;
    private final String sipUrl;

    public DeflectCommand(String uuid, String sipUrl) {
        this.uuid   = uuid;
        this.sipUrl = sipUrl;
    }

    @Override
    public String toApiString() {
        return "uuid_deflect " + uuid + " " + sipUrl;
    }
}
