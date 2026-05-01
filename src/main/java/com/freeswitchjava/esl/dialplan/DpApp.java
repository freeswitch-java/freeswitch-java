package com.freeswitchjava.esl.dialplan;

/**
 * Typed dialplan application builder.
 *
 * <p>Implement this interface to create a self-describing, builder-style representation
 * of a FreeSWITCH dialplan application and its arguments.
 *
 * <p>Pass any {@code DpApp} to
 * {@link com.freeswitchjava.esl.outbound.OutboundSession#execute(DpApp)}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * session.execute(new PlayAndGetDigitsApp()
 *     .min(1).max(1).tries(3).timeout(5000)
 *     .terminator("#").prompt("/tmp/press-1.wav")
 *     .variable("dtmf_result")).join();
 * }</pre>
 */
public interface DpApp {

    /** FreeSWITCH application name (e.g. {@code "play_and_get_digits"}). */
    String appName();

    /** Serialized argument string passed to the application. */
    String toArg();
}
