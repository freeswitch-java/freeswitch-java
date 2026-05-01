package com.freeswitchjava.esl.dialplan.app;

import com.freeswitchjava.esl.dialplan.DpApp;
import com.freeswitchjava.esl.dialplan.DpTools;

/**
 * Typed builder for the {@code limit} dialplan application.
 *
 * <p>Enforces a maximum concurrent call count for a resource.
 * When the limit is exceeded, the caller is transferred to {@code transferDest}.
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Allow max 5 concurrent calls to "support"
 * session.execute(new LimitApp("outbound", "1001")
 *     .max(5)
 *     .transferDest("busy_signal XML default")).join();
 *
 * // Using db backend instead of hash
 * session.execute(new LimitApp("outbound", "1001")
 *     .backend("db")
 *     .max(3)).join();
 * }</pre>
 */
public final class LimitApp implements DpApp {

    private String backend      = "hash";
    private final String realm;
    private final String resource;
    private int    max          = -1;
    private String transferDest;

    /**
     * @param realm    namespace for the limit (e.g. {@code "outbound"})
     * @param resource resource identifier (e.g. extension number)
     */
    public LimitApp(String realm, String resource) {
        this.realm    = realm;
        this.resource = resource;
    }

    /**
     * Storage backend: {@code "hash"} (default, in-memory) or {@code "db"} (SQLite).
     */
    public LimitApp backend(String backend) { this.backend = backend; return this; }

    /** Maximum concurrent calls allowed (-1 = no limit, default). */
    public LimitApp max(int max) { this.max = max; return this; }

    /**
     * Transfer destination if limit is exceeded.
     * Format: {@code "extension dialplan context"} (e.g. {@code "busy XML default"}).
     */
    public LimitApp transferDest(String dest) { this.transferDest = dest; return this; }

    @Override
    public String appName() { return DpTools.LIMIT; }

    @Override
    public String toArg() {
        StringBuilder sb = new StringBuilder(backend)
                .append(' ').append(realm)
                .append(' ').append(resource);
        if (max >= 0)          sb.append(' ').append(max);
        if (transferDest != null) sb.append(' ').append(transferDest);
        return sb.toString();
    }
}
