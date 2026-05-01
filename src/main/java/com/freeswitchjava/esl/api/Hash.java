package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Typed API for {@code mod_hash} — in-memory hash table operations.
 *
 * <p>Similar to {@link Db} but stored in memory (faster, not persisted across restarts).
 * Obtain via {@code client.hash()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * client.hash().insert("myapp", "active_calls", "3").join();
 * String val = client.hash().select("myapp", "active_calls").join().getBody();
 * client.hash().delete("myapp", "active_calls").join();
 * }</pre>
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Modules/mod_hash/">
 *     mod_hash documentation</a>
 */
public final class Hash {

    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public Hash(Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    /**
     * {@code hash insert/<realm>/<key>/<value>} — Insert or update a key-value pair.
     */
    public CompletableFuture<ApiResponse> insert(String realm, String key, String value) {
        return api("hash insert/" + realm + "/" + key + "/" + value);
    }

    /**
     * {@code hash delete/<realm>/<key>} — Delete a key.
     */
    public CompletableFuture<ApiResponse> delete(String realm, String key) {
        return api("hash delete/" + realm + "/" + key);
    }

    /**
     * {@code hash select/<realm>/<key>} — Retrieve a value.
     * Returns the value as the response body, or empty if not found.
     */
    public CompletableFuture<ApiResponse> select(String realm, String key) {
        return api("hash select/" + realm + "/" + key);
    }

    /**
     * {@code hash delete/<realm>} — Delete all keys in a realm.
     */
    public CompletableFuture<ApiResponse> deleteRealm(String realm) {
        return api("hash delete/" + realm);
    }

    /**
     * {@code hash show/<realm>} — List all key-value pairs in a realm.
     */
    public CompletableFuture<ApiResponse> show(String realm) {
        return api("hash show/" + realm);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private CompletableFuture<ApiResponse> api(String command) {
        return apiExecutor.apply(command);
    }
}
