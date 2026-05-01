package com.freeswitchjava.esl.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * FreeSWITCH {@code uuid_setvar_multi} API command — sets multiple channel variables atomically.
 *
 * <pre>{@code
 * client.api(new SetVarMultiCommand("uuid-abc")
 *     .set("record_stereo", "true")
 *     .set("execute_on_answer", "playback /tmp/welcome.wav")
 *     .set("originate_timeout", "30"));
 * }</pre>
 */
public final class SetVarMultiCommand implements EslApiCommand {

    private final String uuid;
    private final Map<String, String> vars = new LinkedHashMap<>();

    public SetVarMultiCommand(String uuid) {
        this.uuid = uuid;
    }

    public SetVarMultiCommand(String uuid, Map<String, String> vars) {
        this.uuid = uuid;
        this.vars.putAll(vars);
    }

    public SetVarMultiCommand set(String name, String value) {
        vars.put(name, value);
        return this;
    }

    @Override
    public String toApiString() {
        StringJoiner sj = new StringJoiner(";");
        vars.forEach((k, v) -> sj.add(k + "=" + v));
        return "uuid_setvar_multi " + uuid + " " + sj;
    }
}
