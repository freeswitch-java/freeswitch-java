package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_dump} API command — dumps all channel variables and info.
 *
 * <pre>{@code
 * client.api(new DumpCommand("uuid-abc"));
 * client.api(new DumpCommand("uuid-abc").format("json"));
 * client.api(new DumpCommand("uuid-abc").format("xml"));
 * }</pre>
 */
public final class DumpCommand implements EslApiCommand {

    private final String uuid;
    private String format;

    public DumpCommand(String uuid) {
        this.uuid = uuid;
    }

    /** Output format: {@code txt} (default), {@code xml}, {@code json}, {@code plain}. */
    public DumpCommand format(String format) {
        this.format = format;
        return this;
    }

    @Override
    public String toApiString() {
        return format != null ? "uuid_dump " + uuid + " " + format : "uuid_dump " + uuid;
    }
}
