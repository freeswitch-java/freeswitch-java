package com.freeswitchjava.esl.api;

/**
 * Represents a FreeSWITCH API command that can be sent over ESL.
 *
 * <p>FreeSWITCH API commands are the same commands you run in {@code fs_cli}
 * or via the {@code api <command>} ESL instruction. Every typed command class
 * implements this interface and produces the correct API string.
 *
 * <p>Pass any implementation to
 * {@link com.freeswitchjava.esl.inbound.InboundClient#api(EslApiCommand)}:
 *
 * <pre>{@code
 * client.api(new OriginateCommand("sofia/default/1001@domain.com", "2000", "default"));
 * client.api(new HangupCommand("uuid-abc", HangupCause.NORMAL_CLEARING));
 * client.api(new TransferCommand("uuid-abc", "1002"));
 * client.api(new SetVarCommand("uuid-abc", "record_stereo", "true"));
 * client.api(new ReloadXmlCommand());
 *
 * // Anything not covered by a typed class:
 * client.api(new RawApiCommand("sofia status profile internal"));
 * }</pre>
 *
 * @see RawApiCommand
 * @see OriginateCommand
 * @see HangupCommand
 * @see TransferCommand
 */
public interface EslApiCommand {

    /**
     * Returns the FreeSWITCH API command string, e.g. {@code "uuid_kill abc-123 NORMAL_CLEARING"}.
     * This is passed verbatim as the argument to {@code api}.
     */
    String toApiString();
}
