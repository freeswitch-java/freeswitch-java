package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code global_getvar} API command — reads a global variable.
 *
 * <pre>{@code
 * ApiResponse r = client.api(new GlobalGetVarCommand("default_password")).join();
 * System.out.println(r.getBody());
 *
 * // Get all global variables
 * client.api(new GlobalGetVarCommand()).join();
 * }</pre>
 */
public final class GlobalGetVarCommand implements EslApiCommand {

    private final String name;

    /** Get all global variables. */
    public GlobalGetVarCommand() {
        this(null);
    }

    public GlobalGetVarCommand(String name) {
        this.name = name;
    }

    @Override
    public String toApiString() {
        return name != null ? "global_getvar " + name : "global_getvar";
    }
}
