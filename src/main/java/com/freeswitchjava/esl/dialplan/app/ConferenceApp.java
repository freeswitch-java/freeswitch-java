package com.freeswitchjava.esl.dialplan.app;

import com.freeswitchjava.esl.dialplan.DpApp;
import com.freeswitchjava.esl.dialplan.DpTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Typed builder for the {@code conference} dialplan application.
 *
 * <p>Routes the caller into a FreeSWITCH conference room.
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Join default room
 * session.execute(new ConferenceApp("my-room")).join();
 *
 * // Join with profile and flags
 * session.execute(new ConferenceApp("my-room")
 *     .profile("default")
 *     .flags("mute", "deaf")).join();
 *
 * // Join with PIN
 * session.execute(new ConferenceApp("my-room")
 *     .profile("default")
 *     .pin("1234")).join();
 * }</pre>
 */
public final class ConferenceApp implements DpApp {

    private final String     room;
    private String           profile;
    private final List<String> flags = new ArrayList<>();
    private String           pin;

    /**
     * @param room conference room name
     */
    public ConferenceApp(String room) {
        this.room = room;
    }

    /** Conference profile to use (e.g. {@code "default"}). */
    public ConferenceApp profile(String profile) { this.profile = profile; return this; }

    /**
     * Flags to apply (e.g. {@code "mute"}, {@code "deaf"}, {@code "moderator"}).
     * Multiple flags are joined with {@code |}.
     */
    public ConferenceApp flags(String... flags) {
        for (String f : flags) this.flags.add(f);
        return this;
    }

    /** PIN required to enter the conference. */
    public ConferenceApp pin(String pin) { this.pin = pin; return this; }

    @Override
    public String appName() { return DpTools.CONFERENCE; }

    @Override
    public String toArg() {
        StringBuilder sb = new StringBuilder(room);
        if (profile != null && !profile.isBlank()) sb.append('@').append(profile);
        if (!flags.isEmpty()) sb.append('+').append(String.join("|", flags));
        if (pin != null && !pin.isBlank()) sb.append('+').append(pin);
        return sb.toString();
    }
}
