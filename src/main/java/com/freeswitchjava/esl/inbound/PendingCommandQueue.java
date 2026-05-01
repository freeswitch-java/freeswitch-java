package com.freeswitchjava.esl.inbound;

import com.freeswitchjava.esl.model.CommandReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;

/**
 * Ordered queue of pending command futures.
 *
 * <p>ESL guarantees that {@code command/reply} messages arrive in the same order
 * as commands were sent. This queue exploits that guarantee: each command enqueues
 * a future; each received reply dequeues and completes the head future.
 *
 * <p><strong>Thread safety:</strong> All operations on this queue MUST be performed
 * on the Netty channel's event loop thread. There is no internal synchronization.
 * Callers are responsible for submitting tasks to the event loop.
 */
public final class PendingCommandQueue {

    private static final Logger log = LoggerFactory.getLogger(PendingCommandQueue.class);

    private final Deque<CompletableFuture<CommandReply>> queue = new ArrayDeque<>();

    /**
     * Enqueues a future that will be completed when the next {@code command/reply} arrives.
     * Must be called on the Netty event loop.
     */
    public void enqueue(CompletableFuture<CommandReply> future) {
        queue.addLast(future);
    }

    /**
     * Completes the oldest pending future with the given reply.
     * Must be called on the Netty event loop.
     *
     * @return true if a pending future was found, false if the queue was empty
     */
    public boolean complete(CommandReply reply) {
        CompletableFuture<CommandReply> future = queue.pollFirst();
        if (future == null) {
            log.warn("Received command/reply but no pending command in queue: {}", reply);
            return false;
        }
        future.complete(reply);
        return true;
    }

    /**
     * Fails all pending futures — called on disconnect.
     */
    public void failAll(Throwable cause) {
        CompletableFuture<CommandReply> future;
        while ((future = queue.pollFirst()) != null) {
            future.completeExceptionally(cause);
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}
