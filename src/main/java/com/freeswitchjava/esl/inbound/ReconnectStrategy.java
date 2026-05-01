package com.freeswitchjava.esl.inbound;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Computes reconnect delays using exponential backoff with full jitter.
 *
 * <p>Algorithm:
 * <pre>
 *   cap   = maxDelayMs
 *   base  = initialDelayMs
 *   sleep = random(0, min(cap, base * 2^attempt))
 * </pre>
 * "Full jitter" (AWS recommendation) spreads load when many clients reconnect simultaneously.
 *
 * @see <a href="https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/">
 *     Exponential Backoff And Jitter</a>
 */
public final class ReconnectStrategy {

    private final long initialDelayMs;
    private final long maxDelayMs;
    private final int  maxAttempts;  // 0 = unlimited

    private int attempt = 0;

    public ReconnectStrategy(long initialDelayMs, long maxDelayMs, int maxAttempts) {
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs     = maxDelayMs;
        this.maxAttempts    = maxAttempts;
    }

    /**
     * @return true if another attempt should be made, false if max attempts exhausted
     */
    public boolean shouldRetry() {
        return maxAttempts == 0 || attempt < maxAttempts;
    }

    /**
     * Returns the delay in milliseconds for the next attempt, then increments the counter.
     */
    public long nextDelayMs() {
        long cap = Math.min(maxDelayMs, initialDelayMs * (1L << Math.min(attempt, 30)));
        long delay = ThreadLocalRandom.current().nextLong(0, cap + 1);
        attempt++;
        return delay;
    }

    /** Resets the attempt counter (call after a successful connection). */
    public void reset() {
        attempt = 0;
    }

    public int getAttempt() {
        return attempt;
    }
}
