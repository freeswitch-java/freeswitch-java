package com.freeswitchjava.esl.inbound;

import com.freeswitchjava.esl.codec.EslHeaders;
import com.freeswitchjava.esl.codec.EslMessage;
import com.freeswitchjava.esl.event.EventBus;
import com.freeswitchjava.esl.event.EventName;
import com.freeswitchjava.esl.model.CommandReply;
import com.freeswitchjava.esl.model.EslEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.CompletableFuture;

public class InboundHandler extends SimpleChannelInboundHandler<EslMessage> {

    private static final Logger log = LoggerFactory.getLogger(InboundHandler.class);

    private final String password;
    private final CompletableFuture<Void> authFuture;
    private final PendingCommandQueue pendingQueue;
    private final PendingCommandQueue apiQueue;
    private final BgapiJobTracker bgapiTracker;
    private final EventBus eventBus;

    private boolean authenticated = false;
    private String remote = "?";

    public InboundHandler(String password, CompletableFuture<Void> authFuture,
                          PendingCommandQueue pendingQueue, PendingCommandQueue apiQueue,
                          BgapiJobTracker bgapiTracker, EventBus eventBus) {
        this.password     = password;
        this.authFuture   = authFuture;
        this.pendingQueue = pendingQueue;
        this.apiQueue     = apiQueue;
        this.bgapiTracker = bgapiTracker;
        this.eventBus     = eventBus;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        remote = ctx.channel().remoteAddress().toString();
        MDC.put("eslChannel", remote);
        log.info("[CONNECT] {}", remote);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EslMessage message) {
        String ct = message.getContentType();
        if (ct == null) {
            log.warn("[RECV] No Content-Type in frame — ignoring");
            return;
        }
        switch (ct) {
            case EslHeaders.CT_AUTH_REQUEST      -> handleAuthRequest(ctx);
            case EslHeaders.CT_COMMAND_REPLY     -> handleCommandReply(ctx, message);
            case EslHeaders.CT_API_RESPONSE      -> handleApiResponse(message);
            case EslHeaders.CT_EVENT_PLAIN       -> dispatchEvent(EslEvent.fromPlainMessage(message));
            case EslHeaders.CT_EVENT_JSON        -> handleEventJson(message);
            case EslHeaders.CT_EVENT_XML         -> handleEventXml(message);
            case EslHeaders.CT_DISCONNECT_NOTICE -> { log.info("[DISCONNECT] Notice received"); ctx.close(); }
            default -> log.debug("[RECV] Unknown content-type [{}]", ct);
        }
    }

    private void handleAuthRequest(ChannelHandlerContext ctx) {
        log.debug("[AUTH] Sending password");
        ctx.writeAndFlush("auth " + password);
    }

    private void handleCommandReply(ChannelHandlerContext ctx, EslMessage message) {
        CommandReply reply = new CommandReply(message);

        if (!authenticated) {
            if (reply.isOk()) {
                authenticated = true;
                authFuture.complete(null);
                log.info("[AUTH] OK");
            } else {
                log.error("[AUTH] FAILED — reply=[{}]", reply.getReplyText());
                authFuture.completeExceptionally(
                        new SecurityException("FreeSWITCH auth failed: " + reply.getReplyText()));
                ctx.close();
            }
            return;
        }

        String jobUuid = reply.getJobUuid();
        if (jobUuid != null && !jobUuid.isBlank()) {
            log.debug("[BGAPI] Accepted — job=[{}]", jobUuid);
            pendingQueue.complete(reply);
            return;
        }

        if (!pendingQueue.complete(reply)) {
            log.warn("[CMD] Unexpected reply, no pending command — reply=[{}]", reply.getReplyText());
        }
    }

    private void handleApiResponse(EslMessage message) {
        CommandReply synthetic = new CommandReply(message);
        if (!apiQueue.complete(synthetic)) {
            log.warn("[API] Unexpected response, no pending API command — body=[{}]",
                    truncate(message.getBody(), 120));
        }
    }

    private void handleEventJson(EslMessage message) {
        try {
            dispatchEvent(EslEvent.fromJsonMessage(message));
        } catch (Exception e) {
            log.warn("[EVENT] JSON parse failed — {}", e.getMessage());
        }
    }

    private void handleEventXml(EslMessage message) {
        try {
            dispatchEvent(EslEvent.fromXmlMessage(message));
        } catch (Exception e) {
            log.warn("[EVENT] XML parse failed — {}", e.getMessage());
        }
    }

    private void dispatchEvent(EslEvent event) {
        String name = event.getEventName();
        String uuid = event.getUniqueId();

        if (EventName.BACKGROUND_JOB.wireValue().equals(name)) {
            String jobUuid = event.getHeader(EslHeaders.JOB_UUID);
            if (jobUuid == null) jobUuid = event.getHeader(EslHeaders.VARIABLE_JOB_UUID);
            if (jobUuid != null && bgapiTracker.complete(jobUuid, event.getEventBody())) {
                log.debug("[BGAPI] Result delivered — job=[{}]", jobUuid);
                return;
            }
        }

        if (uuid != null) {
            log.debug("[EVENT] [{}] uuid=[{}]", name, uuid);
        } else {
            log.debug("[EVENT] [{}]", name);
        }

        eventBus.publish(event);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("[DISCONNECT] Connection closed — {}", remote);
        RuntimeException ex = new IllegalStateException("ESL connection closed");
        pendingQueue.failAll(ex);
        apiQueue.failAll(ex);
        bgapiTracker.failAll(ex);
        if (!authFuture.isDone()) authFuture.completeExceptionally(ex);
        MDC.remove("eslChannel");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("[ERROR] Channel exception — {}", cause.getMessage(), cause);
        ctx.close();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "null";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
