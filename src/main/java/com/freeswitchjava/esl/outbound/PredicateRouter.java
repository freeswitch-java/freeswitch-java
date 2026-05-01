package com.freeswitchjava.esl.outbound;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Routes a session to one of two handlers based on a predicate evaluated against the
 * {@link OutboundSessionRequest}.
 *
 * <pre>{@code
 * // Route by dialplan context
 * new PredicateRouter(
 *     req -> "ivr".equals(req.getContext()),
 *     session -> { /* IVR flow *\/ },
 *     OutboundSessionRouter.staticHandler(session -> session.hangup().join())
 * )
 *
 * // Route by caller ID prefix
 * new PredicateRouter(
 *     req -> req.getCallerIdNumber().startsWith("1800"),
 *     session -> { /* toll-free handler *\/ },
 *     defaultRouter
 * )
 * }</pre>
 */
public final class PredicateRouter implements OutboundSessionRouter {

    private final Predicate<OutboundSessionRequest> predicate;
    private final Consumer<OutboundSession> handler;
    private final OutboundSessionRouter fallback;

    /**
     * @param predicate condition evaluated against the incoming session request
     * @param handler   handler used when predicate matches
     * @param fallback  router used when predicate does not match (may be another {@code PredicateRouter})
     */
    public PredicateRouter(Predicate<OutboundSessionRequest> predicate,
                           Consumer<OutboundSession> handler,
                           OutboundSessionRouter fallback) {
        this.predicate = predicate;
        this.handler   = handler;
        this.fallback  = fallback;
    }

    @Override
    public Consumer<OutboundSession> route(OutboundSessionRequest request) {
        if (predicate.test(request)) {
            return handler;
        }
        return fallback != null ? fallback.route(request) : null;
    }
}
