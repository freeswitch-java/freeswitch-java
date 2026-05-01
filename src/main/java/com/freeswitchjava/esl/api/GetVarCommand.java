package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_getvar} API command — reads a channel variable.
 *
 * <pre>{@code
 * ApiResponse r = client.api(new GetVarCommand("uuid-abc", "sip_call_id")).join();
 * String callId = r.getBody();
 * }</pre>
 */
public final class GetVarCommand implements EslApiCommand {

    private final String uuid;
    private final String name;

    public GetVarCommand(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public String toApiString() {
        return "uuid_getvar " + uuid + " " + name;
    }
}
