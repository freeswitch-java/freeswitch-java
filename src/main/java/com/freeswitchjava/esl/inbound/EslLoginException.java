package com.freeswitchjava.esl.inbound;

/**
 * Thrown by {@link InboundClient#login()} when the connection or authentication fails.
 *
 * <p>Common causes:
 * <ul>
 *   <li>FreeSWITCH is unreachable (wrong host/port, firewall)</li>
 *   <li>Wrong password (mod_event_socket rejects auth)</li>
 *   <li>Connection timed out</li>
 *   <li>Login was interrupted</li>
 * </ul>
 */
public class EslLoginException extends Exception {

    public EslLoginException(String message) {
        super(message);
    }

    public EslLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
