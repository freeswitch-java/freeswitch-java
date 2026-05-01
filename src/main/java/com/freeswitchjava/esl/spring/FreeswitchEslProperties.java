package com.freeswitchjava.esl.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the FreeSWITCH ESL inbound client.
 *
 * <pre>{@code
 * # application.yml
 * freeswitch:
 *   esl:
 *     host: freeswitch.example.com
 *     port: 8021
 *     password: ClueCon
 *     auto-reconnect: true
 *     events: ALL
 * }</pre>
 */
@ConfigurationProperties(prefix = "freeswitch.esl")
public class FreeswitchEslProperties {

    /** FreeSWITCH host (default: localhost). */
    private String host = "localhost";

    /** ESL port (default: 8021). */
    private int port = 8021;

    /** ESL password (default: ClueCon). */
    private String password = "ClueCon";

    /** Connection timeout in milliseconds (default: 10000). */
    private int connectTimeoutMs = 10_000;

    /** Enable automatic reconnection on disconnect (default: true). */
    private boolean autoReconnect = true;

    /** Initial reconnect delay in milliseconds (default: 1000). */
    private long reconnectInitialDelayMs = 1_000;

    /** Maximum reconnect delay in milliseconds (default: 30000). */
    private long reconnectMaxDelayMs = 30_000;

    /** Maximum reconnect attempts; 0 = unlimited (default: 0). */
    private int maxReconnectAttempts = 0;

    /** Re-send event subscriptions after each reconnect (default: true). */
    private boolean restoreSubscriptions = true;

    /** Enable TLS (default: false). */
    private boolean ssl = false;

    /**
     * Trust all TLS certificates without validation (default: false).
     * For development / self-signed certs only — do not use in production.
     */
    private boolean sslTrustAll = false;

    /**
     * FreeSWITCH event names to subscribe to on startup.
     * Comma-separated list or {@code ALL} (default: ALL).
     * Examples: {@code CHANNEL_ANSWER,CHANNEL_HANGUP,DTMF} or {@code ALL}.
     */
    private String events = "ALL";

    /** Enable {@link com.freeswitchjava.esl.live.LiveChannelManager} bean (default: false). */
    private boolean liveChannels = false;

    /** Outbound server configuration. */
    private Outbound outbound = new Outbound();

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getHost()                    { return host; }
    public void   setHost(String host)         { this.host = host; }

    public int    getPort()                    { return port; }
    public void   setPort(int port)            { this.port = port; }

    public String getPassword()                { return password; }
    public void   setPassword(String password) { this.password = password; }

    public int    getConnectTimeoutMs()                   { return connectTimeoutMs; }
    public void   setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public boolean isAutoReconnect()                      { return autoReconnect; }
    public void    setAutoReconnect(boolean autoReconnect) { this.autoReconnect = autoReconnect; }

    public long   getReconnectInitialDelayMs()                          { return reconnectInitialDelayMs; }
    public void   setReconnectInitialDelayMs(long reconnectInitialDelayMs) { this.reconnectInitialDelayMs = reconnectInitialDelayMs; }

    public long   getReconnectMaxDelayMs()                       { return reconnectMaxDelayMs; }
    public void   setReconnectMaxDelayMs(long reconnectMaxDelayMs) { this.reconnectMaxDelayMs = reconnectMaxDelayMs; }

    public int    getMaxReconnectAttempts()                        { return maxReconnectAttempts; }
    public void   setMaxReconnectAttempts(int maxReconnectAttempts) { this.maxReconnectAttempts = maxReconnectAttempts; }

    public boolean isRestoreSubscriptions()                           { return restoreSubscriptions; }
    public void    setRestoreSubscriptions(boolean restoreSubscriptions) { this.restoreSubscriptions = restoreSubscriptions; }

    public boolean isSsl()              { return ssl; }
    public void    setSsl(boolean ssl)  { this.ssl = ssl; }

    public boolean isSslTrustAll()                  { return sslTrustAll; }
    public void    setSslTrustAll(boolean sslTrustAll) { this.sslTrustAll = sslTrustAll; }

    public String  getEvents()               { return events; }
    public void    setEvents(String events)  { this.events = events; }

    public boolean isLiveChannels()                    { return liveChannels; }
    public void    setLiveChannels(boolean liveChannels) { this.liveChannels = liveChannels; }

    public Outbound getOutbound()                    { return outbound; }
    public void     setOutbound(Outbound outbound)   { this.outbound = outbound; }

    // ── Nested: outbound server ───────────────────────────────────────────────

    public static class Outbound {

        /** Enable the outbound server (default: false). */
        private boolean enabled = false;

        /** Bind address (default: 0.0.0.0). */
        private String bindAddress = "0.0.0.0";

        /** Port for FreeSWITCH to connect to (default: 8084). */
        private int port = 8084;

        /** Netty boss thread count (default: 1). */
        private int bossThreads = 1;

        /** Netty worker thread count; 0 = Netty default (2 × CPU) (default: 0). */
        private int workerThreads = 0;

        /** Max concurrent sessions; 0 = unlimited (default: 0). */
        private int maxConcurrentSessions = 0;

        public boolean isEnabled()                    { return enabled; }
        public void    setEnabled(boolean enabled)    { this.enabled = enabled; }

        public String  getBindAddress()                      { return bindAddress; }
        public void    setBindAddress(String bindAddress)    { this.bindAddress = bindAddress; }

        public int     getPort()                  { return port; }
        public void    setPort(int port)          { this.port = port; }

        public int     getBossThreads()                    { return bossThreads; }
        public void    setBossThreads(int bossThreads)     { this.bossThreads = bossThreads; }

        public int     getWorkerThreads()                      { return workerThreads; }
        public void    setWorkerThreads(int workerThreads)     { this.workerThreads = workerThreads; }

        public int     getMaxConcurrentSessions()                          { return maxConcurrentSessions; }
        public void    setMaxConcurrentSessions(int maxConcurrentSessions) { this.maxConcurrentSessions = maxConcurrentSessions; }
    }
}
