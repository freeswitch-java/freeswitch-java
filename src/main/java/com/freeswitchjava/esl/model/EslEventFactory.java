package com.freeswitchjava.esl.model;

import com.freeswitchjava.esl.codec.EslHeaders;
import com.freeswitchjava.esl.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal factory that maps an event-name string to its most specific {@link EslEvent} subclass.
 *
 * <p>Custom event classes for FreeSWITCH {@code CUSTOM} events can be registered at runtime
 * via {@link #registerCustomEventClass(String, Class)}:
 *
 * <pre>{@code
 * // FreeSWITCH dialplan fires:  send_info Event-Subclass: conference::maintenance
 * EslEventFactory.registerCustomEventClass(
 *     "conference::maintenance", ConferenceMaintenanceEvent.class);
 *
 * // ConferenceMaintenanceEvent must have: (Map<String,String>, String) constructor
 * client.addEventListener(EventName.CUSTOM, event -> {
 *     if (event instanceof ConferenceMaintenanceEvent e) { ... }
 * });
 * }</pre>
 */
public final class EslEventFactory {

    private static final Logger log = LoggerFactory.getLogger(EslEventFactory.class);

    /** Registry for CUSTOM event subclasses, keyed by Event-Subclass value. */
    private static final ConcurrentHashMap<String, Class<? extends EslEvent>> customRegistry =
            new ConcurrentHashMap<>();

    private EslEventFactory() {}

    // ── Custom event registration ─────────────────────────────────────────────

    /**
     * Registers a custom event class for a specific {@code CUSTOM} event subclass.
     *
     * <p>The class must expose a public constructor with signature
     * {@code (Map<String, String> headers, String body)}.
     *
     * @param subclass the {@code Event-Subclass} value (e.g. {@code "conference::maintenance"})
     * @param clazz    the custom event class to instantiate
     */
    public static void registerCustomEventClass(String subclass, Class<? extends EslEvent> clazz) {
        customRegistry.put(subclass, clazz);
        log.debug("[FACTORY] Registered custom event class — subclass=[{}] class=[{}]",
                subclass, clazz.getSimpleName());
    }

    /** Removes a previously registered custom event class mapping. */
    public static void deregisterCustomEventClass(String subclass) {
        customRegistry.remove(subclass);
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    static EslEvent create(String eventName, Map<String, String> headers, String body) {
        if (eventName == null) return new EslEvent(headers, body);

        return switch (eventName) {
            // ── Channel lifecycle ──────────────────────────────────────────────
            case "CHANNEL_CREATE"           -> new ChannelCreateEvent(headers, body);
            case "CHANNEL_DESTROY"          -> new ChannelDestroyEvent(headers, body);
            case "CHANNEL_STATE"            -> new ChannelStateEvent(headers, body);
            case "CHANNEL_CALLSTATE"        -> new ChannelStateEvent(headers, body);
            case "CHANNEL_ANSWER"           -> new ChannelAnswerEvent(headers, body);
            case "CHANNEL_HANGUP"           -> new ChannelHangupEvent(headers, body);
            case "CHANNEL_HANGUP_COMPLETE"  -> new ChannelHangupCompleteEvent(headers, body);
            case "CHANNEL_ORIGINATE"        -> new ChannelOriginateEvent(headers, body);
            case "CHANNEL_OUTGOING"         -> new ChannelOutgoingEvent(headers, body);
            case "CHANNEL_GLOBAL"           -> new ChannelEvent(headers, body);

            // ── Dialplan execution ─────────────────────────────────────────────
            case "CHANNEL_EXECUTE"          -> new ChannelExecuteEvent(headers, body);
            case "CHANNEL_EXECUTE_COMPLETE" -> new ChannelExecuteCompleteEvent(headers, body);
            case "CHANNEL_APPLICATION"      -> new ChannelExecuteEvent(headers, body);
            case "CHANNEL_PROGRESS"         -> new ChannelProgressEvent(headers, body);
            case "CHANNEL_PROGRESS_MEDIA"   -> new ChannelProgressMediaEvent(headers, body);

            // ── Bridge ────────────────────────────────────────────────────────
            case "CHANNEL_BRIDGE"           -> new ChannelBridgeEvent(headers, body);
            case "CHANNEL_UNBRIDGE"         -> new ChannelUnbridgeEvent(headers, body);

            // ── Hold / Park ───────────────────────────────────────────────────
            case "CHANNEL_HOLD"             -> new ChannelHoldEvent(headers, body);
            case "CHANNEL_UNHOLD"           -> new ChannelUnholdEvent(headers, body);
            case "CHANNEL_PARK"             -> new ChannelParkEvent(headers, body);
            case "CHANNEL_UNPARK"           -> new ChannelUnparkEvent(headers, body);

            // ── DTMF ──────────────────────────────────────────────────────────
            case "DTMF"                     -> new DtmfEvent(headers, body);

            // ── Recording / Playback ──────────────────────────────────────────
            case "RECORD_START"             -> new RecordStartEvent(headers, body);
            case "RECORD_STOP"              -> new RecordStopEvent(headers, body);
            case "PLAYBACK_START"           -> new PlaybackStartEvent(headers, body);
            case "PLAYBACK_STOP"            -> new PlaybackStopEvent(headers, body);

            // ── Media bugs ────────────────────────────────────────────────────
            case "MEDIA_BUG_START"          -> new MediaBugStartEvent(headers, body);
            case "MEDIA_BUG_STOP"           -> new MediaBugStopEvent(headers, body);

            // ── Background job ────────────────────────────────────────────────
            case "BACKGROUND_JOB"           -> new BackgroundJobEvent(headers, body);

            // ── CDR / call detail ─────────────────────────────────────────────
            case "CDR"                      -> new CdrEvent(headers, body);
            case "CALL_DETAIL"              -> new CdrEvent(headers, body);

            // ── Scheduler ─────────────────────────────────────────────────────
            case "ADD_SCHEDULE"             -> new ScheduleEvent(headers, body);
            case "DEL_SCHEDULE"             -> new ScheduleEvent(headers, body);
            case "EXE_SCHEDULE"             -> new ScheduleEvent(headers, body);
            case "RE_SCHEDULE"              -> new ScheduleEvent(headers, body);

            // ── API ───────────────────────────────────────────────────────────
            case "API"                      -> new ApiEvent(headers, body);

            // ── Speech / Tone ─────────────────────────────────────────────────
            case "DETECTED_SPEECH"          -> new DetectedSpeechEvent(headers, body);
            case "DETECTED_TONE"            -> new EslEvent(headers, body);

            // ── Heartbeat / session ───────────────────────────────────────────
            case "HEARTBEAT"                -> new HeartbeatEvent(headers, body);
            case "SESSION_HEARTBEAT"        -> new HeartbeatEvent(headers, body);

            // ── Registration ──────────────────────────────────────────────────
            case "REGISTER"                 -> new RegisterEvent(headers, body);
            case "REGISTER_ATTEMPT"         -> new RegisterEvent(headers, body);
            case "UNREGISTER"               -> new UnregisterEvent(headers, body);

            // ── Device state ──────────────────────────────────────────────────
            case "DEVICE_STATE"             -> new DeviceStateEvent(headers, body);

            // ── NAT ───────────────────────────────────────────────────────────
            case "NAT"                      -> new NatEvent(headers, body);

            // ── SIP INFO ──────────────────────────────────────────────────────
            case "RECV_INFO"                -> new RecvInfoEvent(headers, body);
            case "SEND_INFO"                -> new SendInfoEvent(headers, body);

            // ── Phone features ────────────────────────────────────────────────
            case "PHONE_FEATURE"            -> new PhoneFeatureEvent(headers, body);
            case "PHONE_FEATURE_SUBSCRIBE"  -> new PhoneFeatureEvent(headers, body);

            // ── CUSTOM — dispatch to registered subclass or base EslEvent ─────
            case "CUSTOM"                   -> createCustomEvent(headers, body);

            // ── All others ────────────────────────────────────────────────────
            default                         -> new EslEvent(headers, body);
        };
    }

    private static EslEvent createCustomEvent(Map<String, String> headers, String body) {
        String subclass = headers.get(EslHeaders.EVENT_SUBCLASS);
        if (subclass == null) return new EslEvent(headers, body);

        Class<? extends EslEvent> clazz = customRegistry.get(subclass);
        if (clazz == null) return new EslEvent(headers, body);

        try {
            Constructor<? extends EslEvent> ctor = clazz.getConstructor(Map.class, String.class);
            return ctor.newInstance(headers, body);
        } catch (Exception e) {
            log.warn("[FACTORY] Failed to instantiate custom event class=[{}] subclass=[{}] — {}",
                    clazz.getSimpleName(), subclass, e.getMessage());
            return new EslEvent(headers, body);
        }
    }
}
