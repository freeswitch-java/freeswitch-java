package com.freeswitchjava.esl.inbound;

import com.freeswitchjava.esl.api.Blacklist;
import com.freeswitchjava.esl.api.Callcenter;
import com.freeswitchjava.esl.api.Conference;
import com.freeswitchjava.esl.api.Db;
import com.freeswitchjava.esl.api.Distributor;
import com.freeswitchjava.esl.api.EslApiCommand;
import com.freeswitchjava.esl.api.FsCtlCommand;
import com.freeswitchjava.esl.api.Hash;
import com.freeswitchjava.esl.api.Nibblebill;
import com.freeswitchjava.esl.api.ShowCommand;
import com.freeswitchjava.esl.api.Sofia;
import com.freeswitchjava.esl.api.Uuid;
import com.freeswitchjava.esl.api.ValetParking;
import com.freeswitchjava.esl.api.Voicemail;
import com.freeswitchjava.esl.model.HangupCause;
import io.netty.handler.ssl.SslContext;
import com.freeswitchjava.esl.codec.EslFrameDecoder;
import com.freeswitchjava.esl.codec.EslMessageEncoder;
import com.freeswitchjava.esl.command.FilterCommand;
import com.freeswitchjava.esl.command.LogLevel;
import com.freeswitchjava.esl.command.SendEvent;
import com.freeswitchjava.esl.command.SendMsg;
import com.freeswitchjava.esl.event.EslEventListener;
import com.freeswitchjava.esl.event.EventBus;
import com.freeswitchjava.esl.event.EventName;
import com.freeswitchjava.esl.model.ApiResponse;
import com.freeswitchjava.esl.model.CommandReply;
import com.freeswitchjava.esl.model.EslEvent;
import com.freeswitchjava.esl.model.OriginateOptions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * FreeSWITCH ESL inbound client.
 *
 * <p>Connects to FreeSWITCH's mod_event_socket port (default 8021), authenticates,
 * and exposes the full ESL command set as a typed async API.
 *
 * <h2>Quick start</h2>
 * <pre>{@code
 * InboundClient client = InboundClient.create(
 *     InboundClientConfig.builder()
 *         .host("localhost").port(8021).password("ClueCon")
 *         .autoReconnect(true).build());
 *
 * client.connect().join();
 * client.subscribe(EventName.CHANNEL_ANSWER, EventName.CHANNEL_HANGUP).join();
 * client.addEventListener(EventName.CHANNEL_ANSWER, event ->
 *     System.out.println("Answered: " + event.getUniqueId()));
 *
 * // API calls
 * client.api("status").thenAccept(r -> System.out.println(r.getBody()));
 * client.bgapi("originate sofia/default/1001@domain.com &park()");
 *
 * // Typed sub-APIs
 * client.uuid("abc-123").hold();
 * client.conference("sales").mute(3);
 * client.sofia().profile("internal").rescan();
 * }</pre>
 */
