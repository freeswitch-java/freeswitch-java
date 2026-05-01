package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_recv_dtmf} API command — injects DTMF as if received from the remote end.
 *
 * <pre>{@code
 * client.api(new RecvDtmfCommand("uuid-abc", "1234"));
 * }</pre>
 */
public final class RecvDtmfCommand implements EslApiCommand {

    private final String uuid;
    private final String digits;

    public RecvDtmfCommand(String uuid, String digits) {
        this.uuid   = uuid;
        this.digits = digits;
    }

    @Override
    public String toApiString() {
        return "uuid_recv_dtmf " + uuid + " " + digits;
    }
}
