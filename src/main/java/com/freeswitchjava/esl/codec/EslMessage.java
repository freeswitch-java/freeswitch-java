package com.freeswitchjava.esl.codec;

import java.util.Collections;
import java.util.Map;

/**
 * A single decoded ESL message: outer headers plus an optional body string.
 * Headers are stored lowercase for case-insensitive access.
 */
public final class EslMessage {

    private final Map<String, String> headers;
    private final String body; // null if no body

    public EslMessage(Map<String, String> headers, String body) {
        this.headers = Collections.unmodifiableMap(headers);
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    public boolean hasHeader(String name) {
        return headers.containsKey(name.toLowerCase());
    }

    public String getContentType() {
        return headers.get(EslHeaders.CONTENT_TYPE);
    }

    public boolean hasBody() {
        return body != null && !body.isEmpty();
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "EslMessage{contentType=" + getContentType()
                + ", headers=" + headers.size()
                + ", bodyLength=" + (body == null ? 0 : body.length()) + "}";
    }
}
