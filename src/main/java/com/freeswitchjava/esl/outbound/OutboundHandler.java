package com.freeswitchjava.esl.outbound;

import com.freeswitchjava.esl.codec.EslHeaders;
import com.freeswitchjava.esl.codec.EslMessage;
import com.freeswitchjava.esl.event.EventBus;
import com.freeswitchjava.esl.event.EventName;
import com.freeswitchjava.esl.inbound.BgapiJobTracker;
import com.freeswitchjava.esl.inbound.PendingCommandQueue;
import com.freeswitchjava.esl.model.CommandReply;
import com.freeswitchjava.esl.model.EslEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class OutboundHandler extends SimpleChannelInboundHandler<EslMessage> {

    private static final Logger log = LoggerFactory.getLogger(OutboundHandler.class);

    private final OutboundSessionRouter router;
    private final Semaphore semaphore;   // null = unlimited
    private final boolean autoConnect;   // true = router mode: connect before routing

    private PendingCommandQueue pendingQueue;
    private PendingCommandQueue apiQueue;
    private BgapiJobTracker bgapiTracker;
    private EventBus eventBus;
    private OutboundSession session;

    private String remote = "?";

    OutboundHandler(OutboundSessionRouter router, Semaphore semaphore, boolean autoConnect) {
        this.router      = router;
        this.semaphore   = semaphore;
        this.autoConnect = autoConnect;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        remote = ctx.channel().remoteAddress().toString();
        MDC.put("eslChannel", remote);
        log.info("[OUTBOUND] [CONNECT] FreeSWITCH connected from {}", remote);

        // Enforce max concurrent sessions
        if (semaphore != null && !semaphore.tryAcquire()) {
            log.warn("[OUTBOUND] [REJECT] Max concurrent sessions reached — rejecting {}", remote);
            ctx.writeAndFlush("api hangup\n\n");
            ctx.close();
            return;
        }

        pendingQueue = new PendingCommandQueue();
        apiQueue     = new PendingCommandQueue();
        bgapiTracker = new BgapiJobTracker();
        eventBus     = new EventBus(Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "esl-outbound-event-" + ctx.channel().id().asShortText());
            t.setDaemon(true);
            return t;
        }));

        session = new OutboundSession(ctx.channel(), pendingQueue, apiQueue, bgapiTracker, eventBus);

        Executor handlerExecutor = Executors.newVirtualThreadPerTaskExecutor();
        handlerExecutor.execute(() -> {
            try {
                Consumer<OutboundSession> handler;

                if (autoConnect) {
                    // Router mode: connect first so routing can inspect channel variables
                    session.connect().join();
                    OutboundSessionRequest request = session.getRequest();
                    handler = router.route(request);
                    if (handler == null) {
                        log.warn("[OUTBOUND] [ROUTE] No handler — hanging up uuid=[{}]",
                                session.getUniqueId());
                        session.hangup().join();
                        return;
                    }
                    log.debug("[OUTBOUND] [ROUTE] uuid=[{}] from=[{}] context=[{}]",
                            session.getUniqueId(), request.getCallerIdNumber(), request.getContext());
                } else {
                    // Consumer mode: pass session to handler as-is; handler calls connect()
                    handler = router.route(null);
                    if (handler == null) return;
                }

                handler.accept(session);

            } catch (Exception e) {
                log.error("[OUTBOUND] [SESSION] Unhandled exception — uuid=[{}]",
                        session.getUniqueId(), e);
            } finally {
                if (semaphore != null) semaphore.release();
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EslMessage message) {
        String ct = message.getContentType();
        if (ct == null) {
            log.warn("[OUTBOUND] [RECV] No Content-Type — ignoring");
            return;
        }
        switch (ct) {
            case EslHeaders.CT_COMMAND_REPLY -> {
                CommandReply reply = new CommandReply(message);
                if (!pendingQueue.complete(reply)) {
                    log.warn("[OUTBOUND] [CMD] Unexpected reply, no pending command — reply=[{}]",
                            reply.getReplyText());
                } else {
                    log.debug("[OUTBOUND] [CMD] reply=[{}]", reply.getReplyText());
                }
            }
            case EslHeaders.CT_API_RESPONSE -> {
                CommandReply synthetic = new CommandReply(message);
                if (!apiQueue.complete(synthetic)) {
                    log.warn("[OUTBOUND] [API] Unexpected response, no pending API command");
                }
            }
            case EslHeaders.CT_EVENT_PLAIN -> dispatchEvent(EslEvent.fromPlainMessage(message));
            case EslHeaders.CT_EVENT_JSON  -> {
                try { dispatchEvent(EslEvent.fromJsonMessage(message)); }
                catch (Exception e) {
                    log.warn("[OUTBOUND] [EVENT] JSON parse failed — {}", e.getMessage());
                }
            }
            case EslHeaders.CT_DISCONNECT_NOTICE -> {
                log.info("[OUTBOUND] [DISCONNECT] Notice received — {}", remote);
                ctx.close();
            }
            default -> log.debug("[OUTBOUND] [RECV] Unknown content-type [{}]", ct);
        }
    }

    private void dispatchEvent(EslEvent event) {
        String name = event.getEventName();
        String uuid = event.getUniqueId();

        if (EventName.BACKGROUND_JOB.wireValue().equals(name)) {
            String jobUuid = event.getHeader(EslHeaders.JOB_UUID);
            if (jobUuid == null) jobUuid = event.getHeader(EslHeaders.VARIABLE_JOB_UUID);
            if (jobUuid != null && bgapiTracker.complete(jobUuid, event.getEventBody())) {
                log.debug("[OUTBOUND] [BGAPI] Result delivered — job=[{}]", jobUuid);
                return;
            }
        }

        if (uuid != null) {
            log.debug("[OUTBOUND] [EVENT] [{}] uuid=[{}]", name, uuid);
        } else {
            log.debug("[OUTBOUND] [EVENT] [{}]", name);
        }
        eventBus.publish(event);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("[OUTBOUND] [DISCONNECT] Disconnected — {}", remote);
        RuntimeException ex = new IllegalStateException("ESL outbound connection closed");
        pendingQueue.failAll(ex);
        apiQueue.failAll(ex);
        bgapiTracker.failAll(ex);
        eventBus.clear();
        MDC.remove("eslChannel");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("[OUTBOUND] [ERROR] Channel exception — {}", cause.getMessage(), cause);
        ctx.close();
    }
}
