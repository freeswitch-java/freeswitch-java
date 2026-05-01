package com.freeswitchjava.esl.model;

import com.freeswitchjava.esl.codec.EslMessage;

/**
 * Wraps an {@code api/response} or a {@code BACKGROUND_JOB} event result.
 */
public final class ApiResponse {

    private final String body;
    private final boolean success;

    public ApiResponse(String body) {
        this.body = body == null ? "" : body;
        // FreeSWITCH api responses start with +OK on success, -ERR on failure
        this.success = this.body.startsWith("+OK") || (!this.body.startsWith("-ERR") && !this.body.startsWith("-USAGE"));
    }

    public static ApiResponse of(EslMessage message) {
        return new ApiResponse(message.getBody());
    }

    public static ApiResponse ofBody(String body) {
        return new ApiResponse(body);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getBody() {
        return body;
    }

    /**
     * Returns the error text when {@link #isSuccess()} is {@code false},
     * stripping the leading {@code "-ERR "} or {@code "-USAGE "} prefix.
     * Returns {@code null} if the response was successful.
     */
    public String getErrorMessage() {
        if (success) return null;
        if (body.startsWith("-ERR "))   return body.substring(5).trim();
        if (body.startsWith("-ERR"))    return body.substring(4).trim();
        if (body.startsWith("-USAGE ")) return body.substring(7).trim();
        if (body.startsWith("-USAGE"))  return body.substring(6).trim();
        return body.trim();
    }

    @Override
    public String toString() {
        return "ApiResponse{body=" + body.substring(0, Math.min(body.length(), 120)) + "}";
    }
}
