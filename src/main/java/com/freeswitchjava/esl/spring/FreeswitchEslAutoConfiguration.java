package com.freeswitchjava.esl.spring;

import com.freeswitchjava.esl.event.EslEventListener;
import com.freeswitchjava.esl.event.EventName;
import com.freeswitchjava.esl.inbound.InboundClient;
import com.freeswitchjava.esl.inbound.InboundClientConfig;
import com.freeswitchjava.esl.live.LiveChannelManager;
import com.freeswitchjava.esl.outbound.OutboundServer;
import com.freeswitchjava.esl.outbound.OutboundServerConfig;
import com.freeswitchjava.esl.outbound.OutboundSessionRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Spring Boot auto-configuration for FreeSWITCH ESL.
 *
 * <p>Activated when {@code freeswitch.esl.host} is set in application properties.
 * Creates an {@link InboundClient} bean, connects on startup, subscribes to events,
 * and registers any {@link EslEventListener} beans found in the context.
 *
 * <h2>Minimal configuration</h2>
 * <pre>{@code
 * freeswitch:
 *   esl:
 *     host: freeswitch.example.com
 *     password: ClueCon
 * }</pre>
 *
 * <h2>Full configuration reference</h2>
 * <pre>{@code
 * freeswitch:
 *   esl:
 *     host: freeswitch.example.com
 *     port: 8021
 *     password: ClueCon
 *     connect-timeout-ms: 10000
 *     auto-reconnect: true
 *     reconnect-initial-delay-ms: 1000
 *     reconnect-max-delay-ms: 30000
 *     max-reconnect-attempts: 0
 *     restore-subscriptions: true
 *     ssl: false
 *     ssl-trust-all: false
 *     events: ALL
 *     live-channels: false
 *     outbound:
 *       enabled: false
 *       port: 8084
 *       bind-address: 0.0.0.0
 *       boss-threads: 1
 *       worker-threads: 0
 *       max-concurrent-sessions: 0
 * }</pre>
 */
