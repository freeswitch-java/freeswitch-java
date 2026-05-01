package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Typed API for {@code mod_distributor} — round-robin / weighted call distribution.
 *
 * <p>Obtain via {@code client.distributor()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Get the next gateway from a distribution list
 * String gw = client.distributor().next("my_list").join().getBody();
 *
 * // Reload distributor configuration
 * client.distributor().reload().join();
 * }</pre>
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Modules/mod_distributor/">
 *     mod_distributor documentation</a>
 */
public final class Distributor {

    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public Distributor(Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    /**
     * {@code distributor <list_name>} — Return the next entry from a distribution list.
     * The returned body contains the selected gateway/destination.
     *
     * @param listName name of the distributor list defined in {@code distributor.conf.xml}
     */
    public CompletableFuture<ApiResponse> next(String listName) {
        return api("distributor " + listName);
    }

    /**
     * {@code distributor_ctl reload} — Reload distributor configuration.
     */
    public CompletableFuture<ApiResponse> reload() {
        return api("distributor_ctl reload");
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private CompletableFuture<ApiResponse> api(String command) {
        return apiExecutor.apply(command);
    }
}
