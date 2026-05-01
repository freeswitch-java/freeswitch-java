package com.freeswitchjava.esl.outbound;

import java.net.InetSocketAddress;

/**
 * Immutable configuration for {@link OutboundServer}.
 */
public final class OutboundServerConfig {

    private final String bindAddress;
    private final int port;
    private final int bossThreads;
    private final int workerThreads;
    private final int connectTimeoutMs;
    private final int maxConcurrentSessions;

    private OutboundServerConfig(Builder builder) {
        this.bindAddress           = builder.bindAddress;
        this.port                  = builder.port;
        this.bossThreads           = builder.bossThreads;
        this.workerThreads         = builder.workerThreads;
        this.connectTimeoutMs      = builder.connectTimeoutMs;
        this.maxConcurrentSessions = builder.maxConcurrentSessions;
    }

    public String getBindAddress()       { return bindAddress; }
    public int getPort()                 { return port; }
    public int getBossThreads()          { return bossThreads; }
    public int getWorkerThreads()        { return workerThreads; }
    public int getConnectTimeoutMs()     { return connectTimeoutMs; }
    /** Maximum number of concurrently active sessions. {@code 0} = unlimited. */
    public int getMaxConcurrentSessions() { return maxConcurrentSessions; }

    public InetSocketAddress socketAddress() {
        return new InetSocketAddress(bindAddress, port);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String bindAddress = "0.0.0.0";
        private int port = 8084;
        private int bossThreads = 1;
        private int workerThreads = 0; // 0 = Netty default (2 * CPU cores)
        private int connectTimeoutMs = 30_000;
        private int maxConcurrentSessions = 0; // 0 = unlimited

        public Builder bindAddress(String bindAddress) {
            this.bindAddress = bindAddress;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder bossThreads(int n) {
            this.bossThreads = n;
            return this;
        }

        public Builder workerThreads(int n) {
            this.workerThreads = n;
            return this;
        }

        public Builder connectTimeoutMs(int ms) {
            this.connectTimeoutMs = ms;
            return this;
        }

        /**
         * Maximum number of simultaneously active outbound sessions.
         * When the limit is reached new connections are rejected with a hangup until capacity is freed.
         * Default is {@code 0} (unlimited).
         */
        public Builder maxConcurrentSessions(int max) {
            this.maxConcurrentSessions = max;
            return this;
        }

        public OutboundServerConfig build() {
            return new OutboundServerConfig(this);
        }
    }
}
