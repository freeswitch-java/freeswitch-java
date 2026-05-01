package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_hold} / {@code uuid_hold off} API command.
 *
 * <pre>{@code
 * client.api(new HoldCommand("uuid-abc"));           // hold
 * client.api(new HoldCommand("uuid-abc").off());     // unhold
 * client.api(new HoldCommand("uuid-abc").toggle());  // toggle
 * }</pre>
 */
public final class HoldCommand implements EslApiCommand {

    private enum Mode { ON, OFF, TOGGLE }

    private final String uuid;
    private Mode mode = Mode.ON;

    public HoldCommand(String uuid) {
        this.uuid = uuid;
    }

    public HoldCommand off()    { this.mode = Mode.OFF;    return this; }
    public HoldCommand toggle() { this.mode = Mode.TOGGLE; return this; }

    @Override
    public String toApiString() {
        return switch (mode) {
            case ON     -> "uuid_hold " + uuid;
            case OFF    -> "uuid_hold off " + uuid;
            case TOGGLE -> "uuid_hold toggle " + uuid;
        };
    }
}
