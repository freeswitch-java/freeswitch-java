package com.freeswitchjava.esl.dialplan.app;

import com.freeswitchjava.esl.dialplan.DpApp;
import com.freeswitchjava.esl.dialplan.DpTools;

/**
 * Typed builder for the {@code record} dialplan application.
 *
 * <p>Records audio from the channel's input stream to a file.
 * For recording both legs simultaneously, use
 * {@link com.freeswitchjava.esl.outbound.OutboundSession#recordSession(String)} instead.
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Record with 60-second limit
 * session.execute(new RecordApp("/tmp/voicemail.wav")
 *     .limit(60)).join();
 *
 * // Record with silence detection (stop after 3 hits of 500ms silence)
 * session.execute(new RecordApp("/tmp/message.wav")
 *     .limit(120)
 *     .silenceThreshold(500)
 *     .silenceHits(3)).join();
 * }</pre>
 */
public final class RecordApp implements DpApp {

    private final String path;
    private int          maxSeconds;
    private int          silenceThreshold;
    private int          silenceHits;

    /**
     * @param path output file path (e.g. {@code "/tmp/recording.wav"})
     */
    public RecordApp(String path) {
        this.path = path;
    }

    /** Maximum recording duration in seconds (0 = unlimited). */
    public RecordApp limit(int seconds) { this.maxSeconds = seconds; return this; }

    /**
     * Silence energy threshold for stop-on-silence detection (e.g. {@code 500}).
     * Must be combined with {@link #silenceHits(int)}.
     */
    public RecordApp silenceThreshold(int threshold) { this.silenceThreshold = threshold; return this; }

    /**
     * Number of consecutive silence detections required to stop recording.
     * Must be combined with {@link #silenceThreshold(int)}.
     */
    public RecordApp silenceHits(int hits) { this.silenceHits = hits; return this; }

    @Override
    public String appName() { return DpTools.RECORD; }

    @Override
    public String toArg() {
        StringBuilder sb = new StringBuilder(path);
        if (maxSeconds       > 0) sb.append(' ').append(maxSeconds);
        if (silenceThreshold > 0) sb.append(' ').append(silenceThreshold);
        if (silenceHits      > 0) sb.append(' ').append(silenceHits);
        return sb.toString();
    }
}
