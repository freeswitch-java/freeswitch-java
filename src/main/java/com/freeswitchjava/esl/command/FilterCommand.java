package com.freeswitchjava.esl.command;

import java.util.Objects;

/**
 * Represents an ESL {@code filter} or {@code filter delete} command.
 *
 * <p>The {@code filter} command tells FreeSWITCH to send only events whose
 * specified header matches the given value. Multiple filters are additive (AND logic).
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Only receive events for a specific UUID
 * FilterCommand f = FilterCommand.add("Unique-ID", "abc-123-uuid");
 *
 * // Remove a specific filter
 * FilterCommand f2 = FilterCommand.delete("Unique-ID", "abc-123-uuid");
 *
 * // Combine: receive CHANNEL_ANSWER events only for a specific UUID
 * client.filter(FilterCommand.add("Event-Name", "CHANNEL_ANSWER"));
 * client.filter(FilterCommand.add("Unique-ID", "abc-123-uuid"));
 * }</pre>
 */
public final class FilterCommand {

    private final boolean delete;
    private final String header;
    private final String value;

    private FilterCommand(boolean delete, String header, String value) {
        this.delete = delete;
        this.header = Objects.requireNonNull(header);
        this.value  = Objects.requireNonNull(value);
    }

    /** Creates an {@code filter <header> <value>} command. */
    public static FilterCommand add(String header, String value) {
        return new FilterCommand(false, header, value);
    }

    /** Creates a {@code filter delete <header> <value>} command. */
    public static FilterCommand delete(String header, String value) {
        return new FilterCommand(true, header, value);
    }

    /** Produces the ESL command string (without trailing {@code \n\n}). */
    public String toCommand() {
        if (delete) {
            return "filter delete " + header + " " + value;
        }
        return "filter " + header + " " + value;
    }

    public boolean isDelete() { return delete; }
    public String getHeader() { return header; }
    public String getValue()  { return value; }
}
