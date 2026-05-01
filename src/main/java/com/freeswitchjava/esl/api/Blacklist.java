package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Typed API for {@code mod_blacklist} — caller ID blacklisting.
 *
 * <p>Obtain via {@code client.blacklist()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Add a number to the blacklist
 * client.blacklist().add("default", "15551234567").join();
 *
 * // Check if a number is blacklisted
 * boolean blocked = "true".equals(
 *     client.blacklist().check("default", "15551234567").join().getBody().trim()
 * );
 *
 * // Remove from blacklist
 * client.blacklist().remove("default", "15551234567").join();
 * }</pre>
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Modules/mod_blacklist/">
 *     mod_blacklist documentation</a>
 */
public final class Blacklist {

    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public Blacklist(Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    /**
     * {@code blacklist add <list_name> <number>} — Add a number to a blacklist.
     *
     * @param listName blacklist name (e.g. {@code "default"})
     * @param number   caller ID number to block
     */
    public CompletableFuture<ApiResponse> add(String listName, String number) {
        return api("blacklist add " + listName + " " + number);
    }

    /**
     * {@code blacklist remove <list_name> <number>} — Remove a number from a blacklist.
     */
    public CompletableFuture<ApiResponse> remove(String listName, String number) {
        return api("blacklist remove " + listName + " " + number);
    }

    /**
     * {@code blacklist check <list_name> <number>} — Check if a number is blacklisted.
     * Response body is {@code "true"} or {@code "false"}.
     */
    public CompletableFuture<ApiResponse> check(String listName, String number) {
        return api("blacklist check " + listName + " " + number);
    }

    /**
     * {@code blacklist reload <list_name>} — Reload a blacklist from disk.
     */
    public CompletableFuture<ApiResponse> reload(String listName) {
        return api("blacklist reload " + listName);
    }

    /**
     * {@code blacklist flush <list_name>} — Remove all entries from a blacklist.
     */
    public CompletableFuture<ApiResponse> flush(String listName) {
        return api("blacklist flush " + listName);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private CompletableFuture<ApiResponse> api(String command) {
        return apiExecutor.apply(command);
    }
}
