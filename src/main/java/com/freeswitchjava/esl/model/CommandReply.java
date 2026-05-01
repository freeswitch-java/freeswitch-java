package com.freeswitchjava.esl.model;

import com.freeswitchjava.esl.codec.EslHeaders;
import com.freeswitchjava.esl.codec.EslMessage;

/**
 * Wraps a {@code command/reply} ESL message.
 */
public final class CommandReply {

    private final EslMessage message;

    public CommandReply(EslMessage message) {
        this.message = message;
    }

    /** @return true if reply text starts with {@code +OK} */
    public boolean isOk() {
        String replyText = getReplyText();
        return replyText != null && replyText.startsWith("+OK");
    }

    public String getReplyText() {
        return message.getHeader(EslHeaders.REPLY_TEXT);
    }

    /**
     * Returns the error message when {@link #isOk()} is {@code false},
     * stripping the leading {@code "-ERR "} prefix.
     * Returns {@code null} if the reply was successful.
     */
    public String getErrorMessage() {
        if (isOk()) return null;
        String replyText = getReplyText();
        if (replyText == null) return null;
        if (replyText.startsWith("-ERR ")) return replyText.substring(5);
        if (replyText.startsWith("-ERR"))  return replyText.substring(4);
        return replyText;
    }

    /** Job-UUID present when this is the immediate response to a bgapi command. */
    public String getJobUuid() {
        return message.getHeader(EslHeaders.JOB_UUID);
    }

    /**
     * Returns the {@code Application-Response} header value from a
     * {@code CHANNEL_EXECUTE_COMPLETE} event reply.
     *
     * <p>This is the result produced by dialplan apps that return a value, e.g.:
     * <ul>
     *   <li>{@code play_and_get_digits} — digits collected</li>
     *   <li>{@code read} — digits collected</li>
     *   <li>{@code bridge} — hangup cause of the B-leg</li>
     *   <li>{@code detected_speech} — recognition result JSON</li>
     * </ul>
     *
     * @return the application response string, or {@code null} if not present
     */
    public String getApplicationResponse() {
        return message.getHeader("Application-Response");
    }

    public EslMessage getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "CommandReply{replyText=" + getReplyText() + "}";
    }
}
