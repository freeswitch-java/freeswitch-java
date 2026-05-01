package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

/**
 * Convenience base class for typed event handling — no {@code instanceof} chains required.
 *
 * <p>Override only the event methods you care about. All others are no-ops by default.
 * Uses Java 21 pattern-matching switch — no reflection, no {@code instanceof} chains.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * client.addEventListener(new AbstractEslEventListener() {
 *     {@literal @}Override
 *     public void onChannelAnswer(ChannelAnswerEvent event) {
 *         log.info("Answered: {} -> {}", event.getCallerIdNumber(), event.getDestinationNumber());
 *     }
 *
 *     {@literal @}Override
 *     public void onChannelHangup(ChannelHangupEvent event) {
 *         log.info("Hangup: {} cause={}", event.getUniqueId(), event.getHangupCause());
 *     }
 *
 *     {@literal @}Override
 *     public void onDtmf(DtmfEvent event) {
 *         log.info("Digit: {}", event.getDtmfDigit());
 *     }
 * });
 * }</pre>
 */
public abstract class AbstractEslEventListener implements EslEventListener {

    @Override
    public final void onEslEvent(EslEvent event) {
        switch (event) {
            // ── Channel lifecycle ──────────────────────────────────────────────
            case ChannelCreateEvent           e -> onChannelCreate(e);
            case ChannelDestroyEvent          e -> onChannelDestroy(e);
            case ChannelStateEvent            e -> onChannelState(e);
            case ChannelAnswerEvent           e -> onChannelAnswer(e);
            case ChannelHangupCompleteEvent   e -> onChannelHangupComplete(e);
            case ChannelHangupEvent           e -> onChannelHangup(e);
            case ChannelOriginateEvent        e -> onChannelOriginate(e);
            case ChannelOutgoingEvent         e -> onChannelOutgoing(e);
            // ── Dialplan execution ────────────────────────────────────────────
            case ChannelExecuteCompleteEvent  e -> onChannelExecuteComplete(e);
            case ChannelExecuteEvent          e -> onChannelExecute(e);
            case ChannelProgressMediaEvent    e -> onChannelProgressMedia(e);
            case ChannelProgressEvent         e -> onChannelProgress(e);
            // ── Bridge ────────────────────────────────────────────────────────
            case ChannelBridgeEvent           e -> onChannelBridge(e);
            case ChannelUnbridgeEvent         e -> onChannelUnbridge(e);
            // ── Hold / Park ───────────────────────────────────────────────────
            case ChannelHoldEvent             e -> onChannelHold(e);
            case ChannelUnholdEvent           e -> onChannelUnhold(e);
            case ChannelParkEvent             e -> onChannelPark(e);
            case ChannelUnparkEvent           e -> onChannelUnpark(e);
            // ── DTMF ──────────────────────────────────────────────────────────
            case DtmfEvent                    e -> onDtmf(e);
            // ── Recording / Playback ──────────────────────────────────────────
            case RecordStartEvent             e -> onRecordStart(e);
            case RecordStopEvent              e -> onRecordStop(e);
            case PlaybackStartEvent           e -> onPlaybackStart(e);
            case PlaybackStopEvent            e -> onPlaybackStop(e);
            case MediaBugStartEvent           e -> onMediaBugStart(e);
            case MediaBugStopEvent            e -> onMediaBugStop(e);
            // ── Speech ────────────────────────────────────────────────────────
            case DetectedSpeechEvent          e -> onDetectedSpeech(e);
            // ── Background jobs ───────────────────────────────────────────────
            case BackgroundJobEvent           e -> onBackgroundJob(e);
            // ── CDR ───────────────────────────────────────────────────────────
            case CdrEvent                     e -> onCdr(e);
            // ── System ───────────────────────────────────────────────────────
            case HeartbeatEvent               e -> onHeartbeat(e);
            case ApiEvent                     e -> onApi(e);
            case ScheduleEvent                e -> onSchedule(e);
            case RegisterEvent                e -> onRegister(e);
            case UnregisterEvent              e -> onUnregister(e);
            case DeviceStateEvent             e -> onDeviceState(e);
            case NatEvent                     e -> onNat(e);
            case RecvInfoEvent                e -> onRecvInfo(e);
            case SendInfoEvent                e -> onSendInfo(e);
            case PhoneFeatureEvent            e -> onPhoneFeature(e);
            // ── Everything else ───────────────────────────────────────────────
            default                           -> onUnhandledEvent(event);
        }
    }

