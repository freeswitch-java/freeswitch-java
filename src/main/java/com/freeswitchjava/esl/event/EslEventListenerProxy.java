package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wraps any {@link EslEventListener} in its own dedicated single thread with a bounded queue.
 *
 * <p>By default, the event bus dispatches all listeners sequentially on one shared thread.
 * If a single listener does slow work (database writes, HTTP calls, blocking joins), it
 * delays every other listener. Wrap that listener in a proxy to give it its own thread:
 *
 * <pre>{@code
 * EslEventListenerProxy proxy = new EslEventListenerProxy(new MySlowHandler(), 512);
 * client.addEventListener(proxy);
 *
 * // Later, on shutdown:
 * proxy.shutdown();
 * }</pre>
 *
 * <p>When the internal queue is full, new events are dropped and a warning is logged.
 * Monitor {@link #getQueueSize()} and {@link #getDroppedCount()} to tune {@code queueCapacity}.
 */
public final class EslEventListenerProxy implements EslEventListener {

    private static final Logger log = LoggerFactory.getLogger(EslEventListenerProxy.class);

    private final EslEventListener delegate;
    private final BlockingQueue<EslEvent> queue;
    private final Thread dispatchThread;
    private final AtomicLong droppedCount = new AtomicLong();

    private volatile boolean running = true;

    /**
     * @param delegate       the real listener to wrap
     * @param queueCapacity  maximum number of queued events before drops occur
     */
    public EslEventListenerProxy(EslEventListener delegate, int queueCapacity) {
        this.delegate = delegate;
        this.queue    = new LinkedBlockingQueue<>(queueCapacity);
        this.dispatchThread = Thread.ofVirtual()
                .name("esl-listener-proxy-" + delegate.getClass().getSimpleName())
                .start(this::processQueue);
    }

    /** Creates a proxy with a default queue capacity of 1024. */
    public EslEventListenerProxy(EslEventListener delegate) {
        this(delegate, 1024);
    }

    @Override
    public void onEslEvent(EslEvent event) {
        if (!queue.offer(event)) {
            droppedCount.incrementAndGet();
            log.warn("[PROXY] Queue full — dropped event=[{}] delegate=[{}] queueSize=[{}]",
                    event.getEventName(), delegate.getClass().getSimpleName(), queue.size());
        }
    }

    /** Current number of events waiting to be dispatched. */
    public int getQueueSize() {
        return queue.size();
    }

    /** Total number of events dropped due to a full queue since creation. */
    public long getDroppedCount() {
        return droppedCount.get();
    }

    /**
     * Stops the dispatch thread. Call this on application shutdown.
     * Events still in the queue are discarded.
     */
    public void shutdown() {
        running = false;
        dispatchThread.interrupt();
    }

    private void processQueue() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                EslEvent event = queue.take();
                try {
                    delegate.onEslEvent(event);
                } catch (Exception e) {
                    log.error("[PROXY] Delegate threw exception — event=[{}] delegate=[{}]",
                            event.getEventName(), delegate.getClass().getSimpleName(), e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
