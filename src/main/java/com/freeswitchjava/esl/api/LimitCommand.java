package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_limit} API command — enforces a resource limit on a channel.
 *
 * <pre>{@code
 * client.api(new LimitCommand("uuid-abc", "hash", "outbound", "1001", 2));
 * }</pre>
 */
public final class LimitCommand implements EslApiCommand {

    private final String uuid;
    private final String backend;
    private final String realm;
    private final String resource;
    private final int    max;

    /**
     * @param backend  limit backend: {@code "hash"} or {@code "db"}
     * @param realm    namespace for the limit (e.g. domain name)
     * @param resource resource identifier (e.g. a user or DID)
     * @param max      maximum concurrent usage (-1 = no limit)
     */
    public LimitCommand(String uuid, String backend, String realm, String resource, int max) {
        this.uuid     = uuid;
        this.backend  = backend;
        this.realm    = realm;
        this.resource = resource;
        this.max      = max;
    }

    @Override
    public String toApiString() {
        return "uuid_limit " + uuid + " " + backend + " " + realm + " " + resource + " " + max;
    }
}
