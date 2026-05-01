package com.freeswitchjava.esl.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * FreeSWITCH {@code originate} API command.
 *
 * <p>Originates an outbound call leg. Equivalent to running
 * {@code originate <call-url> <destination>} in {@code fs_cli}.
 *
 * <pre>{@code
 * // Originate and send to dialplan extension
 * client.api(new OriginateCommand("sofia/default/1001@domain.com")
 *     .extension("2000").context("default")
 *     .callerIdName("Support").callerIdNumber("5550100")
 *     .timeout(30));
 *
 * // Originate and execute an application inline
 * client.api(new OriginateCommand("sofia/default/1001@domain.com")
 *     .application("park"));
 *
 * // Originate with channel variables
 * client.api(new OriginateCommand("sofia/default/1001@domain.com")
 *     .variable("ignore_early_media", "true")
 *     .variable("originate_timeout", "60")
 *     .extension("2000").context("default"));
 * }</pre>
 */
public final class OriginateCommand implements EslApiCommand {

    private final String callUrl;
    private String extension;
    private String dialplan = "XML";
    private String context  = "default";
    private String application;
    private String appArg;
    private String callerIdName;
    private String callerIdNumber;
    private int    timeout = 60;
    private final Map<String, String> variables = new LinkedHashMap<>();

    public OriginateCommand(String callUrl) {
        this.callUrl = callUrl;
    }

    public OriginateCommand extension(String extension) {
        this.extension = extension;
        return this;
    }

    public OriginateCommand dialplan(String dialplan) {
        this.dialplan = dialplan;
        return this;
    }

    public OriginateCommand context(String context) {
        this.context = context;
        return this;
    }

    public OriginateCommand application(String application) {
        this.application = application;
        return this;
    }

    public OriginateCommand applicationArg(String arg) {
        this.appArg = arg;
        return this;
    }

    public OriginateCommand callerIdName(String name) {
        this.callerIdName = name;
        return this;
    }

    public OriginateCommand callerIdNumber(String number) {
        this.callerIdNumber = number;
        return this;
    }

    public OriginateCommand timeout(int seconds) {
        this.timeout = seconds;
        return this;
    }

    public OriginateCommand variable(String name, String value) {
        this.variables.put(name, value);
        return this;
    }

    @Override
    public String toApiString() {
        StringBuilder sb = new StringBuilder("originate ");

        // Channel variables + caller ID as {var=val,...} prefix
        Map<String, String> vars = new LinkedHashMap<>(variables);
        if (callerIdName   != null) vars.put("origination_caller_id_name",   callerIdName);
        if (callerIdNumber != null) vars.put("origination_caller_id_number", callerIdNumber);
        vars.put("originate_timeout", String.valueOf(timeout));

        if (!vars.isEmpty()) {
            StringJoiner sj = new StringJoiner(",", "{", "}");
            vars.forEach((k, v) -> sj.add(k + "=" + v));
            sb.append(sj);
        }

        sb.append(callUrl).append(" ");

        if (application != null) {
            sb.append("&").append(application);
            if (appArg != null) sb.append("(").append(appArg).append(")");
        } else {
            sb.append(extension != null ? extension : "park");
            sb.append(" ").append(dialplan);
            sb.append(" ").append(context);
        }

        return sb.toString();
    }
}