@AutoConfiguration
@EnableConfigurationProperties(FreeswitchEslProperties.class)
@ConditionalOnProperty(prefix = "freeswitch.esl", name = "host")
public class FreeswitchEslAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FreeswitchEslAutoConfiguration.class);

    // ── InboundClient ─────────────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    public InboundClient freeswitchEslClient(FreeswitchEslProperties props) {
        InboundClientConfig config = InboundClientConfig.builder()
                .host(props.getHost())
                .port(props.getPort())
                .password(props.getPassword())
                .connectTimeoutMs(props.getConnectTimeoutMs())
                .autoReconnect(props.isAutoReconnect())
                .reconnectInitialDelayMs(props.getReconnectInitialDelayMs())
                .reconnectMaxDelayMs(props.getReconnectMaxDelayMs())
                .maxReconnectAttempts(props.getMaxReconnectAttempts())
                .restoreSubscriptions(props.isRestoreSubscriptions())
                .ssl(props.isSsl())
                .sslTrustAll(props.isSslTrustAll())
                .build();

        return InboundClient.create(config);
    }

    /**
     * Manages the InboundClient lifecycle — connects on app start, closes on app stop.
     * Also registers all {@link EslEventListener} beans and subscribes to configured events.
     */
    @Bean
    public SmartLifecycle freeswitchEslClientLifecycle(
            InboundClient client,
            FreeswitchEslProperties props,
            ObjectProvider<List<EslEventListener>> listenersProvider) {

        return new SmartLifecycle() {

            private volatile boolean running = false;

            @Override
            public void start() {
                try {
                    log.info("[ESL] Connecting to {}:{}", props.getHost(), props.getPort());
                    client.login(props.getConnectTimeoutMs(), TimeUnit.MILLISECONDS);

                    // Subscribe to configured events
                    String events = props.getEvents();
                    if (events != null && !events.isBlank()) {
                        if ("ALL".equalsIgnoreCase(events.trim())) {
                            client.subscribe(EventName.ALL).join();
                        } else {
                            EventName[] names = parseEventNames(events);
                            if (names.length > 0) client.subscribe(names).join();
                        }
                    }

                    // Register all EslEventListener beans in the context
                    List<EslEventListener> listeners = listenersProvider.getIfAvailable();
                    if (listeners != null) {
                        for (EslEventListener listener : listeners) {
                            client.addEventListener(listener);
                            log.debug("[ESL] Registered listener: {}", listener.getClass().getSimpleName());
                        }
                    }

                    running = true;
                    log.info("[ESL] Connected to FreeSWITCH at {}:{}", props.getHost(), props.getPort());

                } catch (Exception e) {
                    log.error("[ESL] Failed to connect to FreeSWITCH at {}:{} — {}",
                            props.getHost(), props.getPort(), e.getMessage());
                    throw new RuntimeException("FreeSWITCH ESL connection failed", e);
                }
            }

            @Override
            public void stop() {
                running = false;
                client.close();
                log.info("[ESL] Disconnected from FreeSWITCH");
            }

            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public int getPhase() {
                // Start after most beans, stop before them
                return Integer.MAX_VALUE - 100;
            }
        };
    }

    private EventName[] parseEventNames(String events) {
        String[] parts = events.split(",");
        return java.util.Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try { return EventName.valueOf(s); }
                    catch (IllegalArgumentException ex) {
                        log.warn("[ESL] Unknown event name '{}' in freeswitch.esl.events — skipping", s);
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toArray(EventName[]::new);
    }

    // ── Annotation-driven listener registration ───────────────────────────────

    @Bean
    public EslAnnotationBeanPostProcessor eslAnnotationBeanPostProcessor(InboundClient client) {
        return new EslAnnotationBeanPostProcessor(client);
    }

    // ── LiveChannelManager ────────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "freeswitch.esl", name = "live-channels", havingValue = "true")
    public LiveChannelManager freeswitchLiveChannelManager(InboundClient client) {
        return LiveChannelManager.attach(client);
    }

    // ── OutboundServer ────────────────────────────────────────────────────────

    @Configuration
    @ConditionalOnProperty(prefix = "freeswitch.esl.outbound", name = "enabled", havingValue = "true")
    static class OutboundServerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public OutboundServer freeswitchOutboundServer(
                FreeswitchEslProperties props,
                ObjectProvider<OutboundSessionRouter> routerProvider) {

            FreeswitchEslProperties.Outbound outboundProps = props.getOutbound();

            OutboundServerConfig config = OutboundServerConfig.builder()
                    .bindAddress(outboundProps.getBindAddress())
                    .port(outboundProps.getPort())
                    .bossThreads(outboundProps.getBossThreads())
                    .workerThreads(outboundProps.getWorkerThreads())
                    .maxConcurrentSessions(outboundProps.getMaxConcurrentSessions())
                    .build();

            OutboundSessionRouter router = routerProvider.getIfAvailable();
            if (router == null) {
                throw new IllegalStateException(
                        "freeswitch.esl.outbound.enabled=true but no OutboundSessionRouter bean found. " +
                        "Declare a bean of type OutboundSessionRouter or Consumer<OutboundSession> in your configuration.");
            }

            return OutboundServer.create(config, router);
        }

        @Bean
        public SmartLifecycle freeswitchOutboundServerLifecycle(OutboundServer outboundServer) {
            return new SmartLifecycle() {

                private volatile boolean running = false;

                @Override
                public void start() {
                    try {
                        outboundServer.start().join();
                        running = true;
                    } catch (Exception e) {
                        throw new RuntimeException("FreeSWITCH outbound server failed to start", e);
                    }
                }

                @Override
                public void stop() {
                    running = false;
                    outboundServer.close();
                }

                @Override public boolean isRunning() { return running; }

                @Override
                public int getPhase() {
                    return Integer.MAX_VALUE - 200; // start before inbound client lifecycle
                }
            };
        }
    }
}
