package com.freeswitchjava.esl.api;

/**
 * Enum of all {@code show} API sub-commands.
 *
 * <p>Pass to {@code client.show(ShowCommand.CHANNELS)} to list active channels, etc.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * client.show(ShowCommand.CHANNELS).thenAccept(r -> System.out.println(r.getBody()));
 * client.show(ShowCommand.CALLS, "json").thenAccept(r -> parseJson(r.getBody()));
 * }</pre>
 */
public enum ShowCommand implements EslApiCommand {

    CHANNELS("channels"),
    CALLS("calls"),
    BRIDGED_CALLS("bridged_calls"),
    DETAILED_CALLS("detailed_calls"),
    REGISTRATIONS("registrations"),
    CODECS("codec"),
    MODULES("modules"),
    ENDPOINTS("endpoint"),
    INTERFACES("interfaces"),
    DIALPLAN("dialplan"),
    API("api"),
    APPLICATION("application"),
    FILE("file"),
    TIMER("timer"),
    ALIASES("aliases"),
    NAT_MAP("nat_map");

    private final String value;

    ShowCommand(String value) { this.value = value; }

    /** Produces {@code show <resource>} command string. */
    public String toCommand() {
        return "show " + value;
    }

    /** Produces {@code show <resource> as <format>} — format: xml, json, delim. */
    public String toCommand(String format) {
        return "show " + value + " as " + format;
    }

    @Override
    public String toApiString() {
        return toCommand();
    }
}
