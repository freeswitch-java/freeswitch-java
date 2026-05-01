package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code global_setvar} API command — sets a global variable on the FreeSWITCH instance.
 *
 * <pre>{@code
 * client.api(new GlobalSetVarCommand("default_password", "mysecret"));
 * }</pre>
 */
public final class GlobalSetVarCommand implements EslApiCommand {

    private final String name;
    private final String value;

    public GlobalSetVarCommand(String name, String value) {
        this.name  = name;
        this.value = value;
    }

    @Override
    public String toApiString() {
        return "global_setvar " + name + "=" + value;
    }
}
