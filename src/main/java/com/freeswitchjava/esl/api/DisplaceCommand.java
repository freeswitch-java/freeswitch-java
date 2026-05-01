package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_displace} API command — replaces the audio stream with a file.
 *
 * <p>Unlike {@code uuid_broadcast}, this replaces (displaces) the existing audio rather
 * than overlaying it. The {@code mux} flag mixes the file with the original audio instead.
 *
 * <pre>{@code
 * client.api(new DisplaceCommand("uuid-abc", "/tmp/hold.wav").start());
 * client.api(new DisplaceCommand("uuid-abc", "/tmp/hold.wav").start().mux().limit(60));
 * client.api(new DisplaceCommand("uuid-abc", "/tmp/hold.wav").stop());
 * }</pre>
 */
public final class DisplaceCommand implements EslApiCommand {

    private final String uuid;
    private final String file;
    private boolean start = true;
    private boolean mux   = false;
    private Integer limitSeconds;

    public DisplaceCommand(String uuid, String file) {
        this.uuid = uuid;
        this.file = file;
    }

    public DisplaceCommand start() { this.start = true;  return this; }
    public DisplaceCommand stop()  { this.start = false; return this; }

    /** Mix the file with the original audio instead of replacing it. */
    public DisplaceCommand mux() {
        this.mux = true;
        return this;
    }

    /** Maximum displacement duration in seconds. */
    public DisplaceCommand limit(int seconds) {
        this.limitSeconds = seconds;
        return this;
    }

    @Override
    public String toApiString() {
        if (!start) {
            return "uuid_displace " + uuid + " stop " + file;
        }
        StringBuilder sb = new StringBuilder("uuid_displace ")
                .append(uuid).append(" start ").append(file);
        if (limitSeconds != null) sb.append(" ").append(limitSeconds);
        if (mux) sb.append(" mux");
        return sb.toString();
    }
}
