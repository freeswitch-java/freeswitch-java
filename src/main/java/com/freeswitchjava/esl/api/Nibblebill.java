package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Typed API for {@code mod_nibblebill} — per-call billing / credit deduction.
 *
 * <p>Obtain via {@code client.nibblebill()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * String uuid = "some-call-uuid";
 *
 * // Pause billing for a call
 * client.nibblebill().pause(uuid).join();
 *
 * // Resume billing
 * client.nibblebill().resume(uuid).join();
 *
 * // Check current balance
 * client.nibblebill().balance(uuid).join();
 * }</pre>
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Modules/mod_nibblebill/">
 *     mod_nibblebill documentation</a>
 */
public final class Nibblebill {

    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public Nibblebill(Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    /**
     * {@code nibblebill pause <uuid>} — Pause billing for a call.
     */
    public CompletableFuture<ApiResponse> pause(String uuid) {
        return api("nibblebill pause " + uuid);
    }

    /**
     * {@code nibblebill resume <uuid>} — Resume billing for a call.
     */
    public CompletableFuture<ApiResponse> resume(String uuid) {
        return api("nibblebill resume " + uuid);
    }

    /**
     * {@code nibblebill reset <uuid>} — Reset the nibble amount for a call.
     */
    public CompletableFuture<ApiResponse> reset(String uuid) {
        return api("nibblebill reset " + uuid);
    }

    /**
     * {@code nibblebill deduct <uuid> <amount>} — Manually deduct an amount from a call.
     *
     * @param uuid   call UUID
     * @param amount amount to deduct (e.g. {@code "0.05"})
     */
    public CompletableFuture<ApiResponse> deduct(String uuid, String amount) {
        return api("nibblebill deduct " + uuid + " " + amount);
    }

    /**
     * {@code nibblebill add <uuid> <amount>} — Add credit to a call.
     */
    public CompletableFuture<ApiResponse> add(String uuid, String amount) {
        return api("nibblebill add " + uuid + " " + amount);
    }

    /**
     * {@code nibblebill balance <uuid>} — Check current balance for a call.
     */
    public CompletableFuture<ApiResponse> balance(String uuid) {
        return api("nibblebill balance " + uuid);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private CompletableFuture<ApiResponse> api(String command) {
        return apiExecutor.apply(command);
    }
}
