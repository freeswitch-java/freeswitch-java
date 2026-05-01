package com.freeswitchjava.esl.command;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Builder for the {@code sendevent} ESL command.
 *
 * <p>Injects a synthetic event into the FreeSWITCH event system. Useful for triggering
 * custom events, waking up waiting applications, or sending messages to modules.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * SendEvent event = SendEvent.of("CUSTOM")
 *     .header("Event-Subclass", "myapp::notification")
 *     .header("My-Header", "some-value")
 *     .body("optional body text")
 *     .build();
 *
 * client.sendEvent(event);
 * }</pre>
 */
public final class SendEvent {

    private final String eventName;
    private final Map<String, String> headers;
    private final String body;

    private SendEvent(Builder b) {
        this.eventName = b.eventName;
        this.headers   = Map.copyOf(b.headers);
        this.body      = b.body;
    }

    /**
     * Serializes to the ESL sendevent wire format (without the trailing {@code \n\n}).
     */
    public String toFrame() {
        StringBuilder sb = new StringBuilder();
        sb.append("sendevent ").append(eventName).append('\n');
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append('\n'));
        if (body != null && !body.isBlank()) {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            sb.append("content-length: ").append(bodyBytes.length).append('\n');
            sb.append('\n');
            sb.append(body);
            return sb.toString();
        }
        // strip trailing \n — encoder appends \n\n
        if (sb.charAt(sb.length() - 1) == '\n') sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public String getEventName() { return eventName; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }

    public static Builder of(String eventName) {
        return new Builder(eventName);
    }

    public static final class Builder {
        private final String eventName;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private String body;

        private Builder(String eventName) {
            this.eventName = Objects.requireNonNull(eventName);
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public SendEvent build() {
            return new SendEvent(this);
        }
    }
}
