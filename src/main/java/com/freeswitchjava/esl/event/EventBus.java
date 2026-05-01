package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Thread-safe event dispatch bus.
 *
 * <p>Listeners are invoked on the provided {@link Executor} (not the Netty I/O thread)
 * to avoid blocking I/O processing. A single-threaded executor preserves event ordering.
 */
public final class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    /** Wildcard: matches every event name. */
    public static final String WILDCARD = "*";

    public record EventRegistration(String id, String eventName, Consumer<EslEvent> listener) {}

    private final CopyOnWriteArrayList<EventRegistration> registrations = new CopyOnWriteArrayList<>();
    private final Executor dispatchExecutor;

    public EventBus(Executor dispatchExecutor) {
        this.dispatchExecutor = dispatchExecutor;
    }

    /**
     * Registers a listener for a specific event name, or {@link #WILDCARD} for all events.
     *
     * @return a registration token that can be used to remove the listener
     */
    public EventRegistration register(String eventName, Consumer<EslEvent> listener) {
        EventRegistration reg = new EventRegistration(UUID.randomUUID().toString(), eventName, listener);
        registrations.add(reg);
        return reg;
    }

    public void unregister(EventRegistration registration) {
        registrations.remove(registration);
    }

    /**
     * Dispatches an event to all matching listeners asynchronously on the dispatch executor.
     */
    public void publish(EslEvent event) {
        String eventName = event.getEventName();
        List<EventRegistration> snapshot = List.copyOf(registrations);
        for (EventRegistration reg : snapshot) {
            if (WILDCARD.equals(reg.eventName()) || reg.eventName().equalsIgnoreCase(eventName)) {
                dispatchExecutor.execute(() -> {
                    try {
                        reg.listener().accept(event);
                    } catch (Exception e) {
                        log.error("[EVENTBUS] Listener threw exception — event=[{}] listener=[{}] cause=[{}]",
                                eventName, reg.id(), e.getMessage(), e);
                    }
                });
            }
        }
    }

    public void clear() {
        registrations.clear();
    }
}
