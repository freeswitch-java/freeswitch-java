package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_record} API command — starts or stops recording a channel.
 *
 * <pre>{@code
 * client.api(new RecordCommand("uuid-abc", "/tmp/call.wav").start());
 * client.api(new RecordCommand("uuid-abc", "/tmp/call.wav").stop());
 * client.api(new RecordCommand("uuid-abc", "/tmp/call.wav").start().limit(300)); // max 5 min
 * }</pre>
 */
public final class RecordCommand implements EslApiCommand {

    public enum Action { start, stop, mask, unmask }

    private final String uuid;
    private final String file;
    private Action action = Action.start;
    private Integer limitSeconds;

    public RecordCommand(String uuid, String file) {
        this.uuid = uuid;
        this.file = file;
    }

    public RecordCommand start()  { this.action = Action.start;  return this; }
    public RecordCommand stop()   { this.action = Action.stop;   return this; }
    public RecordCommand mask()   { this.action = Action.mask;   return this; }
    public RecordCommand unmask() { this.action = Action.unmask; return this; }

    /** Maximum recording duration in seconds (only applies to {@code start}). */
    public RecordCommand limit(int seconds) {
        this.limitSeconds = seconds;
        return this;
    }

    @Override
    public String toApiString() {
        StringBuilder sb = new StringBuilder("uuid_record ")
                .append(uuid).append(" ")
                .append(action.name()).append(" ")
                .append(file);
        if (action == Action.start && limitSeconds != null) {
            sb.append(" ").append(limitSeconds);
        }
        return sb.toString();
    }
}
