package com.freeswitchjava.esl.inbound;

/**
 * Lifecycle states of an {@link InboundClient} connection.
 */
public enum ConnectionState {

    /** Initial state — not yet connected. */
    DISCONNECTED,

    /** TCP connection is being established. */
    CONNECTING,

    /** TCP connected; waiting for auth/request and sending password. */
    AUTHENTICATING,

    /** Authenticated and ready to send commands. */
    AUTHENTICATED,

    /** Connection was lost; waiting before attempting reconnect. */
    RECONNECTING,

    /** Client has been closed ({@link InboundClient#close()} was called). */
    CLOSED
}
