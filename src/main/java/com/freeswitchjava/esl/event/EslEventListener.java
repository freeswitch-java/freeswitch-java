package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.EventListener;

/**
 * Listener interface for FreeSWITCH ESL events.
 *
 * <p>Implement this interface and register it with
 * {@link com.freeswitchjava.esl.inbound.InboundClient#addEventListener(EslEventListener)}.
 * Every event received from FreeSWITCH is delivered to {@link #onEslEvent(EslEvent)}.
 * Use {@code instanceof} pattern matching inside the method to handle the specific
 * event types you care about.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class CallHandler implements EslEventListener {
 *
 *     @Override
 *     public void onEslEvent(EslEvent event) {
 *         if (event instanceof ChannelAnswerEvent answer) {
 *             System.out.println("Answered: " + answer.getCallerIdNumber()
 *                 + " -> " + answer.getDestinationNumber());
 *
 *         } else if (event instanceof DtmfEvent dtmf) {
 *             System.out.println("DTMF: " + dtmf.getDtmfDigit());
 *
 *         } else if (event instanceof ChannelHangupEvent hangup) {
 *             System.out.println("Hung up: " + hangup.getHangupCauseEnum());
 *
 *         } else if (event instanceof RecordStopEvent rec) {
 *             System.out.println("Recording saved: " + rec.getRecordFilePath()
 *                 + " (" + rec.getRecordSeconds() + "s)");
 *         }
 *     }
 * }
 *
 * // Register
 * client.addEventListener(new CallHandler());
 * }</pre>
 *
 * @see com.freeswitchjava.esl.inbound.InboundClient#addEventListener(EslEventListener)
 */
public interface EslEventListener extends EventListener {

    /**
     * Called for every event received from FreeSWITCH.
     *
     * <p>This method is invoked on the event-dispatch thread, not the Netty I/O thread,
     * so blocking here is safe but will delay delivery of subsequent events.
     * For long-running work, dispatch to your own executor.
     *
     * @param event the event received; cast or use {@code instanceof} to access typed fields
     */
    void onEslEvent(EslEvent event);
}
