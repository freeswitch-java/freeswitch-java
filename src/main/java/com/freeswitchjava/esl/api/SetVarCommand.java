package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_setvar} / {@code uuid_setvar_multi} API command.
 *
 * <pre>{@code
 * client.api(new SetVarCommand("uuid-abc", "my_var", "hello"));
 * client.api(new SetVarCommand("uuid-abc", "my_var", null));   // unset
 * }</pre>
 */
public final class SetVarCommand implements EslApiCommand {

    private final String uuid;
    private final String name;
    private final String value;

    /**
     * @param value the value to set, or {@code null} to unset the variable
     */
    public SetVarCommand(String uuid, String name, String value) {
        this.uuid  = uuid;
        this.name  = name;
        this.value = value;
    }

    @Override
    public String toApiString() {
        if (value == null) {
            return "uuid_setvar " + uuid + " " + name;
        }
        return "uuid_setvar " + uuid + " " + name + " " + value;
    }
}
