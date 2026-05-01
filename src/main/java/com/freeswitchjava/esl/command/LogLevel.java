package com.freeswitchjava.esl.command;

/**
 * Log levels for the ESL {@code log} command.
 *
 * <p>Sending {@code log <level>} enables log output to the ESL socket at the given level.
 * Send {@code nolog} to disable log output.
 */
public enum LogLevel {

    CONSOLE(0),
    ALERT(1),
    CRIT(2),
    ERR(3),
    WARNING(4),
    NOTICE(5),
    INFO(6),
    DEBUG(7);

    private final int value;

    LogLevel(int value) { this.value = value; }

    public int value() { return value; }

    /** Produces the ESL {@code log <level>} command string. */
    public String toCommand() {
        return "log " + name();
    }
}
