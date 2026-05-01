package com.freeswitchjava.esl.live;

import com.freeswitchjava.esl.model.EslEvent;
import com.freeswitchjava.esl.model.HangupCause;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Instant;

/**
 * Real-time stateful view of an active FreeSWITCH channel.
 *
 * <p>Maintained by {@link LiveChannelManager}. State updates automatically as events arrive —
 * no manual UUID filtering required. Attach {@link PropertyChangeListener}s to react to
 * specific property changes (state, bridge, hangup).
 *
 * <pre>{@code
 * LiveChannelManager manager = LiveChannelManager.attach(client);
 * manager.onChannelCreated(channel -> {
 *     channel.addPropertyChangeListener("state", evt ->
 *         log.info("{} state: {} -> {}", channel.getUniqueId(), evt.getOldValue(), evt.getNewValue()));
 * });
 * }</pre>
 */
public final class LiveChannel {

    // ── Property name constants (use with addPropertyChangeListener) ──────────

    public static final String PROP_STATE          = "state";
    public static final String PROP_ANSWER_STATE   = "answerState";
    public static final String PROP_BRIDGED_UUID   = "bridgedUuid";
    public static final String PROP_CALLER_ID_NAME = "callerIdName";
    public static final String PROP_CALLER_ID_NUM  = "callerIdNumber";

    private final String uuid;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private volatile String callerIdName;
    private volatile String callerIdNumber;
    private volatile String destinationNumber;
    private volatile String context;
    private volatile String channelState;
    private volatile String answerState;
    private volatile String callDirection;
    private volatile String bridgedUuid;
    private volatile HangupCause hangupCause;
    private volatile Instant createdAt;
    private volatile Instant answeredAt;
    private volatile Instant hungUpAt;
    private volatile boolean active = true;

    LiveChannel(EslEvent createEvent) {
        this.uuid              = createEvent.getUniqueId();
        this.callerIdName      = createEvent.getCallerIdName();
        this.callerIdNumber    = createEvent.getCallerIdNumber();
        this.destinationNumber = createEvent.getDestinationNumber();
        this.context           = createEvent.getContext();
        this.channelState      = createEvent.getChannelState();
        this.answerState       = createEvent.getAnswerState();
        this.callDirection     = createEvent.getCallDirection();
        this.createdAt         = Instant.now();
    }

    // ── State update (called by LiveChannelManager) ───────────────────────────

    void applyEvent(EslEvent event) {
        String name = event.getEventName();
        switch (name) {
            case "CHANNEL_STATE", "CHANNEL_CALLSTATE" -> {
                setChannelState(event.getChannelState());
                setAnswerState(event.getAnswerState());
            }
            case "CHANNEL_ANSWER" -> {
                setChannelState("CS_ACTIVE");
                setAnswerState("answered");
                this.answeredAt = Instant.now();
            }
            case "CHANNEL_BRIDGE" -> {
                String other = event.getHeader("Other-Leg-Unique-ID");
                setBridgedUuid(other);
            }
            case "CHANNEL_UNBRIDGE" -> setBridgedUuid(null);
            case "CHANNEL_HANGUP", "CHANNEL_HANGUP_COMPLETE" -> {
                String causeStr = event.getHangupCause();
                this.hangupCause = causeStr != null ? HangupCause.fromName(causeStr) : null;
                this.hungUpAt    = Instant.now();
                this.active      = false;
                setChannelState("CS_DESTROY");
            }
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getUniqueId()          { return uuid; }
    public String getCallerIdName()      { return callerIdName; }
    public String getCallerIdNumber()    { return callerIdNumber; }
    public String getDestinationNumber() { return destinationNumber; }
    public String getContext()           { return context; }
    public String getChannelState()      { return channelState; }
    public String getAnswerState()       { return answerState; }
    public String getCallDirection()     { return callDirection; }
    public String getBridgedUuid()       { return bridgedUuid; }
    public HangupCause getHangupCause() { return hangupCause; }
    public Instant getCreatedAt()        { return createdAt; }
    public Instant getAnsweredAt()       { return answeredAt; }
    public Instant getHungUpAt()         { return hungUpAt; }

    /** {@code true} until a hangup event is received. */
    public boolean isActive()            { return active; }

    /** {@code true} if the channel is bridged to another leg. */
    public boolean isBridged()           { return bridgedUuid != null; }

    // ── PropertyChangeListener support ────────────────────────────────────────

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /** Listen for changes to a specific property (use the {@code PROP_*} constants). */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    // ── Internal setters with change events ───────────────────────────────────

    private void setChannelState(String newVal) {
        String old = channelState;
        channelState = newVal;
        pcs.firePropertyChange(PROP_STATE, old, newVal);
    }

    private void setAnswerState(String newVal) {
        String old = answerState;
        answerState = newVal;
        pcs.firePropertyChange(PROP_ANSWER_STATE, old, newVal);
    }

    private void setBridgedUuid(String newVal) {
        String old = bridgedUuid;
        bridgedUuid = newVal;
        pcs.firePropertyChange(PROP_BRIDGED_UUID, old, newVal);
    }

    @Override
    public String toString() {
        return "LiveChannel{uuid=" + uuid
                + ", from=" + callerIdNumber
                + ", to=" + destinationNumber
                + ", state=" + channelState
                + ", active=" + active + "}";
    }
}
