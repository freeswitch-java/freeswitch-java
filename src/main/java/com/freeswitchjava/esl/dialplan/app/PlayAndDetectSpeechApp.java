package com.freeswitchjava.esl.dialplan.app;

import com.freeswitchjava.esl.dialplan.DpApp;
import com.freeswitchjava.esl.dialplan.DpTools;

/**
 * Typed builder for the {@code play_and_detect_speech} dialplan application.
 *
 * <p>Plays a prompt file while simultaneously performing ASR (automatic speech recognition).
 * Stops playback when speech is detected. Results are stored in the
 * {@code detect_speech_result} channel variable.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * session.execute(new PlayAndDetectSpeechApp("/tmp/say-your-name.wav")
 *     .engine("pocketsphinx")
 *     .grammar("name_grammar")).join();
 *
 * String result = session.getChannelVar("detect_speech_result");
 * }</pre>
 */
public final class PlayAndDetectSpeechApp implements DpApp {

    private final String file;
    private String       engine  = "pocketsphinx";
    private String       grammar;
    private String       params;

    /**
     * @param file prompt file to play while listening
     */
    public PlayAndDetectSpeechApp(String file) {
        this.file = file;
    }

    /** ASR engine name (default {@code "pocketsphinx"}). */
    public PlayAndDetectSpeechApp engine(String engine) { this.engine = engine; return this; }

    /** Grammar name or inline JSGF grammar. */
    public PlayAndDetectSpeechApp grammar(String grammar) { this.grammar = grammar; return this; }

    /**
     * Additional engine parameters passed inside curly braces.
     * e.g. {@code "hmm=/path/to/model,dict=/path/to/dict"}.
     */
    public PlayAndDetectSpeechApp params(String params) { this.params = params; return this; }

    @Override
    public String appName() { return DpTools.PLAY_AND_DETECT_SPEECH; }

    @Override
    public String toArg() {
        // Format: file detect:engine {params}grammar
        StringBuilder sb = new StringBuilder(file).append(" detect:").append(engine).append(' ');
        if (params != null && !params.isBlank()) sb.append('{').append(params).append('}');
        if (grammar != null) sb.append(grammar);
        return sb.toString();
    }
}
