package com.freeswitchjava.esl.outbound;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Routes an incoming outbound session to the appropriate handler based on channel data.
 *
 * <p>The router is called <em>after</em> {@code connect()} completes, so all channel
 * variables (caller ID, context, SIP headers, etc.) are available for routing decisions.
 *
 * <p>Use the static factory methods for common patterns, or implement the interface
 * for custom logic:
 *
 * <pre>{@code
 * OutboundServer.create(config,
 *     OutboundSessionRouter.predicate(
 *         req -> "ivr".equals(req.getContext()),
 *         session -> { /* IVR handler *\/ },
 *         session -> { /* default handler *\/ }
 *     ));
 * }</pre>
 *
 * @see StaticRouter
 * @see PredicateRouter
 * @see CompositeRouter
 */
@FunctionalInterface
public interface OutboundSessionRouter {

    /**
     * Returns the handler for this session.
     * Called after {@code connect()} — channel variables are available via {@link OutboundSession#getRequest()}.
     *
     * @param request typed view of the incoming channel
     * @return the handler to invoke, or {@code null} to reject the session (hangs up)
     */
    Consumer<OutboundSession> route(OutboundSessionRequest request);

    // ── Factory helpers ───────────────────────────────────────────────────────

    /** Routes all sessions to the same handler. */
    static OutboundSessionRouter staticHandler(Consumer<OutboundSession> handler) {
        return new StaticRouter(handler);
    }

    /**
     * Routes to {@code handler} if {@code predicate} matches, otherwise to {@code fallback}.
     *
     * <pre>{@code
     * OutboundSessionRouter.predicate(
     *     req -> "sales".equals(req.getContext()),
     *     salesSession -> { ... },
     *     defaultSession -> { ... }
     * )
     * }</pre>
     */
    static OutboundSessionRouter predicate(Predicate<OutboundSessionRequest> predicate,
                                           Consumer<OutboundSession> handler,
                                           Consumer<OutboundSession> fallback) {
        return new PredicateRouter(predicate, handler, OutboundSessionRouter.staticHandler(fallback));
    }

    /**
     * Chains multiple routers — first non-null result wins.
     *
     * <pre>{@code
     * OutboundSessionRouter.composite(salesRouter, supportRouter, defaultRouter)
     * }</pre>
     */
    static OutboundSessionRouter composite(OutboundSessionRouter... routers) {
        return new CompositeRouter(routers);
    }
}
