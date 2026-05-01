package com.freeswitchjava.esl.dialplan.app;

import com.freeswitchjava.esl.dialplan.DpApp;
import com.freeswitchjava.esl.dialplan.DpTools;

/**
 * Typed builder for the {@code read} dialplan application.
 *
 * <p>Plays a prompt and reads DTMF digits into a channel variable.
 * Unlike {@code play_and_get_digits}, {@code read} has no retry logic
 * or regex validation — use it for simple single-pass digit collection.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * session.execute(new ReadApp()
 *     .min(4).max(4)
 *     .prompt("/tmp/enter-pin.wav")
 *     .variable("pin_number")
 *     .timeout(7000)
 *     .terminator("#")).join();
 *
 * String pin = session.getChannelVar("pin_number");
 * }</pre>
 */
public final class ReadApp implements DpApp {

    private int    min         = 1;
    private int    max         = 11;
    private String prompt      = "silence_stream://0";
    private String variable    = "read_digits";
    private int    timeout     = 5000;
    private String terminators = "#";

    /** Minimum digits required (default 1). */
    public ReadApp min(int min) { this.min = min; return this; }

    /** Maximum digits allowed (default 11). */
    public ReadApp max(int max) { this.max = max; return this; }

    /** Prompt file to play before reading. */
    public ReadApp prompt(String path) { this.prompt = path; return this; }

    /** Channel variable to store the collected digits in (default {@code "read_digits"}). */
    public ReadApp variable(String varName) { this.variable = varName; return this; }

    /** Inter-digit timeout in milliseconds (default 5000). */
    public ReadApp timeout(int ms) { this.timeout = ms; return this; }

    /** Terminator digits (default {@code "#"}). Pass {@code ""} to disable. */
    public ReadApp terminator(String terminators) { this.terminators = terminators; return this; }

    @Override
    public String appName() { return DpTools.READ; }

    @Override
    public String toArg() {
        return min + " " + max + " " + prompt + " " + variable + " " + timeout
                + (terminators != null && !terminators.isBlank() ? " " + terminators : "");
    }
}
