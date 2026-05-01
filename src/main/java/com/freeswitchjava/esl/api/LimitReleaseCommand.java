package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_limit_release} API command — releases a resource limit on a channel.
 *
 * <pre>{@code
 * client.api(new LimitReleaseCommand("uuid-abc", "hash", "outbound", "1001"));
 * client.api(new LimitReleaseCommand("uuid-abc")); // release all limits on this channel
 * }</pre>
 */
public final class LimitReleaseCommand implements EslApiCommand {

    private final String uuid;
    private final String backend;
    private final String realm;
    private final String resource;

    /** Release all limits on this channel. */
    public LimitReleaseCommand(String uuid) {
        this(uuid, null, null, null);
    }

    /** Release a specific limit. */
    public LimitReleaseCommand(String uuid, String backend, String realm, String resource) {
        this.uuid     = uuid;
        this.backend  = backend;
        this.realm    = realm;
        this.resource = resource;
    }

    @Override
    public String toApiString() {
        if (backend == null) return "uuid_limit_release " + uuid;
        return "uuid_limit_release " + uuid + " " + backend + " " + realm + " " + resource;
    }
}
