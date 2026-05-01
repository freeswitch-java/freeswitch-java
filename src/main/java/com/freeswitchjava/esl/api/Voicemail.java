package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Typed API for {@code mod_voicemail} management operations.
 *
 * <p>Obtain via {@code client.voicemail()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Check how many messages user 1001 has
 * client.voicemail().check("default", "1001").join();
 *
 * // Delete all messages for a user
 * client.voicemail().delete("default", "1001").join();
 * }</pre>
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Modules/mod_voicemail_6587522/">
 *     mod_voicemail documentation</a>
 */
public final class Voicemail {

    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public Voicemail(Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    /**
     * {@code voicemail check default <domain> <user>} — Check voicemail for a user.
     * Returns message counts.
     *
     * @param domain domain name (e.g. {@code "default"})
     * @param user   extension/user ID
     */
    public CompletableFuture<ApiResponse> check(String domain, String user) {
        return api("voicemail check default " + domain + " " + user);
    }

    /**
     * {@code voicemail delete <domain> <user> [<message_uuid>]} — Delete messages.
     * Omit {@code messageUuid} to delete all messages for the user.
     */
    public CompletableFuture<ApiResponse> delete(String domain, String user) {
        return api("voicemail delete " + domain + " " + user);
    }

    public CompletableFuture<ApiResponse> delete(String domain, String user, String messageUuid) {
        return api("voicemail delete " + domain + " " + user + " " + messageUuid);
    }

    /**
     * {@code voicemail list <domain> <user> [<folder>]} — List voicemail messages.
     *
     * @param folder optional folder name (e.g. {@code "inbox"}, {@code "saved"})
     */
    public CompletableFuture<ApiResponse> list(String domain, String user) {
        return api("voicemail list " + domain + " " + user);
    }

    public CompletableFuture<ApiResponse> list(String domain, String user, String folder) {
        return api("voicemail list " + domain + " " + user + " " + folder);
    }

    /**
     * {@code voicemail read <domain> <user> <message_uuid>} — Mark a message as read.
     */
    public CompletableFuture<ApiResponse> read(String domain, String user, String messageUuid) {
        return api("voicemail read " + domain + " " + user + " " + messageUuid);
    }

    /**
     * {@code voicemail count <domain> <user>} — Count messages for a user.
     * Returns counts in format {@code "new:saved:total"}.
     */
    public CompletableFuture<ApiResponse> count(String domain, String user) {
        return api("voicemail count " + domain + " " + user);
    }

    /**
     * {@code voicemail reload} — Reload voicemail configuration.
     */
    public CompletableFuture<ApiResponse> reload() {
        return api("voicemail reload");
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private CompletableFuture<ApiResponse> api(String command) {
        return apiExecutor.apply(command);
    }
}
