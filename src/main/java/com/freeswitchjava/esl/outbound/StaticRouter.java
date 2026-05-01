package com.freeswitchjava.esl.outbound;

import java.util.function.Consumer;

/**
 * Routes all sessions to the same handler regardless of channel data.
 *
 * <pre>{@code
 * OutboundServer.create(config, new StaticRouter(session -> {
 *     session.answer().join();
 *     session.playback("ivr/welcome.wav").join();
 *     session.hangup().join();
 * }));
 * }</pre>
 */
public final class StaticRouter implements OutboundSessionRouter {

    private final Consumer<OutboundSession> handler;

    public StaticRouter(Consumer<OutboundSession> handler) {
        this.handler = handler;
    }

    @Override
    public Consumer<OutboundSession> route(OutboundSessionRequest request) {
        return handler;
    }
}
