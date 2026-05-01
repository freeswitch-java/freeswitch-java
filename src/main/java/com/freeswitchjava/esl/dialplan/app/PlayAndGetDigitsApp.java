package com.freeswitchjava.esl.dialplan.app;

import com.freeswitchjava.esl.dialplan.DpApp;
import com.freeswitchjava.esl.dialplan.DpTools;

/**
 * Typed builder for the {@code play_and_get_digits} dialplan application.
 *
 * <p>Plays a prompt file and collects DTMF digits into a channel variable.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * session.execute(new PlayAndGetDigitsApp()
 *     .min(1).max(1).tries(3).timeout(5000)
 *     .terminator("#").prompt("/tmp/press-option.wav")
 *     .variable("dtmf_choice")).join();
 *
 * String digit = session.getChannelVar("dtmf_choice");
 * }</pre>
 */
public final class PlayAndGetDigitsApp implements DpApp {

    private int    min          = 1;
    private int    max          = 1;
    private int    tries        = 1;
    private int    timeout      = 5000;
    private int    digitTimeout = 0;
    private String terminator   = "#";
    private String prompt       = "silence_stream://0";
    private String variable     = "dtmf_digits";
    private String regex;
    private String invalidPrompt;

    /** Minimum digits required (default 1). */
    public PlayAndGetDigitsApp min(int min) { this.min = min; return this; }

    /** Maximum digits allowed (default 1). */
    public PlayAndGetDigitsApp max(int max) { this.max = max; return this; }

    /** Number of attempts before giving up (default 1). */
    public PlayAndGetDigitsApp tries(int tries) { this.tries = tries; return this; }

    /** Inter-digit timeout in milliseconds (default 5000). */
    public PlayAndGetDigitsApp timeout(int ms) { this.timeout = ms; return this; }

    /** Initial digit timeout in milliseconds (0 = use {@link #timeout}). */
    public PlayAndGetDigitsApp digitTimeout(int ms) { this.digitTimeout = ms; return this; }

    /** Digit that terminates input early (default {@code "#"}). Pass {@code ""} to disable. */
    public PlayAndGetDigitsApp terminator(String terminator) { this.terminator = terminator; return this; }

    /** Prompt file to play. */
    public PlayAndGetDigitsApp prompt(String path) { this.prompt = path; return this; }

    /** Channel variable to store the collected digits in (default {@code "dtmf_digits"}). */
    public PlayAndGetDigitsApp variable(String varName) { this.variable = varName; return this; }

    /** Optional regex to validate collected digits (e.g. {@code "\\d+"}). */
    public PlayAndGetDigitsApp regex(String regex) { this.regex = regex; return this; }

    /** Optional prompt to play when regex validation fails. */
    public PlayAndGetDigitsApp invalidPrompt(String path) { this.invalidPrompt = path; return this; }

    @Override
    public String appName() { return DpTools.PLAY_AND_GET_DIGITS; }

    @Override
    public String toArg() {
        StringBuilder sb = new StringBuilder()
                .append(min).append(' ')
                .append(max).append(' ')
                .append(tries).append(' ')
                .append(timeout).append(' ')
                .append(terminator != null ? terminator : "").append(' ')
                .append(prompt).append(' ')
                .append(variable);
        if (invalidPrompt != null && !invalidPrompt.isBlank()) sb.append(' ').append(invalidPrompt);
        else if (regex != null && !regex.isBlank()) sb.append(' ').append("silence_stream://0");
        if (regex != null && !regex.isBlank()) sb.append(' ').append(regex);
        if (digitTimeout > 0) sb.append(' ').append(digitTimeout);
        return sb.toString();
    }
}
