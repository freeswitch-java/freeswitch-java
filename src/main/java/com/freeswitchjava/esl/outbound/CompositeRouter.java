package com.freeswitchjava.esl.outbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Tries each router in order and returns the first non-null handler.
 * If all routers return {@code null} the session is rejected (hung up).
 *
 * <pre>{@code
 * OutboundSessionRouter router = new CompositeRouter(
 *     new PredicateRouter(req -> "sales".equals(req.getContext()),   salesHandler,   null),
 *     new PredicateRouter(req -> "support".equals(req.getContext()), supportHandler, null),
 *     OutboundSessionRouter.staticHandler(defaultHandler)  // catch-all
 * );
 *
 * OutboundServer.create(config, router);
 * }</pre>
 */
public final class CompositeRouter implements OutboundSessionRouter {

    private static final Logger log = LoggerFactory.getLogger(CompositeRouter.class);

    private final OutboundSessionRouter[] routers;

    public CompositeRouter(OutboundSessionRouter... routers) {
        this.routers = routers;
    }

    @Override
    public Consumer<OutboundSession> route(OutboundSessionRequest request) {
        for (OutboundSessionRouter router : routers) {
            Consumer<OutboundSession> handler = router.route(request);
            if (handler != null) return handler;
        }
        log.warn("[ROUTER] No handler matched — uuid=[{}] from=[{}] context=[{}]",
                request.getUniqueId(), request.getCallerIdNumber(), request.getContext());
        return null;
    }
}
