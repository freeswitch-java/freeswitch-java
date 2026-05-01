package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code reloadxml} API command — reloads the XML configuration.
 *
 * <pre>{@code
 * client.api(new ReloadXmlCommand());
 * }</pre>
 */
public final class ReloadXmlCommand implements EslApiCommand {

    @Override
    public String toApiString() {
        return "reloadxml";
    }
}
