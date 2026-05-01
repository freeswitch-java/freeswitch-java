package com.freeswitchjava.esl.api;

/**
 * Represents a FreeSWITCH {@code fsctl} system-control command.
 *
 * <p>Use factory methods to construct commands and pass to {@code client.api(fsctl.toCommand())}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * client.api(FsCtlCommand.pause().toCommand());
 * client.api(FsCtlCommand.maxSessions(1000).toCommand());
 * client.api(FsCtlCommand.shutdown(ShutdownMode.ELEGANT).toCommand());
 * }</pre>
 */
public final class FsCtlCommand implements EslApiCommand {

    /** Shutdown mode options for {@link #shutdown(ShutdownMode)}. */
    public enum ShutdownMode {
        /** Shutdown immediately. */
        NOW,
        /** Wait for all active calls to complete. */
        ELEGANT,
        /** Wait for current call then shut down. */
        ASAP,
        /** Cancel a pending shutdown. */
        CANCEL,
        /** Restart instead of shutting down. */
        RESTART
    }

    private final String command;

    private FsCtlCommand(String command) {
        this.command = command;
    }

    public String toCommand() {
        return command;
    }

    @Override
    public String toApiString() {
        return command;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /** {@code fsctl shutdown [now|elegant|asap|cancel|restart]} */
    public static FsCtlCommand shutdown(ShutdownMode mode) {
        return new FsCtlCommand("fsctl shutdown " + mode.name().toLowerCase());
    }

    /** {@code fsctl pause} — Stop accepting new calls. */
    public static FsCtlCommand pause() {
        return new FsCtlCommand("fsctl pause");
    }

    /** {@code fsctl pause_check} — Check if system is paused. */
    public static FsCtlCommand pauseCheck() {
        return new FsCtlCommand("fsctl pause_check");
    }

    /** {@code fsctl resume} — Resume accepting calls after pause. */
    public static FsCtlCommand resume() {
        return new FsCtlCommand("fsctl resume");
    }

    /** {@code fsctl max_sessions <n>} — Set max concurrent sessions. */
    public static FsCtlCommand maxSessions(int n) {
        return new FsCtlCommand("fsctl max_sessions " + n);
    }

    /** {@code fsctl sps <n>} — Set sessions per second limit. */
    public static FsCtlCommand sessionsPerSecond(int n) {
        return new FsCtlCommand("fsctl sps " + n);
    }

    /** {@code fsctl hupall <cause>} — Hangup all active calls. */
    public static FsCtlCommand hupAll(String cause) {
        return new FsCtlCommand("fsctl hupall " + (cause != null ? cause : "NORMAL_CLEARING"));
    }

    /** {@code fsctl loglevel <level>} — Set log verbosity (0–7). */
    public static FsCtlCommand logLevel(int level) {
        return new FsCtlCommand("fsctl loglevel " + level);
    }

    /** {@code fsctl debug_level <level>} — Set debug verbosity. */
    public static FsCtlCommand debugLevel(int level) {
        return new FsCtlCommand("fsctl debug_level " + level);
    }

    /** {@code fsctl recover} — Recover sessions from crash. */
    public static FsCtlCommand recover() {
        return new FsCtlCommand("fsctl recover");
    }

    /** {@code fsctl send_sighup} — Reload log files (rotate). */
    public static FsCtlCommand sendSighup() {
        return new FsCtlCommand("fsctl send_sighup");
    }
}
