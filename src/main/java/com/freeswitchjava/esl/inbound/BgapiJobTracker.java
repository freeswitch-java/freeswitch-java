package com.freeswitchjava.esl.inbound;

import com.freeswitchjava.esl.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Correlates {@code bgapi} job results with their pending {@link CompletableFuture}s.
 *
 * <p>When {@code bgapi} is sent, FreeSWITCH immediately replies with a {@code command/reply}
 * containing a {@code Job-UUID}. Later, a {@code BACKGROUND_JOB} event arrives with that
 * same UUID and the actual result. This class bridges those two events.
 */
public final class BgapiJobTracker {

    private static final Logger log = LoggerFactory.getLogger(BgapiJobTracker.class);

    private final ConcurrentHashMap<String, CompletableFuture<ApiResponse>> pending = new ConcurrentHashMap<>();

    /**
     * Registers a future to be completed when the job result arrives.
     */
    public void register(String jobUuid, CompletableFuture<ApiResponse> future) {
        pending.put(jobUuid, future);
    }

    /**
     * Completes the future associated with {@code jobUuid} with the given response body.
     *
     * @return true if a matching job was found, false if the UUID was unknown
     */
    public boolean complete(String jobUuid, String responseBody) {
        CompletableFuture<ApiResponse> future = pending.remove(jobUuid);
        if (future == null) {
            log.warn("Received BACKGROUND_JOB for unknown Job-UUID: {}", jobUuid);
            return false;
        }
        future.complete(ApiResponse.ofBody(responseBody));
        return true;
    }

    /**
     * Fails all pending futures — called on disconnect so callers don't hang indefinitely.
     */
    public void failAll(Throwable cause) {
        pending.forEach((uuid, future) -> future.completeExceptionally(cause));
        pending.clear();
    }
}
