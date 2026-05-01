package com.freeswitchjava.esl.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Holds a collected stream of {@link EslEvent}s returned by a command that produces
 * multiple events rather than a single response (e.g. {@code show channels}, {@code conference list}).
 *
 * <pre>{@code
 * ResponseEvents result = client.collectEvents(
 *     "conference sales list",
 *     EventName.CONFERENCE_DATA,
 *     EventName.CONFERENCE_DATA_END
 * ).join();
 *
 * List<ConferenceDataEvent> members = result.getEventsOfType(ConferenceDataEvent.class);
 * }</pre>
 */
public final class ResponseEvents {

    private final List<EslEvent> events;
    private final boolean timedOut;

    public ResponseEvents(List<EslEvent> events, boolean timedOut) {
        this.events   = List.copyOf(events);
        this.timedOut = timedOut;
    }

    /** All collected events, in the order they were received. */
    public List<EslEvent> getEvents() {
        return events;
    }

    /** Returns all events that are instances of {@code type}. */
    @SuppressWarnings("unchecked")
    public <T extends EslEvent> List<T> getEventsOfType(Class<T> type) {
        return events.stream()
                .filter(type::isInstance)
                .map(e -> (T) e)
                .collect(Collectors.toList());
    }

    /** Returns the first event in the collection. */
    public Optional<EslEvent> first() {
        return events.stream().findFirst();
    }

    /** Returns the first event of the given type. */
    public <T extends EslEvent> Optional<T> firstOfType(Class<T> type) {
        return getEventsOfType(type).stream().findFirst();
    }

    /** Number of events collected. */
    public int size() {
        return events.size();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    /** {@code true} if the collection was cut short because the completion event never arrived. */
    public boolean isTimedOut() {
        return timedOut;
    }
}
