package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_media} API command — forces media on or off for a channel.
 *
 * <p>When media is forced on, FreeSWITCH re-inserts itself into the RTP path.
 * When turned off, it attempts to go back to proxy/no-media mode.
 *
 * <pre>{@code
 * client.api(new MediaCommand("uuid-abc").on());   // force media through FS
 * client.api(new MediaCommand("uuid-abc").off());  // proxy / bypass FS media
 * }</pre>
 */
public final class MediaCommand implements EslApiCommand {

    private final String uuid;
    private boolean on = true;

    public MediaCommand(String uuid) {
        this.uuid = uuid;
    }

    public MediaCommand on()  { this.on = true;  return this; }
    public MediaCommand off() { this.on = false; return this; }

    @Override
    public String toApiString() {
        return on ? "uuid_media " + uuid : "uuid_media off " + uuid;
    }
}
