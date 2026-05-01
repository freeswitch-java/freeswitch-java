package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_bridge} API command — bridges two existing channels together.
 *
 * <pre>{@code
 * client.api(new BridgeCommand("uuid-leg-a", "uuid-leg-b"));
 * }</pre>
 */
public final class BridgeCommand implements EslApiCommand {

    private final String uuidA;
    private final String uuidB;

    public BridgeCommand(String uuidA, String uuidB) {
        this.uuidA = uuidA;
        this.uuidB = uuidB;
    }

    @Override
    public String toApiString() {
        return "uuid_bridge " + uuidA + " " + uuidB;
    }
}
