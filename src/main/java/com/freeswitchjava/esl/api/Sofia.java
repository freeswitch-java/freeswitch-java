package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Fluent API for FreeSWITCH {@code sofia} SIP module commands.
 *
 * <p>Obtain via {@code client.sofia()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * client.sofia().status().thenAccept(r -> System.out.println(r.getBody()));
 * client.sofia().profile("internal").rescan();
 * }</pre>
 */
public final class Sofia {

    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public Sofia(Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.apiExecutor = Objects.requireNonNull(apiExecutor);
    }

    /** {@code sofia status} — Overall Sofia status. */
    public CompletableFuture<ApiResponse> status() {
        return api("sofia status");
    }

    /** {@code sofia status profile <name>} — Status of a specific profile. */
    public CompletableFuture<ApiResponse> profileStatus(String profileName) {
        return api("sofia status profile " + profileName);
    }

    /** {@code sofia loglevel <category> <level>} — Set Sofia log level. */
    public CompletableFuture<ApiResponse> logLevel(String category, int level) {
        return api("sofia loglevel " + category + " " + level);
    }

    /** {@code sofia tracelevel <level>} — Set Sofia SIP trace level. */
    public CompletableFuture<ApiResponse> traceLevel(int level) {
        return api("sofia tracelevel " + level);
    }

    /** Returns a builder for profile-specific operations. */
    public Profile profile(String profileName) {
        return new Profile(profileName, apiExecutor);
    }

    /** Returns a builder for gateway-specific operations. */
    public Gateway gateway(String profileName, String gatewayName) {
        return new Gateway(profileName, gatewayName, apiExecutor);
    }

    private CompletableFuture<ApiResponse> api(String cmd) {
        return apiExecutor.apply(cmd);
    }

    // ─── Profile sub-API ─────────────────────────────────────────────────────

    public static final class Profile {
        private final String profile;
        private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

        private Profile(String profile, Function<String, CompletableFuture<ApiResponse>> exec) {
            this.profile     = Objects.requireNonNull(profile);
            this.apiExecutor = exec;
        }

        /** {@code sofia profile <name> start} */
        public CompletableFuture<ApiResponse> start() { return cmd("start"); }

        /** {@code sofia profile <name> stop} */
        public CompletableFuture<ApiResponse> stop() { return cmd("stop"); }

        /** {@code sofia profile <name> restart} */
        public CompletableFuture<ApiResponse> restart() { return cmd("restart"); }

        /** {@code sofia profile <name> rescan} — Reload config without restarting. */
        public CompletableFuture<ApiResponse> rescan() { return cmd("rescan"); }

        /** {@code sofia profile <name> flush_inbound_reg} — Flush inbound registrations. */
        public CompletableFuture<ApiResponse> flushInboundReg() { return cmd("flush_inbound_reg"); }

        /** {@code sofia profile <name> register <gateway>} — Trigger gateway registration. */
        public CompletableFuture<ApiResponse> register(String gateway) {
            return apiExecutor.apply("sofia profile " + profile + " register " + gateway);
        }

        /** {@code sofia profile <name> unregister <gateway>} */
        public CompletableFuture<ApiResponse> unregister(String gateway) {
            return apiExecutor.apply("sofia profile " + profile + " unregister " + gateway);
        }

        /** {@code sofia profile <name> killgw <gateway>} */
        public CompletableFuture<ApiResponse> killGateway(String gateway) {
            return apiExecutor.apply("sofia profile " + profile + " killgw " + gateway);
        }

        private CompletableFuture<ApiResponse> cmd(String action) {
            return apiExecutor.apply("sofia profile " + profile + " " + action);
        }
    }

    // ─── Gateway sub-API ─────────────────────────────────────────────────────

    public static final class Gateway {
        private final String profile;
        private final String gateway;
        private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

        private Gateway(String profile, String gateway,
                           Function<String, CompletableFuture<ApiResponse>> exec) {
            this.profile     = Objects.requireNonNull(profile);
            this.gateway     = Objects.requireNonNull(gateway);
            this.apiExecutor = exec;
        }

        /** {@code sofia profile <profile> register <gateway>} */
        public CompletableFuture<ApiResponse> register() {
            return apiExecutor.apply("sofia profile " + profile + " register " + gateway);
        }

        /** {@code sofia profile <profile> unregister <gateway>} */
        public CompletableFuture<ApiResponse> unregister() {
            return apiExecutor.apply("sofia profile " + profile + " unregister " + gateway);
        }

        /** {@code sofia profile <profile> killgw <gateway>} */
        public CompletableFuture<ApiResponse> kill() {
            return apiExecutor.apply("sofia profile " + profile + " killgw " + gateway);
        }

        /** {@code sofia status gateway <gateway>} */
        public CompletableFuture<ApiResponse> status() {
            return apiExecutor.apply("sofia status gateway " + gateway);
        }
    }
}
