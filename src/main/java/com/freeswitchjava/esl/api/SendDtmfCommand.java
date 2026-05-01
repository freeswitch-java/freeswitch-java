package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_send_dtmf} API command.
 *
 * <pre>{@code
 * client.api(new SendDtmfCommand("uuid-abc", "1234"));
 * client.api(new SendDtmfCommand("uuid-abc", "1234").durationMs(200));
 * }</pre>
 */
public final class SendDtmfCommand implements EslApiCommand {

    private final String uuid;
    private final String digits;
    private Integer durationMs;

    public SendDtmfCommand(String uuid, String digits) {
        this.uuid   = uuid;
        this.digits = digits;
    }

    public SendDtmfCommand durationMs(int ms) {
        this.durationMs = ms;
        return this;
    }

    @Override
    public String toApiString() {
        String cmd = "uuid_send_dtmf " + uuid + " " + digits;
        if (durationMs != null) cmd += "@" + durationMs;
        return cmd;
    }
}
