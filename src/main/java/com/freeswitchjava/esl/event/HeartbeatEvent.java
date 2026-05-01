package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Periodic heartbeat from FreeSWITCH (every 20 seconds by default).
 * Event-Name: {@code HEARTBEAT}
 *
 * <p>Useful for connection health monitoring and session-count alerting.
 */
public final class HeartbeatEvent extends EslEvent {

    public HeartbeatEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** Current number of active sessions. */
    public String getSessionCount() {
        return getHeader("session-count");
    }

    /** Maximum sessions allowed (from configuration). */
    public String getMaxSessions() {
        return getHeader("max-sessions");
    }

    /** Current sessions per second. */
    public String getSessionsPerSecond() {
        return getHeader("session-per-sec");
    }

    /** Configured sessions-per-second limit. */
    public String getMaxSessionsPerSecond() {
        return getHeader("max-session-per-sec");
    }

    /** FreeSWITCH uptime in seconds. */
    public String getUptimeMs() {
        return getHeader("uptime-msec");
    }

    /** Idle CPU percentage. */
    public String getIdleCpu() {
        return getHeader("idle-cpu");
    }

    /** FreeSWITCH version string. */
    public String getFreeswitchVersion() {
        return getHeader("freeswitch-version");
    }
}
