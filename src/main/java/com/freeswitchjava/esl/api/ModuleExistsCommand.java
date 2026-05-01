package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code module_exists} API command — checks whether a module is loaded.
 *
 * <p>Returns {@code "true"} or {@code "false"}.
 *
 * <pre>{@code
 * boolean loaded = "true".equals(
 *     client.api(new ModuleExistsCommand("mod_callcenter")).join().getBody().trim());
 * }</pre>
 */
public final class ModuleExistsCommand implements EslApiCommand {

    private final String module;

    public ModuleExistsCommand(String module) {
        this.module = module;
    }

    @Override
    public String toApiString() {
        return "module_exists " + module;
    }
}
