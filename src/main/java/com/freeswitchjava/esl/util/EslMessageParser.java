package com.freeswitchjava.esl.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility to parse ESL header blocks (key: value lines separated by {@code \n}).
 *
 * <p>FreeSWITCH URL-encodes the values of event variables (e.g. {@code variable_*} headers)
 * so this parser URL-decodes all values automatically.
 */
public final class EslMessageParser {

    private EslMessageParser() {}

    /**
     * Parses a raw ESL header block into a lowercase-keyed map.
     *
     * @param raw the raw header text (lines separated by {@code \n}, no trailing blank line)
     * @return mutable map of header name (lowercase) → decoded value
     */
    public static Map<String, String> parseHeaders(String raw) {
        Map<String, String> map = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return map;
        }
        for (String line : raw.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                String name = line.substring(0, colonIdx).trim().toLowerCase();
                String value = line.substring(colonIdx + 1).trim();
                map.put(name, urlDecode(value));
            }
        }
        return map;
    }

    /**
     * Splits an event body (plain format) into:
     * <ul>
     *   <li>index 0 — event headers section (everything before the blank line)</li>
     *   <li>index 1 — optional application body after the blank line (may be empty string)</li>
     * </ul>
     */
    public static String[] splitEventBody(String body) {
        if (body == null) return new String[]{"", ""};
        int blankLine = body.indexOf("\n\n");
        if (blankLine < 0) {
            return new String[]{body, ""};
        }
        return new String[]{
            body.substring(0, blankLine),
            body.substring(blankLine + 2)
        };
    }

    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value; // return as-is if decoding fails
        }
    }
}
