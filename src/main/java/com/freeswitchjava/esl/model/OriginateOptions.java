package com.freeswitchjava.esl.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builder for the FreeSWITCH {@code originate} API command.
 *
 * <p>Originate syntax:
 * <pre>
 *   originate {var=val,var2=val2}[sofia/profile/destination] [exten|&app(args)]
 *             [dialplan] [context] [cid_name] [cid_num] [timeout_sec]
 * </pre>
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Simple originate to extension
 * String cmd = OriginateOptions.builder()
 *     .callUrl("sofia/default/1000@pbx.example.com")
 *     .extension("2000")
 *     .context("default")
 *     .callerIdName("Sales")
 *     .callerIdNumber("5551234")
 *     .timeout(30)
 *     .build()
 *     .toApiCommand();
 *
 * // Originate and execute an application
 * String cmd2 = OriginateOptions.builder()
 *     .callUrl("sofia/default/1000@pbx.example.com")
 *     .application("park")
 *     .variable("originate_timeout", "60")
 *     .variable("ignore_early_media", "true")
 *     .build()
 *     .toApiCommand();
 * }</pre>
 */
public final class OriginateOptions {

    private final String callUrl;
    private final String destination;      // extension number OR null if application used
    private final String application;      // &app(args) OR null if extension used
    private final String applicationArgs;
    private final String dialplan;
    private final String context;
    private final String callerIdName;
    private final String callerIdNumber;
    private final int timeoutSeconds;
    private final Map<String, String> variables;
    private final List<String> rawVariables;

    private OriginateOptions(Builder b) {
        this.callUrl        = b.callUrl;
        this.destination    = b.destination;
        this.application    = b.application;
        this.applicationArgs = b.applicationArgs;
        this.dialplan       = b.dialplan;
        this.context        = b.context;
        this.callerIdName   = b.callerIdName;
        this.callerIdNumber = b.callerIdNumber;
        this.timeoutSeconds = b.timeoutSeconds;
        this.variables      = Map.copyOf(b.variables);
        this.rawVariables   = List.copyOf(b.rawVariables);
    }

    /**
     * Produces the full {@code originate} command string ready to pass to {@code api originate}.
     */
    public String toApiCommand() {
        StringBuilder sb = new StringBuilder("originate ");

        // Channel variables in curly braces
        List<String> vars = new ArrayList<>(rawVariables);
        variables.forEach((k, v) -> vars.add(k + "=" + v));
        if (!vars.isEmpty()) {
            sb.append('{').append(String.join(",", vars)).append('}');
        }

        sb.append(callUrl).append(' ');

        // Destination: extension or &application(args)
        if (application != null) {
            sb.append('&').append(application);
            if (applicationArgs != null && !applicationArgs.isBlank()) {
                sb.append('(').append(applicationArgs).append(')');
            }
        } else {
            sb.append(Objects.requireNonNullElse(destination, ""));
        }

        // Optional positional args
        if (dialplan != null || context != null || callerIdName != null || callerIdNumber != null || timeoutSeconds > 0) {
            sb.append(' ').append(Objects.requireNonNullElse(dialplan, "XML"));
        }
        if (context != null || callerIdName != null || callerIdNumber != null || timeoutSeconds > 0) {
            sb.append(' ').append(Objects.requireNonNullElse(context, "default"));
        }
        if (callerIdName != null || callerIdNumber != null || timeoutSeconds > 0) {
            sb.append(' ').append(Objects.requireNonNullElse(callerIdName, ""));
        }
        if (callerIdNumber != null || timeoutSeconds > 0) {
            sb.append(' ').append(Objects.requireNonNullElse(callerIdNumber, ""));
        }
        if (timeoutSeconds > 0) {
            sb.append(' ').append(timeoutSeconds);
        }

        return sb.toString().trim();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String callUrl;
        private String destination;
        private String application;
        private String applicationArgs;
        private String dialplan;
        private String context;
        private String callerIdName;
        private String callerIdNumber;
        private int timeoutSeconds = 0;
        private final Map<String, String> variables = new LinkedHashMap<>();
        private final List<String> rawVariables = new ArrayList<>();

        /** The call URL, e.g. {@code sofia/default/1000@domain.com} or {@code user/1000}. */
        public Builder callUrl(String url) { this.callUrl = Objects.requireNonNull(url); return this; }

        /** Route the answered call to this extension in the dialplan. */
        public Builder extension(String ext) { this.destination = ext; return this; }

        /** Execute this application on answer instead of routing to dialplan. e.g. {@code "park"}, {@code "echo"}. */
        public Builder application(String app) { this.application = app; return this; }

        /** Arguments for the {@link #application(String)}. */
        public Builder applicationArgs(String args) { this.applicationArgs = args; return this; }

        public Builder dialplan(String dialplan) { this.dialplan = dialplan; return this; }
        public Builder context(String context) { this.context = context; return this; }
        public Builder callerIdName(String name) { this.callerIdName = name; return this; }
        public Builder callerIdNumber(String number) { this.callerIdNumber = number; return this; }

        /** How long to wait for answer before giving up (seconds). */
        public Builder timeout(int seconds) { this.timeoutSeconds = seconds; return this; }

        /** Set a channel variable for the outgoing channel. */
        public Builder variable(String name, String value) { this.variables.put(name, value); return this; }

        /** Set a raw variable string (allows expressions, e.g. {@code "^^:key=a:b:c"}). */
        public Builder rawVariable(String raw) { this.rawVariables.add(raw); return this; }

        // ── Common shorthand setters ───────────────────────────────────────────

        public Builder ignoreEarlyMedia(boolean v) { return variable("ignore_early_media", String.valueOf(v)); }
        public Builder returnRingReady(boolean v)  { return variable("return_ring_ready", String.valueOf(v)); }
        public Builder originateTimeout(int secs)  { return variable("originate_timeout", String.valueOf(secs)); }
        public Builder originateRetries(int n)     { return variable("originate_retries", String.valueOf(n)); }
        public Builder retryDelaySeconds(int secs) { return variable("originate_retry_sleep_ms", String.valueOf(secs * 1000)); }
        public Builder legTimeout(int secs)        { return variable("leg_timeout", String.valueOf(secs)); }
        public Builder sipAutoAnswer(boolean v)    { return variable("sip_auto_answer", String.valueOf(v)); }
        public Builder recordSession(String path)  { return variable("record_session", path); }
        public Builder hangupAfterBridge(boolean v){ return variable("hangup_after_bridge", String.valueOf(v)); }

        public OriginateOptions build() {
            Objects.requireNonNull(callUrl, "callUrl is required");
            if (destination == null && application == null) {
                throw new IllegalStateException("Either extension() or application() must be set");
            }
            return new OriginateOptions(this);
        }
    }
}
