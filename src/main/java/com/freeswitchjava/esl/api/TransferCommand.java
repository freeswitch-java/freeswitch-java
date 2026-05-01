package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_transfer} API command — transfers a channel to a new extension.
 *
 * <pre>{@code
 * client.api(new TransferCommand("uuid-abc", "1002"));
 * client.api(new TransferCommand("uuid-abc", "1002", "XML", "default"));
 * client.api(new TransferCommand("uuid-abc", "1002").leg("-bleg"));
 * }</pre>
 */
public final class TransferCommand implements EslApiCommand {

    private final String uuid;
    private final String extension;
    private String dialplan = "XML";
    private String context  = "default";
    private String leg;           // null, "-bleg", or "-both"

    public TransferCommand(String uuid, String extension) {
        this.uuid      = uuid;
        this.extension = extension;
    }

    public TransferCommand(String uuid, String extension, String dialplan, String context) {
        this.uuid      = uuid;
        this.extension = extension;
        this.dialplan  = dialplan;
        this.context   = context;
    }

    /** Transfer the other leg instead of this one ({@code -bleg}) or both ({@code -both}). */
    public TransferCommand leg(String leg) {
        this.leg = leg;
        return this;
    }

    @Override
    public String toApiString() {
        StringBuilder sb = new StringBuilder("uuid_transfer ").append(uuid);
        if (leg != null) sb.append(" ").append(leg);
        sb.append(" ").append(extension)
          .append(" ").append(dialplan)
          .append(" ").append(context);
        return sb.toString();
    }
}
