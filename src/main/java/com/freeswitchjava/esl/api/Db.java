package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Typed API for {@code mod_db} — FreeSWITCH internal database operations.
 *
 * <p>Obtain via {@code client.db()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * client.db().insert("myapp", "session_count", "5").join();
 * String val = client.db().select("myapp", "session_count").join().getBody();
 * client.db().delete("myapp", "session_count").join();
 * }</pre>
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Modules/mod_db_6587544/">
 *     mod_db documentation</a>
 */
public final class Db {

    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public Db(Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    /**
     * {@code db insert/<realm>/<key>/<value>} — Insert or update a key-value pair.
     *
     * @param realm namespace (e.g. app name)
     * @param key   key string
     * @param value value to store
     */
    public CompletableFuture<ApiResponse> insert(String realm, String key, String value) {
        return api("db insert/" + realm + "/" + key + "/" + value);
    }

    /**
     * {@code db delete/<realm>/<key>} — Delete a key.
     */
    public CompletableFuture<ApiResponse> delete(String realm, String key) {
        return api("db delete/" + realm + "/" + key);
    }

    /**
     * {@code db select/<realm>/<key>} — Retrieve a value by key.
     * Returns the value as the response body, or empty if not found.
     */
    public CompletableFuture<ApiResponse> select(String realm, String key) {
        return api("db select/" + realm + "/" + key);
    }

    /**
     * {@code db exists/<realm>/<key>} — Check whether a key exists.
     * Response body is {@code "true"} or {@code "false"}.
     */
    public CompletableFuture<ApiResponse> exists(String realm, String key) {
        return api("db exists/" + realm + "/" + key);
    }

    /**
     * {@code db flush/<realm>} — Delete all keys in a realm.
     */
    public CompletableFuture<ApiResponse> flush(String realm) {
        return api("db flush/" + realm);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private CompletableFuture<ApiResponse> api(String command) {
        return apiExecutor.apply(command);
    }
}
