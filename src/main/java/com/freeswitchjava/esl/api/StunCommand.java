package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code stun} API command — performs a STUN lookup to discover the public IP.
 *
 * <pre>{@code
 * // Use default STUN server (stun.freeswitch.org)
 * client.api(new StunCommand());
 *
 * // Use custom STUN server
 * client.api(new StunCommand("stun.l.google.com:19302"));
 * }</pre>
 */
public final class StunCommand implements EslApiCommand {

    private final String server;

    /** Uses default STUN server ({@code stun.freeswitch.org}). */
    public StunCommand() {
        this(null);
    }

    public StunCommand(String server) {
        this.server = server;
    }

    @Override
    public String toApiString() {
        return server != null ? "stun " + server : "stun";
    }
}
