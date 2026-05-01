package com.freeswitchjava.esl.outbound;

import com.freeswitchjava.esl.codec.EslFrameDecoder;
import com.freeswitchjava.esl.codec.EslMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * FreeSWITCH ESL outbound server.
 *
 * <p>Listens for incoming connections from FreeSWITCH (outbound socket mode).
 * Each new connection spawns an {@link OutboundSession} and passes it to the
 * provided session handler or {@link OutboundSessionRouter}.
 *
 * <h2>FreeSWITCH dialplan configuration</h2>
 * <pre>{@code
 * <action application="socket" data="192.168.1.10:8084 async full"/>
 * }</pre>
 *
 * <h2>Simple usage — single handler</h2>
 * <pre>{@code
 * OutboundServer server = OutboundServer.create(
 *     OutboundServerConfig.builder().port(8084).build(),
 *     session -> {
 *         session.connect().join();
 *         session.answer().join();
 *         session.playback("ivr/ivr-welcome.wav").join();
 *         session.hangup().join();
 *     });
 * server.startup(); // blocks until Ctrl+C
 * }</pre>
 *
 * <h2>Routed usage — multiple handlers by context</h2>
 * <pre>{@code
 * OutboundServer server = OutboundServer.create(
 *     OutboundServerConfig.builder().port(8084).maxConcurrentSessions(100).build(),
 *     OutboundSessionRouter.predicate(
 *         req -> "sales".equals(req.getContext()), salesHandler, defaultHandler));
 * server.startup();
 * }</pre>
 */
public final class OutboundServer implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(OutboundServer.class);

    private final OutboundServerConfig config;
    private final OutboundSessionRouter router;
    private final boolean autoConnect;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    private final AtomicBoolean closed   = new AtomicBoolean(false);
    private final CountDownLatch stopLatch = new CountDownLatch(1);

    // ── Constructors ──────────────────────────────────────────────────────────

    private OutboundServer(OutboundServerConfig config, OutboundSessionRouter router,
                           boolean autoConnect) {
        this.config      = config;
        this.router      = router;
        this.autoConnect = autoConnect;
    }

    /**
     * Creates a server that passes each session to a single handler.
     * The handler is responsible for calling {@link OutboundSession#connect()} itself.
     */
    public static OutboundServer create(OutboundServerConfig config,
                                        Consumer<OutboundSession> sessionHandler) {
        return new OutboundServer(config, new StaticRouter(sessionHandler), false);
    }

    /**
     * Creates a server with a router.
     * The server auto-connects each session before routing, so all channel variables
     * are available to the router via {@link OutboundSessionRequest}.
     */
    public static OutboundServer create(OutboundServerConfig config,
                                        OutboundSessionRouter router) {
        return new OutboundServer(config, router, true);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Binds the server, registers a JVM shutdown hook, then <strong>blocks</strong>
     * until {@link #shutdown()} is called or the process receives SIGTERM / Ctrl+C.
     *
     * <p>Use this as the last call in {@code main()}:
     * <pre>{@code
     * public static void main(String[] args) throws Exception {
     *     OutboundServer server = OutboundServer.create(config, handler);
     *     server.startup(); // blocks here
     * }
     * }</pre>
     *
     * @throws Exception if binding fails or the latch is interrupted
     */
    public void startup() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close, "esl-outbound-shutdown"));
        start().join();
        log.info("[OUTBOUND] Running — press Ctrl+C to stop");
        stopLatch.await();
    }

    /** Stops the server gracefully. Counterpart to {@link #startup()}. */
    public void shutdown() {
        close();
    }

    /**
     * Binds the server and starts accepting connections asynchronously.
     * Prefer {@link #startup()} for blocking use in {@code main()}.
     *
     * @return future that completes when the server is bound and ready
     */
    public CompletableFuture<Void> start() {
        int max = config.getMaxConcurrentSessions();
        Semaphore semaphore = max > 0 ? new Semaphore(max) : null;

        bossGroup   = new NioEventLoopGroup(config.getBossThreads());
        workerGroup = new NioEventLoopGroup(config.getWorkerThreads());

        CompletableFuture<Void> bindFuture = new CompletableFuture<>();

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS))
                                .addLast(new EslFrameDecoder())
                                .addLast(new EslMessageEncoder())
                                .addLast(new OutboundHandler(router, semaphore, autoConnect));
                    }
                });

        bootstrap.bind(config.socketAddress()).addListener(f -> {
            if (f.isSuccess()) {
                serverChannel = ((io.netty.channel.ChannelFuture) f).channel();
                log.info("[OUTBOUND] [START] Listening on [{}]{}",
                        config.socketAddress(),
                        max > 0 ? " maxSessions=[" + max + "]" : "");
                bindFuture.complete(null);
            } else {
                log.error("[OUTBOUND] [START] Bind FAILED — addr=[{}] cause=[{}]",
                        config.socketAddress(), f.cause().getMessage());
                bindFuture.completeExceptionally(f.cause());
            }
        });

        return bindFuture;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
            }
            if (bossGroup != null)   bossGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
            if (workerGroup != null) workerGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
            log.info("[OUTBOUND] [CLOSE] Stopped — addr=[{}]", config.socketAddress());
            stopLatch.countDown();
        }
    }
}
