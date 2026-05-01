package com.freeswitchjava.esl.inbound;

import com.freeswitchjava.esl.model.HangupCause;

/**
 * Lifecycle callbacks for an outbound call originated via
 * {@link InboundClient#originateWithCallback(com.freeswitchjava.esl.model.OriginateOptions, OriginateCallback)}.
 *
 * <p>Override only the outcomes you care about. All methods are no-ops by default.
 *
 * <pre>{@code
 * client.originateWithCallback(
 *     OriginateOptions.builder()
 *         .dialString("sofia/default/1001@domain.com")
 *         .extension("2000").build(),
 *     new OriginateCallback() {
 *         {@literal @}Override public void onRinging(String uuid)  { log.info("Ringing:  {}", uuid); }
 *         {@literal @}Override public void onAnswered(String uuid) { log.info("Answered: {}", uuid); }
 *         {@literal @}Override public void onBusy(String uuid, HangupCause cause)   { log.info("Busy"); }
 *         {@literal @}Override public void onNoAnswer(String uuid) { log.info("No answer"); }
 *         {@literal @}Override public void onFailed(String uuid, HangupCause cause) { log.warn("Failed: {}", cause); }
 *     });
 * }</pre>
 */
public interface OriginateCallback {

    /** Called when the remote party starts ringing ({@code CS_ROUTING} / SIP 180). */
    default void onRinging(String uuid) {}

    /** Called when the remote party answers the call. */
    default void onAnswered(String uuid) {}

    /** Called when the remote party is busy ({@link HangupCause#USER_BUSY}). */
    default void onBusy(String uuid, HangupCause cause) {}

    /** Called when the remote party does not answer before the originate timeout. */
    default void onNoAnswer(String uuid) {}

    /** Called for all other failure conditions (congestion, rejected, unreachable, etc.). */
    default void onFailed(String uuid, HangupCause cause) {}
}