public final class InboundClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(InboundClient.class);

    private final InboundClientConfig config;
    private final EventLoopGroup workerGroup;
    private final ScheduledExecutorService scheduler;

    volatile Channel channel; // package-private for testing
    private volatile PendingCommandQueue pendingQueue;
    private volatile PendingCommandQueue apiQueue;
    private volatile BgapiJobTracker bgapiTracker;
    private volatile EventBus eventBus;

    private volatile String freeswitchVersion;

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final CountDownLatch closeLatch = new CountDownLatch(1);
    private final ReconnectStrategy reconnectStrategy;

    private volatile ConnectionState state = ConnectionState.DISCONNECTED;
    private volatile java.util.function.Consumer<ConnectionState> onStateChange;

    // Tracks the last event subscription command so it can be restored after reconnect
    private volatile String lastSubscribeCommand;
    // Callback invoked after each successful reconnect (user can re-setup filters etc.)
    private volatile Runnable onReconnect;

    private InboundClient(InboundClientConfig config) {
        this.config = config;
        this.workerGroup = new NioEventLoopGroup(1);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "esl-scheduler");
            t.setDaemon(true);
            return t;
        });
        this.reconnectStrategy = new ReconnectStrategy(
                config.getReconnectInitialDelayMs(),
                config.getReconnectMaxDelayMs(),
                config.getMaxReconnectAttempts());
    }

    public static InboundClient create(InboundClientConfig config) {
        return new InboundClient(config);
    }

    /**
     * Connects, authenticates, registers a JVM shutdown hook, then <strong>blocks</strong>
     * until {@link #shutdown()} is called or the process receives SIGTERM / Ctrl+C.
     *
     * <p>This is the recommended entry point — configure the client first, then call
     * {@code startup()} as the last line of {@code main()}. Mirrors the pattern used
     * by asterisk-java's {@code AgiServer.startup()}.
     *
     * <pre>{@code
     * public static void main(String[] args) throws Exception {
     *     InboundClient client = InboundClient.create(
     *         InboundClientConfig.builder()
     *             .host("localhost").port(8021).password("ClueCon")
     *             .autoReconnect(true).build());
     *
     *     client.subscribe(EventName.CHANNEL_ANSWER, EventName.CHANNEL_HANGUP);
     *     client.addEventListener(new MyHandler());
     *     client.startup(); // ← blocks here; Ctrl+C shuts down cleanly
     * }
     * }</pre>
     *
     * @throws EslLoginException    if the initial connection or authentication fails
     * @throws InterruptedException if the blocking wait is interrupted
     */
    public void startup() throws EslLoginException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close, "esl-shutdown"));
        login();
        awaitClose();
    }

    /**
     * Shuts down this client gracefully. Equivalent to {@link #close()}, provided as
     * a named counterpart to {@link #startup()} for readability.
     */
    public void shutdown() {
        close();
    }

    // ── Connection state ──────────────────────────────────────────────────────

    /** Returns the current connection state. */
    public ConnectionState getState() {
        return state;
    }

    /** Returns the immutable client configuration used at creation time. */
    public InboundClientConfig getConfig() {
        return config;
    }

    /** Returns {@code true} if the client is fully authenticated and ready. */
    public boolean isConnected() {
        return state == ConnectionState.AUTHENTICATED;
    }

    /**
     * Registers a callback invoked on every state transition.
     * Called on the Netty event loop or scheduler thread — keep it fast.
     */
    public InboundClient onStateChange(java.util.function.Consumer<ConnectionState> callback) {
        this.onStateChange = callback;
        return this;
    }

    private void setState(ConnectionState newState) {
        ConnectionState prev = this.state;
        this.state = newState;
        log.debug("[STATE] {} → {}", prev, newState);
        java.util.function.Consumer<ConnectionState> cb = onStateChange;
        if (cb != null) {
            try { cb.accept(newState); } catch (Exception e) {
                log.error("[STATE] onStateChange callback failed — state=[{}]", newState, e);
            }
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Connects to FreeSWITCH, authenticates, and blocks until ready.
     *
     * <p>This is the recommended entry point for users. It wraps {@link #connect()}
     * with a clean blocking call and a meaningful exception on failure.
     *
     * <pre>{@code
     * InboundClient client = InboundClient.create(config);
     * client.login();  // blocks until authenticated or throws EslLoginException
     * client.subscribe(EventName.CHANNEL_ANSWER);
     * }</pre>
     *
     * @throws EslLoginException if connection or authentication fails
     */
    public void login() throws EslLoginException {
        login(config.getConnectTimeoutMs(), TimeUnit.MILLISECONDS);
    }

    /**
     * Connects and authenticates with an explicit timeout.
     *
     * @param timeout timeout value
     * @param unit    timeout unit
     * @throws EslLoginException if connection or authentication fails within the timeout
     */
    public void login(long timeout, TimeUnit unit) throws EslLoginException {
        try {
            connect().get(timeout, unit);
        } catch (java.util.concurrent.TimeoutException e) {
            throw new EslLoginException("Timed out connecting to FreeSWITCH at "
                    + config.getHost() + ":" + config.getPort(), e);
        } catch (java.util.concurrent.ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new EslLoginException("Failed to login to FreeSWITCH: " + cause.getMessage(), cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EslLoginException("Login interrupted", e);
        }
    }

    // ── Connection (async) ────────────────────────────────────────────────────

    /**
     * Connects to FreeSWITCH and authenticates asynchronously.
     * Prefer {@link #login()} for synchronous use.
     *
     * @return future that completes on successful authentication
     */
    public CompletableFuture<Void> connect() {
        setState(ConnectionState.CONNECTING);
        CompletableFuture<Void> authFuture = new CompletableFuture<>();

        pendingQueue = new PendingCommandQueue();
        apiQueue     = new PendingCommandQueue();
        bgapiTracker = new BgapiJobTracker();
        eventBus     = new EventBus(Executors.newSingleThreadExecutor(r -> new Thread(r, "esl-event-dispatch")));

        Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMs())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        if (config.isSsl()) {
                            SslContext sslCtx = config.buildSslContext();
                            ch.pipeline().addFirst(
                                    sslCtx.newHandler(ch.alloc(), config.getHost(), config.getPort()));
                        }
                        ch.pipeline()
                                .addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS))
                                .addLast(new EslFrameDecoder())
                                .addLast(new EslMessageEncoder())
                                .addLast(new InboundHandler(
                                        config.getPassword(), authFuture,
                                        pendingQueue, apiQueue, bgapiTracker, eventBus));
                    }
                });

        ChannelFuture connectFuture = bootstrap.connect(config.getHost(), config.getPort());
        connectFuture.addListener(cf -> {
            if (cf.isSuccess()) {
                channel = connectFuture.channel();
                log.info("[CONNECT] {}:{}", config.getHost(), config.getPort());
            } else {
                log.error("[CONNECT] FAILED — host=[{}:{}] cause=[{}]",
                        config.getHost(), config.getPort(), cf.cause().getMessage());
                authFuture.completeExceptionally(cf.cause());
            }
        });

        authFuture.thenRun(() -> setState(ConnectionState.AUTHENTICATED))
                  .exceptionally(ex -> { setState(ConnectionState.DISCONNECTED); return null; });

        if (config.isAutoReconnect()) {
            authFuture.thenRun(() -> {
                reconnectStrategy.reset();
                channel.closeFuture().addListener(cf -> {
                    if (!closed.get()) setState(ConnectionState.RECONNECTING);
                    scheduleReconnect();
                });
            });
        }
        return authFuture;
    }

    // ── Raw commands ─────────────────────────────────────────────────────────

    /**
     * Sends a raw ESL command string, e.g. {@code "noevents"}.
     *
     * @return future completed with the server's {@code command/reply}
     */
    public CompletableFuture<CommandReply> sendCommand(String command) {
        CompletableFuture<CommandReply> future = new CompletableFuture<>();
        channel.eventLoop().execute(() -> {
            pendingQueue.enqueue(future);
            channel.writeAndFlush(command);
        });
        return future;
    }

    // ── API commands ──────────────────────────────────────────────────────────

    /**
     * Executes a typed FreeSWITCH API command.
     *
     * <p>This is the primary way to send API commands. Each command is a plain Java object —
     * no string building, no typos:
     *
     * <pre>{@code
     * client.api(new OriginateCommand("sofia/default/1001@domain.com")
     *     .extension("2000").context("default").callerIdName("Support"));
     *
     * client.api(new HangupCommand("uuid-abc", HangupCause.USER_BUSY));
     * client.api(new TransferCommand("uuid-abc", "1002"));
     * client.api(new HoldCommand("uuid-abc"));
     * client.api(new PlaybackCommand("uuid-abc", "/tmp/welcome.wav"));
     * client.api(new RecordCommand("uuid-abc", "/tmp/call.wav").start());
     * client.api(new SetVarCommand("uuid-abc", "my_var", "hello"));
     * client.api(new ReloadXmlCommand());
     *
     * // Fallback for anything not covered by a typed class:
     * client.api(new RawApiCommand("sofia status profile internal"));
     * }</pre>
     *
     * @return future completed with the API response
     */
    public CompletableFuture<ApiResponse> api(EslApiCommand command) {
        return api(command.toApiString());
    }

    /**
     * Executes a raw FreeSWITCH API command string ({@code api <command>}).
     * Prefer the typed overload {@link #api(EslApiCommand)} where possible.
     *
     * @return future completed with the API response
     */
    public CompletableFuture<ApiResponse> api(String command) {
        CompletableFuture<CommandReply> replyFuture = new CompletableFuture<>();
        channel.eventLoop().execute(() -> {
            apiQueue.enqueue(replyFuture);
            channel.writeAndFlush("api " + command);
        });
        return replyFuture.thenApply(reply -> ApiResponse.of(reply.getMessage()));
    }

    /**
     * Executes a background API command ({@code bgapi <command>}).
     *
     * @return future completed when the BACKGROUND_JOB event arrives with the result
     */
    public CompletableFuture<ApiResponse> bgapi(String command) {
        return bgapi(command, null);
    }

    /**
     * Executes a background API command with an optional custom Job-UUID.
     *
     * @param jobUuid custom UUID to use for this job, or {@code null} to let FreeSWITCH generate one
     */
    public CompletableFuture<ApiResponse> bgapi(String command, String jobUuid) {
        CompletableFuture<ApiResponse> resultFuture = new CompletableFuture<>();
        CompletableFuture<CommandReply> replyFuture = new CompletableFuture<>();
        channel.eventLoop().execute(() -> {
            pendingQueue.enqueue(replyFuture);
            String cmd = "bgapi " + command;
            if (jobUuid != null) cmd += "\nJob-UUID: " + jobUuid;
            channel.writeAndFlush(cmd);
        });
        replyFuture.thenAccept(reply -> {
            String uuid = reply.getJobUuid();
            if (uuid == null || uuid.isBlank()) {
                resultFuture.completeExceptionally(
                        new IllegalStateException("bgapi reply missing Job-UUID: " + reply));
                return;
            }
            bgapiTracker.register(uuid, resultFuture);
        }).exceptionally(ex -> { resultFuture.completeExceptionally(ex); return null; });
        return resultFuture;
    }

    // ── Originate ─────────────────────────────────────────────────────────────

    /**
     * Convenience: originate using a typed {@link OriginateOptions} builder.
     */
    public CompletableFuture<ApiResponse> originate(OriginateOptions options) {
        return api(options.toApiCommand());
    }

    /**
     * Background originate — returns when the call leg is answered or fails.
     */
    public CompletableFuture<ApiResponse> bgOriginate(OriginateOptions options) {
        return bgapi(options.toApiCommand());
    }

    // ── sendmsg ───────────────────────────────────────────────────────────────

    /**
     * Sends a {@link SendMsg} frame to control an active channel.
     *
     * <pre>{@code
     * client.sendMsg(SendMsg.execute("playback").arg("/tmp/welcome.wav").uuid("abc-123").build());
     * client.sendMsg(SendMsg.hangup(HangupCause.NORMAL_CLEARING).uuid("abc-123").build());
     * }</pre>
     */
    public CompletableFuture<CommandReply> sendMsg(SendMsg msg) {
        return sendCommand(msg.toFrame());
    }

    // ── Event subscription ────────────────────────────────────────────────────

    /**
     * Subscribes to one or more events using the type-safe {@link EventName} enum.
     * The last subscription command is remembered and automatically re-sent after reconnect
     * when {@link InboundClientConfig.Builder#restoreSubscriptions(boolean)} is enabled.
     */
    public CompletableFuture<CommandReply> subscribe(EventName... eventNames) {
        String names = java.util.Arrays.stream(eventNames)
                .map(EventName::wireValue)
                .collect(java.util.stream.Collectors.joining(" "));
        String cmd = "event plain " + names;
        lastSubscribeCommand = cmd;
        return sendCommand(cmd);
    }

    /**
     * Subscribes to events in JSON format ({@code event json ...}).
     * Events will be delivered as {@code text/event-json} and parsed automatically.
     * Prefer plain format ({@link #subscribe}) unless you specifically need JSON.
     */
    public CompletableFuture<CommandReply> subscribeJson(EventName... eventNames) {
        String names = java.util.Arrays.stream(eventNames)
                .map(EventName::wireValue)
                .collect(java.util.stream.Collectors.joining(" "));
        String cmd = "event json " + names;
        lastSubscribeCommand = cmd;
        return sendCommand(cmd);
    }

    /**
     * Subscribes to events in XML format ({@code event xml ...}).
     * Events will be delivered as {@code text/event-xml} and parsed automatically.
     */
    public CompletableFuture<CommandReply> subscribeXml(EventName... eventNames) {
        String names = java.util.Arrays.stream(eventNames)
                .map(EventName::wireValue)
                .collect(java.util.stream.Collectors.joining(" "));
        String cmd = "event xml " + names;
        lastSubscribeCommand = cmd;
        return sendCommand(cmd);
    }

    /**
     * Subscribes to events by raw string name (e.g. custom subclass names like {@code "CUSTOM myapp::myevent"}).
     */
    public CompletableFuture<CommandReply> subscribeRaw(String... eventNames) {
        String cmd = "event plain " + String.join(" ", eventNames);
        lastSubscribeCommand = cmd;
        return sendCommand(cmd);
    }

    /** Unsubscribes from all events. */
    public CompletableFuture<CommandReply> noEvents() {
        return sendCommand("noevents");
    }

    /** Suppresses specific event types. */
    public CompletableFuture<CommandReply> nixEvent(EventName... eventNames) {
        String names = java.util.Arrays.stream(eventNames)
                .map(EventName::wireValue)
                .collect(java.util.stream.Collectors.joining(" "));
        return sendCommand("nixevent " + names);
    }

    /**
     * Locks this socket to receive events only for a specific channel UUID.
     * Equivalent to the {@code myevents} command.
     */
    public CompletableFuture<CommandReply> myEvents(String uuid) {
        return sendCommand("myevents plain " + uuid);
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    /**
     * Adds an event filter: only receive events where {@code header} matches {@code value}.
     * Multiple filters are additive (AND logic).
     */
    public CompletableFuture<CommandReply> filter(FilterCommand filterCommand) {
        return sendCommand(filterCommand.toCommand());
    }

    /** Shorthand for {@code FilterCommand.add(header, value)}. */
    public CompletableFuture<CommandReply> filter(String header, String value) {
        return sendCommand(FilterCommand.add(header, value).toCommand());
    }

    /** Removes a previously added filter. */
    public CompletableFuture<CommandReply> deleteFilter(String header, String value) {
        return sendCommand(FilterCommand.delete(header, value).toCommand());
    }

    // ── Logging ───────────────────────────────────────────────────────────────

    /** Enables log output at the specified level (delivered as LOG events). */
    public CompletableFuture<CommandReply> log(LogLevel level) {
        return sendCommand(level.toCommand());
    }

    /** Disables log output to this socket. */
    public CompletableFuture<CommandReply> nolog() {
        return sendCommand("nolog");
    }

    // ── Event injection ───────────────────────────────────────────────────────

    /**
     * Injects a synthetic event into FreeSWITCH using {@link SendEvent}.
     */
    public CompletableFuture<CommandReply> sendEvent(SendEvent event) {
        return sendCommand(event.toFrame());
    }

    // ── Misc ESL commands ─────────────────────────────────────────────────────

    /**
     * Enables {@code divert_events on}: route embedded script callbacks to this socket.
     */
    public CompletableFuture<CommandReply> divertEvents(boolean on) {
        return sendCommand("divert_events " + (on ? "on" : "off"));
    }

    /**
     * Sends {@code resume}: instructs FreeSWITCH to continue processing after a pause.
     */
    public CompletableFuture<CommandReply> resume() {
        return sendCommand("resume");
    }

    /**
     * Sends {@code linger}: keep the socket open after the channel hangs up.
     */
    public CompletableFuture<CommandReply> linger() {
        return sendCommand("linger");
    }

    /**
     * Sends {@code nolinger}: disable lingering behavior.
     */
    public CompletableFuture<CommandReply> noLinger() {
        return sendCommand("nolinger");
    }

    // ── Global variables ──────────────────────────────────────────────────────

    /** {@code api global_getvar [name]} — Get a global variable. */
    public CompletableFuture<ApiResponse> globalGetVar(String name) {
        return api("global_getvar" + (name != null ? " " + name : ""));
    }

    /** {@code api global_setvar name=value} — Set a global variable. */
    public CompletableFuture<ApiResponse> globalSetVar(String name, String value) {
        return api("global_setvar " + name + "=" + value);
    }

    // ── System commands ───────────────────────────────────────────────────────

    /** {@code api status} — FreeSWITCH system status. */
    public CompletableFuture<ApiResponse> status() {
        return api("status");
    }

    /** {@code api version} — FreeSWITCH version string. */
    public CompletableFuture<ApiResponse> version() {
        return api("version");
    }

    /** {@code api reloadxml} — Reload XML configuration. */
    public CompletableFuture<ApiResponse> reloadXml() {
        return api("reloadxml");
    }

    /** {@code api reload <module>} — Reload a specific module. */
    public CompletableFuture<ApiResponse> reload(String module) {
        return api("reload " + module);
    }

    /** {@code api hupall <cause>} — Hangup all active calls. */
    public CompletableFuture<ApiResponse> hupAll(String cause) {
        return api("hupall " + (cause != null ? cause : "NORMAL_CLEARING"));
    }

    /** {@code api create_uuid} — Generate a new UUID. */
    public CompletableFuture<ApiResponse> createUuid() {
        return api("create_uuid");
    }

    /**
     * Returns the FreeSWITCH version string (e.g. {@code "FreeSWITCH Version 1.10.11"}).
     * Result is cached after the first call.
     */
    public CompletableFuture<String> getFreeswitchVersion() {
        if (freeswitchVersion != null) {
            return CompletableFuture.completedFuture(freeswitchVersion);
        }
        return api("version").thenApply(r -> {
            freeswitchVersion = r.getBody().trim();
            return freeswitchVersion;
        });
    }

    /**
     * Returns the local socket address of this connection, or {@code null} if not connected.
     */
    public java.net.InetSocketAddress getLocalAddress() {
        return channel != null ? (java.net.InetSocketAddress) channel.localAddress() : null;
    }

    /**
     * Returns the remote socket address (FreeSWITCH), or {@code null} if not connected.
     */
    public java.net.InetSocketAddress getRemoteAddress() {
        return channel != null ? (java.net.InetSocketAddress) channel.remoteAddress() : null;
    }

    /**
     * Originates a call and delivers lifecycle events via {@link OriginateCallback}.
     *
     * <p>Tracks the new call leg by UUID and correlates subsequent CHANNEL_STATE and
     * CHANNEL_HANGUP events to the appropriate callback method.
     *
     * <pre>{@code
     * client.originateWithCallback(
     *     OriginateOptions.builder().dialString("sofia/default/1001@domain.com").build(),
     *     new OriginateCallback() {
     *         public void onAnswered(String uuid)              { log.info("Answered: {}", uuid); }
     *         public void onFailed(String uuid, HangupCause c) { log.warn("Failed: {}", c); }
     *     });
     * }</pre>
     */
    public void originateWithCallback(OriginateOptions options, OriginateCallback callback) {
        bgOriginate(options).thenAccept(response -> {
            if (!response.isSuccess()) {
                callback.onFailed(null, HangupCause.NORMAL_TEMPORARY_FAILURE);
                return;
            }
            // Extract UUID from "+OK <uuid>" response
            String body = response.getBody().trim();
            String uuid = body.startsWith("+OK ") ? body.substring(4).trim() : body;

            // Track ringing via CHANNEL_STATE
            EventBus.EventRegistration[] stateReg = new EventBus.EventRegistration[1];
            stateReg[0] = addEventListener(EventName.CHANNEL_STATE, event -> {
                if (!uuid.equals(event.getUniqueId())) return;
                String state = event.getChannelState();
                if ("CS_ROUTING".equalsIgnoreCase(state)) {
                    callback.onRinging(uuid);
                } else if ("CS_ACTIVE".equalsIgnoreCase(state)) {
                    callback.onAnswered(uuid);
                    removeEventListener(stateReg[0]);
                }
            });

            // Track hangup / final outcome
            addEventListener(EventName.CHANNEL_HANGUP, event -> {
                if (!uuid.equals(event.getUniqueId())) return;
                removeEventListener(stateReg[0]);
                HangupCause cause = HangupCause.fromName(event.getHangupCause());
                if (cause == null) cause = HangupCause.NORMAL_CLEARING;
                switch (cause) {
                    case USER_BUSY            -> callback.onBusy(uuid, cause);
                    case NO_ANSWER, NO_USER_RESPONSE -> callback.onNoAnswer(uuid);
                    case NORMAL_CLEARING      -> callback.onAnswered(uuid); // hangup after answer
                    default                   -> callback.onFailed(uuid, cause);
                }
            });
        }).exceptionally(ex -> {
            callback.onFailed(null, HangupCause.NORMAL_TEMPORARY_FAILURE);
            return null;
        });
    }

    /** Execute an {@link FsCtlCommand}. */
    public CompletableFuture<ApiResponse> fsctl(FsCtlCommand cmd) {
        return api(cmd.toCommand());
    }

    /** Execute a {@link ShowCommand}. */
    public CompletableFuture<ApiResponse> show(ShowCommand cmd) {
        return api(cmd.toCommand());
    }

    /** Execute a {@link ShowCommand} with a specific output format (xml, json, delim). */
    public CompletableFuture<ApiResponse> show(ShowCommand cmd, String format) {
        return api(cmd.toCommand(format));
    }

    // ── Sub-API accessors ─────────────────────────────────────────────────────

    /**
     * Returns a {@link Uuid} for controlling a specific channel.
     *
     * <pre>{@code
     * client.uuid("abc-123").hold();
     * client.uuid("abc-123").sendDtmf("1234");
     * }</pre>
     */
    public Uuid uuid(String channelUuid) {
        return new Uuid(channelUuid, this::api);
    }

    /**
     * Returns a {@link Conference} for controlling a conference room.
     *
     * <pre>{@code
     * client.conference("sales").mute(3);
     * client.conference("sales").play("/tmp/hold.wav");
     * }</pre>
     */
    public Conference conference(String room) {
        return new Conference(room, this::api);
    }

    /**
     * Returns a {@link Sofia} for SIP profile and gateway management.
     *
     * <pre>{@code
     * client.sofia().profile("internal").rescan();
     * }</pre>
     */
    public Sofia sofia() {
        return new Sofia(this::api);
    }

    /**
     * Returns a {@link Callcenter} for mod_callcenter queue management.
     *
     * <pre>{@code
     * client.callcenter().agentSetStatus("agent1@default", "Available");
     * client.callcenter().membersList("support_queue");
     * }</pre>
     */
    public Callcenter callcenter() {
        return new Callcenter(this::api);
    }

    /**
     * Returns a {@link Db} for mod_db persistent key-value storage.
     *
     * <pre>{@code
     * client.db().insert("myapp", "key", "value").join();
     * String val = client.db().select("myapp", "key").join().getBody();
     * }</pre>
     */
    public Db db() {
        return new Db(this::api);
    }

    /**
     * Returns a {@link Hash} for mod_hash in-memory key-value storage.
     *
     * <pre>{@code
     * client.hash().insert("myapp", "active_calls", "3").join();
     * String val = client.hash().select("myapp", "active_calls").join().getBody();
     * }</pre>
     */
    public Hash hash() {
        return new Hash(this::api);
    }

    /**
     * Returns a {@link Voicemail} for mod_voicemail management.
     *
     * <pre>{@code
     * client.voicemail().check("default", "1001").join();
     * client.voicemail().delete("default", "1001").join();
     * }</pre>
     */
    public Voicemail voicemail() {
        return new Voicemail(this::api);
    }

    /**
     * Returns a {@link ValetParking} for mod_valet_parking call parking.
     *
     * <pre>{@code
     * client.valet().list().join();
     * }</pre>
     */
    public ValetParking valet() {
        return new ValetParking(this::api);
    }

    /**
     * Returns a {@link Nibblebill} for mod_nibblebill per-call billing.
     *
     * <pre>{@code
     * client.nibblebill().pause(uuid).join();
     * client.nibblebill().balance(uuid).join();
     * }</pre>
     */
    public Nibblebill nibblebill() {
        return new Nibblebill(this::api);
    }

    /**
     * Returns a {@link Distributor} for mod_distributor round-robin call distribution.
     *
     * <pre>{@code
     * String gw = client.distributor().next("my_list").join().getBody();
     * }</pre>
     */
    public Distributor distributor() {
        return new Distributor(this::api);
    }

    /**
     * Returns a {@link Blacklist} for mod_blacklist caller ID blocking.
     *
     * <pre>{@code
     * client.blacklist().add("default", "15551234567").join();
     * boolean blocked = "true".equals(
     *     client.blacklist().check("default", "15551234567").join().getBody().trim()
     * );
     * }</pre>
     */
    public Blacklist blacklist() {
        return new Blacklist(this::api);
    }

    // ── Event listeners ───────────────────────────────────────────────────────

    /**
     * Registers an {@link EslEventListener} that receives every event from FreeSWITCH.
     *
     * <p>This is the primary way to listen for events. Implement the interface in your
     * own class and use {@code instanceof} pattern matching to handle specific event types:
     *
     * <pre>{@code
     * public class MyHandler implements EslEventListener {
     *     @Override
     *     public void onEslEvent(EslEvent event) {
     *         if (event instanceof ChannelAnswerEvent answer) {
     *             log.info("Answered: {}", answer.getCallerIdNumber());
     *         } else if (event instanceof ChannelHangupEvent hangup) {
     *             log.info("Hangup cause: {}", hangup.getHangupCauseEnum());
     *         } else if (event instanceof DtmfEvent dtmf) {
     *             log.info("DTMF digit: {}", dtmf.getDtmfDigit());
     *         }
     *     }
     * }
     *
     * client.addEventListener(new MyHandler());
     * }</pre>
     *
     * @return a registration token that can be passed to {@link #removeEventListener} to unregister
     */
    public EventBus.EventRegistration addEventListener(EslEventListener listener) {
        return eventBus.register(EventBus.WILDCARD, listener::onEslEvent);
    }

    /**
     * Registers a listener for a specific event name (type-safe enum overload).
     * Prefer {@link #addEventListener(EslEventListener)} for multi-event handling.
     *
     * @return a registration token for removal via {@link #removeEventListener}
     */
    public EventBus.EventRegistration addEventListener(EventName eventName, Consumer<EslEvent> listener) {
        return eventBus.register(eventName.wireValue(), listener);
    }

    /**
     * Registers a listener by raw event name string or {@link EventBus#WILDCARD} ({@code "*"}).
     *
     * @return a registration token for removal via {@link #removeEventListener}
     */
    public EventBus.EventRegistration addEventListener(String eventName, Consumer<EslEvent> listener) {
        return eventBus.register(eventName, listener);
    }

    public void removeEventListener(EventBus.EventRegistration registration) {
        eventBus.unregister(registration);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            setState(ConnectionState.CLOSED);
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush("exit").addListener(f -> channel.close());
            }
            scheduler.shutdown();
            workerGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
            log.info("[CLOSE] Client closed — host=[{}:{}]", config.getHost(), config.getPort());
            closeLatch.countDown();
        }
    }

    /**
     * Blocks the calling thread until {@link #close()} is called.
     *
     * <p>Call this at the end of your {@code main()} method so the JVM does not exit
     * while the client is running on background threads:
     *
     * <pre>{@code
     * client.login();
     * client.subscribe(EventName.ALL).join();
     * client.addEventListener(new MyHandler());
     *
     * client.awaitClose(); // blocks here — press Ctrl+C or call client.close() to stop
     * }</pre>
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void awaitClose() throws InterruptedException {
        closeLatch.await();
    }

    /**
     * Blocks the calling thread until {@link #close()} is called or the timeout elapses.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit for the timeout
     * @return {@code true} if the client was closed, {@code false} if the timeout elapsed first
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public boolean awaitClose(long timeout, TimeUnit unit) throws InterruptedException {
        return closeLatch.await(timeout, unit);
    }

    /**
     * Registers a callback invoked after each successful reconnect and re-authentication.
     * Use to re-apply filters or other per-connection setup that cannot be auto-restored.
     */
    public InboundClient onReconnect(Runnable callback) {
        this.onReconnect = callback;
        return this;
    }

    private void scheduleReconnect() {
        if (closed.get()) return;

        if (!reconnectStrategy.shouldRetry()) {
            log.error("[RECONNECT] GIVING UP — attempt=[{}] host=[{}:{}]",
                    reconnectStrategy.getAttempt(), config.getHost(), config.getPort());
            return;
        }

        long delay = reconnectStrategy.nextDelayMs();
        int max = config.getMaxReconnectAttempts();
        log.info("[RECONNECT] Attempt [#{}/{}] in [{}ms] — host=[{}:{}]",
                reconnectStrategy.getAttempt(),
                max == 0 ? "∞" : max,
                delay, config.getHost(), config.getPort());

        scheduler.schedule(() -> {
            if (closed.get()) return;
            connect()
                .thenRun(this::onReconnectSuccess)
                .exceptionally(ex -> {
                    log.warn("[RECONNECT] FAILED — attempt=[{}] cause=[{}]",
                            reconnectStrategy.getAttempt(), ex.getMessage());
                    scheduleReconnect();
                    return null;
                });
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void onReconnectSuccess() {
        setState(ConnectionState.AUTHENTICATED);
        log.info("[RECONNECT] OK — host=[{}:{}]", config.getHost(), config.getPort());

        if (config.isRestoreSubscriptions() && lastSubscribeCommand != null) {
            log.debug("[RECONNECT] Restoring subscription — cmd=[{}]", lastSubscribeCommand);
            sendCommand(lastSubscribeCommand)
                    .exceptionally(ex -> {
                        log.warn("[RECONNECT] Subscription restore failed — cause=[{}]", ex.getMessage());
                        return null;
                    });
        }

        Runnable hook = onReconnect;
        if (hook != null) {
            try {
                hook.run();
            } catch (Exception e) {
                log.error("[RECONNECT] onReconnect callback failed", e);
            }
        }

        channel.closeFuture().addListener(cf -> scheduleReconnect());
    }
}
