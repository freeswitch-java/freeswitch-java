package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code reload} API command — reloads a specific module.
 *
 * <pre>{@code
 * client.api(new ReloadCommand("mod_sofia"));
 * client.api(new ReloadCommand("mod_dialplan_xml"));
 * }</pre>
 */
public final class ReloadCommand implements EslApiCommand {

    private final String module;

    public ReloadCommand(String module) {
        this.module = module;
    }

    @Override
    public String toApiString() {
        return "reload " + module;
    }
}
