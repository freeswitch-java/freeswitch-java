package com.freeswitchjava.esl.inbound;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.util.Objects;

/**
 * Immutable configuration for {@link InboundClient}.
 *
 * <h2>Reconnection</h2>
 * When {@code autoReconnect} is enabled, the client uses exponential backoff with jitter:
 * <pre>
 *   delay = min(initialDelay * 2^attempt, maxDelay) + random jitter
 * </pre>
 * Set {@code maxReconnectAttempts} to 0 for unlimited retries.
 */
public final class InboundClientConfig {

    private final String host;
    private final int port;
    private final String password;
    private final int connectTimeoutMs;

    private final boolean autoReconnect;
    private final long reconnectInitialDelayMs;
    private final long reconnectMaxDelayMs;
    private final int  maxReconnectAttempts;   // 0 = unlimited
    private final boolean restoreSubscriptions; // re-send event subscriptions after reconnect
    private final boolean ssl;
    private final boolean sslTrustAll;

    private InboundClientConfig(Builder builder) {
        this.host                    = builder.host;
        this.port                    = builder.port;
        this.password                = builder.password;
        this.connectTimeoutMs        = builder.connectTimeoutMs;
        this.autoReconnect           = builder.autoReconnect;
        this.reconnectInitialDelayMs = builder.reconnectInitialDelayMs;
        this.reconnectMaxDelayMs     = builder.reconnectMaxDelayMs;
        this.maxReconnectAttempts    = builder.maxReconnectAttempts;
        this.restoreSubscriptions    = builder.restoreSubscriptions;
        this.ssl                     = builder.ssl;
        this.sslTrustAll             = builder.sslTrustAll;
    }

    public String  getHost()                    { return host; }
    public int     getPort()                    { return port; }
    public String  getPassword()                { return password; }
    public int     getConnectTimeoutMs()        { return connectTimeoutMs; }
    public boolean isAutoReconnect()            { return autoReconnect; }
    public long    getReconnectInitialDelayMs() { return reconnectInitialDelayMs; }
    public long    getReconnectMaxDelayMs()     { return reconnectMaxDelayMs; }
    public int     getMaxReconnectAttempts()    { return maxReconnectAttempts; }
    public boolean isRestoreSubscriptions()     { return restoreSubscriptions; }
    public boolean isSsl()                      { return ssl; }
    public boolean isSslTrustAll()              { return sslTrustAll; }

    /**
     * Builds the Netty {@link SslContext} for this configuration.
     * Returns {@code null} if SSL is disabled.
     *
     * @throws javax.net.ssl.SSLException if the SSL context cannot be created
     */
    public SslContext buildSslContext() throws javax.net.ssl.SSLException {
        if (!ssl) return null;
        SslContextBuilder builder = SslContextBuilder.forClient();
        if (sslTrustAll) builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        return builder.build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String  host                    = "localhost";
        private int     port                    = 8021;
        private String  password                = "ClueCon";
        private int     connectTimeoutMs        = 10_000;
        private boolean autoReconnect           = false;
        private long    reconnectInitialDelayMs = 1_000;
        private long    reconnectMaxDelayMs     = 30_000;
        private int     maxReconnectAttempts    = 0;      // 0 = unlimited
        private boolean restoreSubscriptions    = true;
        private boolean ssl                     = false;
        private boolean sslTrustAll             = false;

        public Builder host(String host)                     { this.host = Objects.requireNonNull(host); return this; }
        public Builder port(int port)                        {
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Invalid port: " + port);
            this.port = port; return this;
        }
        public Builder password(String password)             { this.password = Objects.requireNonNull(password); return this; }
        public Builder connectTimeoutMs(int ms)              { this.connectTimeoutMs = ms; return this; }

        /** Enable automatic reconnection on disconnect. */
        public Builder autoReconnect(boolean v)              { this.autoReconnect = v; return this; }

        /** Initial delay before first reconnect attempt (default 1 s). */
        public Builder reconnectInitialDelayMs(long ms)      { this.reconnectInitialDelayMs = ms; return this; }

        /** Maximum delay between reconnect attempts (default 30 s). */
        public Builder reconnectMaxDelayMs(long ms)          { this.reconnectMaxDelayMs = ms; return this; }

        /**
         * Maximum number of consecutive reconnect attempts before giving up.
         * Set to {@code 0} for unlimited (default).
         */
        public Builder maxReconnectAttempts(int n)           { this.maxReconnectAttempts = n; return this; }

        /**
         * When {@code true} (default), re-sends the last {@code event plain ...} subscription
         * command automatically after each successful reconnect.
         */
        public Builder restoreSubscriptions(boolean v)       { this.restoreSubscriptions = v; return this; }

        /**
         * Enable TLS for the ESL connection. FreeSWITCH must have TLS enabled on
         * {@code event_socket.conf.xml} with a valid certificate.
         */
        public Builder ssl(boolean v)                        { this.ssl = v; return this; }

        /**
         * When {@code true}, skips TLS certificate validation.
         * Useful for FreeSWITCH instances using self-signed certificates.
         * Do <em>not</em> use in production environments.
         */
        public Builder sslTrustAll(boolean v)                { this.sslTrustAll = v; return this; }

        public InboundClientConfig build() {
            Objects.requireNonNull(host, "host");
            Objects.requireNonNull(password, "password");
            return new InboundClientConfig(this);
        }
    }
}
