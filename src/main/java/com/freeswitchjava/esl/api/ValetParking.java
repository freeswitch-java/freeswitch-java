package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Typed API for {@code mod_valet_parking} — call parking operations.
 *
 * <p>Obtain via {@code client.valet()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Park a call in slot 101
 * client.valet().park("101").join();
 *
 * // Retrieve a parked call from slot 101
 * client.valet().retrieve("101").join();
 *
 * // List all parked calls
 * client.valet().list().join();
 * }</pre>
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Modules/mod_valet_parking_3966447/">
 *     mod_valet_parking documentation</a>
 */
public final class ValetParking {

    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public ValetParking(Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    /**
     * {@code valet_info} — List all parked calls across all lots.
     */
    public CompletableFuture<ApiResponse> list() {
        return api("valet_info");
    }

    /**
     * {@code valet_info <lot>} — List parked calls in a specific lot.
     *
     * @param lot parking lot name or number
     */
    public CompletableFuture<ApiResponse> list(String lot) {
        return api("valet_info " + lot);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private CompletableFuture<ApiResponse> api(String command) {
        return apiExecutor.apply(command);
    }
}
