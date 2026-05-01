package com.freeswitchjava.esl.command;

import com.freeswitchjava.esl.model.HangupCause;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Builder for the {@code sendmsg} ESL command.
 *
 * <p>The {@code sendmsg} command controls an active FreeSWITCH channel. It supports
 * five call-command types, each produced by a dedicated factory method:
 *
 * <ul>
 *   <li>{@link #execute(String)} — run a dialplan application on the channel</li>
 *   <li>{@link #hangup(HangupCause)} — terminate the channel</li>
 *   <li>{@link #unicast(String, int, String, String)} — hook raw audio via UDP</li>
 *   <li>{@link #nomedia()} — switch channel to no-media (proxy) mode</li>
 *   <li>{@link #xferext(String, String)} — transfer with application attachment</li>
 * </ul>
 *
 * <p>Wire format emitted by {@link #toFrame()}:
 * <pre>
 *   sendmsg [uuid]
 *   call-command: execute
 *   execute-app-name: playback
 *   execute-app-arg: /tmp/welcome.wav
 * </pre>
 * (The ESL encoder appends the terminating {@code \n\n}.)
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Execute an application
 * SendMsg.execute("playback")
 *        .arg("/tmp/welcome.wav")
 *        .uuid("channel-uuid")
 *        .build();
 *
 * // Hangup with a specific cause
 * SendMsg.hangup(HangupCause.NORMAL_CLEARING)
 *        .uuid("channel-uuid")
 *        .build();
 * }</pre>
 */
public final class SendMsg {

    /** The call-command values supported by FreeSWITCH. */
    public enum CallCommand {
        EXECUTE("execute"),
        HANGUP("hangup"),
        UNICAST("unicast"),
        NOMEDIA("nomedia"),
        XFEREXT("xferext");

        private final String value;
        CallCommand(String v) { this.value = v; }
        public String value() { return value; }
    }

    private final String uuid;            // may be null (outbound mode: targets connected channel)
    private final String frame;           // the full serialized frame (without trailing \n\n)

    private SendMsg(String uuid, String frame) {
        this.uuid  = uuid;
        this.frame = frame;
    }

    /** UUID of the channel to target. Null in outbound mode. */
    public String getUuid() { return uuid; }

    /**
     * The complete sendmsg frame text, WITHOUT the trailing {@code \n\n}.
     * The ESL encoder is responsible for appending that.
     */
    public String toFrame() { return frame; }

    // ─── Factory methods ────────────────────────────────────────────────────

    /** Begins building an {@code execute} sendmsg to run {@code appName} on the channel. */
    public static ExecuteBuilder execute(String appName) {
        return new ExecuteBuilder(appName);
    }

    /** Begins building a {@code hangup} sendmsg. */
    public static HangupBuilder hangup(HangupCause cause) {
        return new HangupBuilder(cause);
    }

    /** Shorthand hangup with NORMAL_CLEARING. */
    public static HangupBuilder hangup() {
        return new HangupBuilder(HangupCause.NORMAL_CLEARING);
    }

    /** Begins building a {@code unicast} sendmsg to hook raw audio via a UDP socket. */
    public static UnicastBuilder unicast(String remoteHost, int remotePort,
                                         String transport, String flags) {
        return new UnicastBuilder(remoteHost, remotePort, transport, flags);
    }

    /** Produces a {@code nomedia} sendmsg (switch channel to proxy / no-media mode). */
    public static NoMediaBuilder nomedia() {
        return new NoMediaBuilder();
    }

    /** Begins building an {@code xferext} sendmsg. */
    public static XferExtBuilder xferext(String dialplan, String context) {
        return new XferExtBuilder(dialplan, context);
    }

    // ─── Builders ───────────────────────────────────────────────────────────

    public static final class ExecuteBuilder {
        private String uuid;
        private final String appName;
        private String appArg;
        private boolean async = false;
        private int loops = 1;
        private String eventUuid;       // correlates via Application-UUID on execute events
        private boolean eventLock = false;
        private String eventLockUuid;
        private String body;            // for large args via Content-Length

        private ExecuteBuilder(String appName) {
            this.appName = Objects.requireNonNull(appName);
        }

        /** Target channel UUID (omit for outbound-mode current channel). */
        public ExecuteBuilder uuid(String uuid)         { this.uuid = uuid; return this; }
        /** Application argument (short form, goes in execute-app-arg header). */
        public ExecuteBuilder arg(String arg)           { this.appArg = arg; return this; }
        /** Execute asynchronously without blocking the channel. */
        public ExecuteBuilder async(boolean async)      { this.async = async; return this; }
        /** Number of times to loop execution. */
        public ExecuteBuilder loops(int n)              { this.loops = n; return this; }
        /** Sets Event-UUID so execute events include Application-UUID for correlation. */
        public ExecuteBuilder eventUuid(String id)      { this.eventUuid = id; return this; }
        /** Enables/disables FreeSWITCH event-lock to force command ordering in async mode. */
        public ExecuteBuilder eventLock(boolean lock)   { this.eventLock = lock; return this; }
        /** Optional event-lock UUID scope used by FreeSWITCH for lock tracking. */
        public ExecuteBuilder eventLockUuid(String id)  { this.eventLockUuid = id; return this; }
        /**
         * Large application argument sent as body via Content-Length.
         * Use this instead of {@link #arg(String)} when the argument is multiline or very long.
         */
        public ExecuteBuilder body(String body)         { this.body = body; return this; }

        public SendMsg build() {
            StringBuilder sb = new StringBuilder();
            sb.append("sendmsg");
            if (uuid != null) sb.append(' ').append(uuid);
            sb.append('\n');
            sb.append("call-command: execute\n");
            sb.append("execute-app-name: ").append(appName).append('\n');
            if (body != null) {
                byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                sb.append("content-type: text/plain\n");
                sb.append("content-length: ").append(bodyBytes.length).append('\n');
                sb.append('\n');
                sb.append(body);
                return new SendMsg(uuid, sb.toString());
            }
            if (appArg != null && !appArg.isBlank()) {
                sb.append("execute-app-arg: ").append(appArg).append('\n');
            }
            if (loops > 1)       sb.append("loops: ").append(loops).append('\n');
            if (async)           sb.append("async: true\n");
            if (eventLock)       sb.append("event-lock: true\n");
            if (eventLockUuid != null) sb.append("event-lock-uuid: ").append(eventLockUuid).append('\n');
            if (eventUuid != null) sb.append("Event-UUID: ").append(eventUuid).append('\n');
            // strip trailing \n — the encoder will append \n\n
            String frame = sb.toString();
            if (frame.endsWith("\n")) frame = frame.substring(0, frame.length() - 1);
            return new SendMsg(uuid, frame);
        }
    }

    public static final class HangupBuilder {
        private String uuid;
        private final HangupCause cause;

        private HangupBuilder(HangupCause cause) {
            this.cause = Objects.requireNonNull(cause);
        }

        public HangupBuilder uuid(String uuid) { this.uuid = uuid; return this; }

        public SendMsg build() {
            StringBuilder sb = new StringBuilder();
            sb.append("sendmsg");
            if (uuid != null) sb.append(' ').append(uuid);
            sb.append('\n');
            sb.append("call-command: hangup\n");
            sb.append("hangup-cause: ").append(cause.name());
            return new SendMsg(uuid, sb.toString());
        }
    }

    public static final class UnicastBuilder {
        private String uuid;
        private final String remoteHost;
        private final int remotePort;
        private final String transport;   // "tcp" or "udp"
        private final String flags;       // e.g. "native" for raw audio
        private String localHost;
        private int localPort;

        private UnicastBuilder(String remoteHost, int remotePort, String transport, String flags) {
            this.remoteHost = Objects.requireNonNull(remoteHost);
            this.remotePort = remotePort;
            this.transport  = Objects.requireNonNull(transport);
            this.flags      = flags;
        }

        public UnicastBuilder uuid(String uuid)           { this.uuid = uuid; return this; }
        public UnicastBuilder localHost(String host)      { this.localHost = host; return this; }
        public UnicastBuilder localPort(int port)         { this.localPort = port; return this; }

        public SendMsg build() {
            StringBuilder sb = new StringBuilder();
            sb.append("sendmsg");
            if (uuid != null) sb.append(' ').append(uuid);
            sb.append('\n');
            sb.append("call-command: unicast\n");
            sb.append("local-ip: ").append(localHost != null ? localHost : "127.0.0.1").append('\n');
            sb.append("local-port: ").append(localPort > 0 ? localPort : 8025).append('\n');
            sb.append("remote-ip: ").append(remoteHost).append('\n');
            sb.append("remote-port: ").append(remotePort).append('\n');
            sb.append("transport: ").append(transport).append('\n');
            if (flags != null && !flags.isBlank()) sb.append("flags: ").append(flags);
            else sb.deleteCharAt(sb.length() - 1); // remove trailing \n
            return new SendMsg(uuid, sb.toString());
        }
    }

    public static final class NoMediaBuilder {
        private String uuid;
        public NoMediaBuilder uuid(String uuid) { this.uuid = uuid; return this; }

        public SendMsg build() {
            StringBuilder sb = new StringBuilder();
            sb.append("sendmsg");
            if (uuid != null) sb.append(' ').append(uuid);
            sb.append('\n');
            sb.append("call-command: nomedia");
            return new SendMsg(uuid, sb.toString());
        }
    }

    public static final class XferExtBuilder {
        private String uuid;
        private final String dialplan;
        private final String context;

        private XferExtBuilder(String dialplan, String context) {
            this.dialplan = Objects.requireNonNull(dialplan);
            this.context  = Objects.requireNonNull(context);
        }

        public XferExtBuilder uuid(String uuid) { this.uuid = uuid; return this; }

        public SendMsg build() {
            StringBuilder sb = new StringBuilder();
            sb.append("sendmsg");
            if (uuid != null) sb.append(' ').append(uuid);
            sb.append('\n');
            sb.append("call-command: xferext\n");
            sb.append("dialplan: ").append(dialplan).append('\n');
            sb.append("context: ").append(context);
            return new SendMsg(uuid, sb.toString());
        }
    }
}
