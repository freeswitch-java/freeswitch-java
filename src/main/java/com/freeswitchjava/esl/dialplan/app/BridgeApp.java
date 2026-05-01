package com.freeswitchjava.esl.dialplan.app;

import com.freeswitchjava.esl.dialplan.DpApp;
import com.freeswitchjava.esl.dialplan.DpTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Typed builder for the {@code bridge} dialplan application.
 *
 * <p>Bridges the current channel to a new endpoint, with support for
 * channel variables, timeout, and codec/media options via curly-brace prefix.
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Simple bridge
 * session.execute(new BridgeApp("sofia/default/1001@domain.com")).join();
 *
 * // Bridge with timeout and channel vars
 * session.execute(new BridgeApp("sofia/default/1001@domain.com")
 *     .timeout(30)
 *     .var("ignore_early_media", "true")
 *     .var("originate_timeout", "30")).join();
 *
 * // Bridge with codec restriction
 * session.execute(new BridgeApp("sofia/default/1001@domain.com")
 *     .codec("PCMU,PCMA")).join();
 * }</pre>
 */
public final class BridgeApp implements DpApp {

    private final String       dialString;
    private final List<String> vars    = new ArrayList<>();
    private String             codec;
    private int                timeout;
    private boolean            bypass;

    /**
     * @param dialString FreeSWITCH dial string (e.g. {@code "sofia/default/1001@domain.com"})
     */
    public BridgeApp(String dialString) {
        this.dialString = dialString;
    }

    /**
     * Set a channel variable on the B-leg using the curly-brace prefix.
     * Multiple calls accumulate vars.
     */
    public BridgeApp var(String name, String value) {
        vars.add(name + "=" + value);
        return this;
    }

    /**
     * Restrict codecs for this bridge leg (e.g. {@code "PCMU,PCMA"}).
     * Sets {@code absolute_codec_string} in the curly-brace prefix.
     */
    public BridgeApp codec(String codec) {
        this.codec = codec;
        return this;
    }

    /**
     * Originate timeout in seconds. Sets {@code originate_timeout} in the prefix.
     */
    public BridgeApp timeout(int seconds) {
        this.timeout = seconds;
        return this;
    }

    /**
     * Enable media bypass (proxy mode). Sets {@code bypass_media} in the prefix.
     */
    public BridgeApp bypass(boolean bypass) {
        this.bypass = bypass;
        return this;
    }

    @Override
    public String appName() { return DpTools.BRIDGE; }

    @Override
    public String toArg() {
        List<String> all = new ArrayList<>(vars);
        if (codec   != null) all.add("absolute_codec_string=" + codec);
        if (timeout  >  0)   all.add("originate_timeout=" + timeout);
        if (bypass)          all.add("bypass_media=true");

        if (all.isEmpty()) return dialString;
        return "{" + String.join(",", all) + "}" + dialString;
    }
}
