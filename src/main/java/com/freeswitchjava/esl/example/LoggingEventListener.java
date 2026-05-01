package com.freeswitchjava.esl.example;

import com.freeswitchjava.esl.event.AbstractEslEventListener;
import com.freeswitchjava.esl.event.BackgroundJobEvent;
import com.freeswitchjava.esl.event.ChannelAnswerEvent;
import com.freeswitchjava.esl.event.ChannelHangupEvent;
import com.freeswitchjava.esl.event.DtmfEvent;
import com.freeswitchjava.esl.event.HeartbeatEvent;

/**
 * Example listener for inbound ESL sessions.
 */
public final class LoggingEventListener extends AbstractEslEventListener {

    @Override
    protected void onChannelAnswer(ChannelAnswerEvent event) {
        System.out.printf(
                "[CHANNEL_ANSWER] uuid=%s caller=%s destination=%s%n",
                event.getUniqueId(),
                event.getCallerIdNumber(),
                event.getDestinationNumber());
    }

    @Override
    protected void onDtmf(DtmfEvent event) {
        System.out.printf(
                "[DTMF] uuid=%s digit=%s durationMs=%s%n",
                event.getUniqueId(),
                event.getDtmfDigit(),
                event.getDtmfDuration());
    }

    @Override
    protected void onBackgroundJob(BackgroundJobEvent event) {
        System.out.printf(
                "[BACKGROUND_JOB] jobUuid=%s command=%s result=%s%n",
                event.getJobUuid(),
                event.getJobCommand(),
                event.getResult());
    }

    @Override
    protected void onChannelHangup(ChannelHangupEvent event) {
        System.out.printf(
                "[CHANNEL_HANGUP] uuid=%s cause=%s%n",
                event.getUniqueId(),
                event.getHangupCause());
    }

    @Override
    protected void onHeartbeat(HeartbeatEvent event) {
        System.out.printf(
                "[HEARTBEAT] sessions=%s max=%s%n",
                event.getSessionCount(),
                event.getMaxSessions());
    }
}