    // ── Channel lifecycle ──────────────────────────────────────────────────────

    protected void onChannelCreate(ChannelCreateEvent event) {}
    protected void onChannelDestroy(ChannelDestroyEvent event) {}
    protected void onChannelState(ChannelStateEvent event) {}
    protected void onChannelAnswer(ChannelAnswerEvent event) {}
    protected void onChannelHangup(ChannelHangupEvent event) {}
    protected void onChannelHangupComplete(ChannelHangupCompleteEvent event) {}
    protected void onChannelOriginate(ChannelOriginateEvent event) {}
    protected void onChannelOutgoing(ChannelOutgoingEvent event) {}

    // ── Dialplan execution ─────────────────────────────────────────────────────

    protected void onChannelExecute(ChannelExecuteEvent event) {}
    protected void onChannelExecuteComplete(ChannelExecuteCompleteEvent event) {}
    protected void onChannelProgress(ChannelProgressEvent event) {}
    protected void onChannelProgressMedia(ChannelProgressMediaEvent event) {}

    // ── Bridge ────────────────────────────────────────────────────────────────

    protected void onChannelBridge(ChannelBridgeEvent event) {}
    protected void onChannelUnbridge(ChannelUnbridgeEvent event) {}

    // ── Hold / Park ───────────────────────────────────────────────────────────

    protected void onChannelHold(ChannelHoldEvent event) {}
    protected void onChannelUnhold(ChannelUnholdEvent event) {}
    protected void onChannelPark(ChannelParkEvent event) {}
    protected void onChannelUnpark(ChannelUnparkEvent event) {}

    // ── DTMF ──────────────────────────────────────────────────────────────────

    protected void onDtmf(DtmfEvent event) {}

    // ── Recording / Playback ──────────────────────────────────────────────────

    protected void onRecordStart(RecordStartEvent event) {}
    protected void onRecordStop(RecordStopEvent event) {}
    protected void onPlaybackStart(PlaybackStartEvent event) {}
    protected void onPlaybackStop(PlaybackStopEvent event) {}
    protected void onMediaBugStart(MediaBugStartEvent event) {}
    protected void onMediaBugStop(MediaBugStopEvent event) {}

    // ── Speech ────────────────────────────────────────────────────────────────

    protected void onDetectedSpeech(DetectedSpeechEvent event) {}

    // ── Background jobs ───────────────────────────────────────────────────────

    protected void onBackgroundJob(BackgroundJobEvent event) {}

    // ── CDR ───────────────────────────────────────────────────────────────────

    protected void onCdr(CdrEvent event) {}

    // ── System ────────────────────────────────────────────────────────────────

    protected void onHeartbeat(HeartbeatEvent event) {}
    protected void onApi(ApiEvent event) {}
    protected void onSchedule(ScheduleEvent event) {}
    protected void onRegister(RegisterEvent event) {}
    protected void onUnregister(UnregisterEvent event) {}
    protected void onDeviceState(DeviceStateEvent event) {}
    protected void onNat(NatEvent event) {}
    protected void onRecvInfo(RecvInfoEvent event) {}
    protected void onSendInfo(SendInfoEvent event) {}
    protected void onPhoneFeature(PhoneFeatureEvent event) {}

    // ── Catch-all ─────────────────────────────────────────────────────────────

    /** Called for any event that does not match a typed overload above. */
    protected void onUnhandledEvent(EslEvent event) {}
}
