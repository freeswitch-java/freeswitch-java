package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_simplify} API command — removes FreeSWITCH from the media path
 * when both endpoints support peer-to-peer (bypasses the switch for RTP).
 *
 * <pre>{@code
 * client.api(new SimplifyCommand("uuid-abc"));
 * }</pre>
 */
public final class SimplifyCommand implements EslApiCommand {

    private final String uuid;

    public SimplifyCommand(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toApiString() {
        return "uuid_simplify " + uuid;
    }
}
