package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_flush_dtmf} API command — discards all queued DTMF digits on a channel.
 *
 * <pre>{@code
 * client.api(new FlushDtmfCommand("uuid-abc"));
 * }</pre>
 */
public final class FlushDtmfCommand implements EslApiCommand {

    private final String uuid;

    public FlushDtmfCommand(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toApiString() {
        return "uuid_flush_dtmf " + uuid;
    }
}
