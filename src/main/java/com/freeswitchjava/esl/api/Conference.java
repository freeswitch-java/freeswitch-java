package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Fluent API for all FreeSWITCH {@code conference} API sub-commands.
 *
 * <p>Obtain an instance via {@code client.conference("my-room")}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Mute member 3 in room "sales"
 * client.conference("sales").mute(3).join();
 *
 * // Play hold music into the conference
 * client.conference("sales").play("/tmp/hold_music.wav").join();
 *
 * // Dial out from conference
 * client.conference("sales").bgdial("sofia/default/1001@domain.com", "myprofile").join();
 * }</pre>
 */
public final class Conference {

    private final String room;
    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public Conference(String room, Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.room        = Objects.requireNonNull(room, "conference room name");
        this.apiExecutor = Objects.requireNonNull(apiExecutor);
    }

    // ── Member audio control ──────────────────────────────────────────────────

    /** {@code conference <room> mute <member_id|all>} */
    public CompletableFuture<ApiResponse> mute(String memberId) {
        return conf("mute " + memberId);
    }

    public CompletableFuture<ApiResponse> mute(int memberId) {
        return mute(String.valueOf(memberId));
    }

    public CompletableFuture<ApiResponse> muteAll() {
        return mute("all");
    }

    /** {@code conference <room> unmute <member_id|all>} */
    public CompletableFuture<ApiResponse> unmute(String memberId) {
        return conf("unmute " + memberId);
    }

    public CompletableFuture<ApiResponse> unmute(int memberId) {
        return unmute(String.valueOf(memberId));
    }

    public CompletableFuture<ApiResponse> unmuteAll() {
        return unmute("all");
    }

    /** {@code conference <room> tmute <member_id>} — Toggle mute. */
    public CompletableFuture<ApiResponse> toggleMute(int memberId) {
        return conf("tmute " + memberId);
    }

    /** {@code conference <room> deaf <member_id|all>} — Block conference audio from member. */
    public CompletableFuture<ApiResponse> deaf(String memberId) {
        return conf("deaf " + memberId);
    }

    public CompletableFuture<ApiResponse> deafAll() {
        return deaf("all");
    }

    /** {@code conference <room> undeaf <member_id|all>} */
    public CompletableFuture<ApiResponse> undeaf(String memberId) {
        return conf("undeaf " + memberId);
    }

    public CompletableFuture<ApiResponse> undeafAll() {
        return undeaf("all");
    }

    /** {@code conference <room> energy <member_id> <level>} — Adjust noise gate threshold. */
    public CompletableFuture<ApiResponse> energy(int memberId, int level) {
        return conf("energy " + memberId + " " + level);
    }

    /** {@code conference <room> volume_in <member_id> <level>} — Adjust input gain. */
    public CompletableFuture<ApiResponse> volumeIn(int memberId, int level) {
        return conf("volume_in " + memberId + " " + level);
    }

    /** {@code conference <room> volume_out <member_id> <level>} — Adjust output volume. */
    public CompletableFuture<ApiResponse> volumeOut(int memberId, int level) {
        return conf("volume_out " + memberId + " " + level);
    }

    // ── Member management ─────────────────────────────────────────────────────

    /** {@code conference <room> kick <member_id|all|last>} */
    public CompletableFuture<ApiResponse> kick(String memberId) {
        return conf("kick " + memberId);
    }

    public CompletableFuture<ApiResponse> kick(int memberId) {
        return kick(String.valueOf(memberId));
    }

    public CompletableFuture<ApiResponse> kickAll() {
        return kick("all");
    }

    public CompletableFuture<ApiResponse> kickLast() {
        return kick("last");
    }

    /** {@code conference <room> hup <member_id>} — Hangup specific member. */
    public CompletableFuture<ApiResponse> hup(int memberId) {
        return conf("hup " + memberId);
    }

    /** {@code conference <room> transfer <member_id> <other_room>} */
    public CompletableFuture<ApiResponse> transfer(int memberId, String targetRoom) {
        return conf("transfer " + memberId + " " + targetRoom);
    }

    /**
     * {@code conference <room> relate <member_id> <other_member_id> <relation>}
     * — Mute/deaf relationship between two members.
     * {@code relation}: {@code "mute"}, {@code "deaf"}, {@code "clear"}
     */
    public CompletableFuture<ApiResponse> relate(int member1, int member2, String relation) {
        return conf("relate " + member1 + " " + member2 + " " + relation);
    }

    /** {@code conference <room> dtmf <member_id> <digits>} — Send DTMF to a member. */
    public CompletableFuture<ApiResponse> dtmf(int memberId, String digits) {
        return conf("dtmf " + memberId + " " + digits);
    }

    // ── Playback & recording ──────────────────────────────────────────────────

    /** {@code conference <room> play <file> [member_id]} */
    public CompletableFuture<ApiResponse> play(String file) {
        return conf("play " + file);
    }

    public CompletableFuture<ApiResponse> play(String file, int memberId) {
        return conf("play " + file + " " + memberId);
    }

    /** {@code conference <room> stop [current|all|last|member <id>]} */
    public CompletableFuture<ApiResponse> stop(String target) {
        return conf("stop " + target);
    }

    public CompletableFuture<ApiResponse> stopAll() {
        return stop("all");
    }

    public CompletableFuture<ApiResponse> stopCurrent() {
        return stop("current");
    }

    /** {@code conference <room> file_seek [+-]<ms|%>} — Seek in playing file. */
    public CompletableFuture<ApiResponse> fileSeek(String position) {
        return conf("file_seek " + position);
    }

    /** {@code conference <room> pause_play} — Pause current file playback. */
    public CompletableFuture<ApiResponse> pausePlay() {
        return conf("pause_play");
    }

    /** {@code conference <room> file-vol <level>} — Adjust playback volume. */
    public CompletableFuture<ApiResponse> fileVolume(int level) {
        return conf("file-vol " + level);
    }

    /** {@code conference <room> record <file>} — Start conference recording. */
    public CompletableFuture<ApiResponse> record(String file) {
        return conf("recording start " + file);
    }

    /** {@code conference <room> recording stop <file>} */
    public CompletableFuture<ApiResponse> stopRecording(String file) {
        return conf("recording stop " + file);
    }

    /** {@code conference <room> recording pause <file>} */
    public CompletableFuture<ApiResponse> pauseRecording(String file) {
        return conf("recording pause " + file);
    }

    /** {@code conference <room> recording resume <file>} */
    public CompletableFuture<ApiResponse> resumeRecording(String file) {
        return conf("recording resume " + file);
    }

    /** {@code conference <room> chkrecord} — Query current recording status. */
    public CompletableFuture<ApiResponse> checkRecording() {
        return conf("chkrecord");
    }

    // ── Room management ───────────────────────────────────────────────────────

    /** {@code conference <room> lock} — Prevent new members from joining. */
    public CompletableFuture<ApiResponse> lock() {
        return conf("lock");
    }

    /** {@code conference <room> unlock} */
    public CompletableFuture<ApiResponse> unlock() {
        return conf("unlock");
    }

    /** {@code conference <room> pin <pin>} — Set a PIN for joining. */
    public CompletableFuture<ApiResponse> pin(String pin) {
        return conf("pin " + pin);
    }

    /** {@code conference <room> nopin} — Remove PIN requirement. */
    public CompletableFuture<ApiResponse> nopin() {
        return conf("nopin");
    }

    /** {@code conference <room> floor <member_id>} — Toggle floor for a member. */
    public CompletableFuture<ApiResponse> floor(int memberId) {
        return conf("floor " + memberId);
    }

    /** {@code conference <room> get <param>} — Get a conference parameter. */
    public CompletableFuture<ApiResponse> get(String param) {
        return conf("get " + param);
    }

    /** {@code conference <room> set <param> <value>} — Set a conference parameter. */
    public CompletableFuture<ApiResponse> set(String param, String value) {
        return conf("set " + param + " " + value);
    }

    /** {@code conference <room> getvar <name>} */
    public CompletableFuture<ApiResponse> getVar(String name) {
        return conf("getvar " + name);
    }

    /** {@code conference <room> setvar <name> <value>} */
    public CompletableFuture<ApiResponse> setVar(String name, String value) {
        return conf("setvar " + name + " " + value);
    }

    /** {@code conference <room> say <text>} — Speak text via TTS into conference. */
    public CompletableFuture<ApiResponse> say(String text) {
        return conf("say " + text);
    }

    /** {@code conference <room> saymember <member_id> <text>} — Speak text to one member. */
    public CompletableFuture<ApiResponse> sayMember(int memberId, String text) {
        return conf("saymember " + memberId + " " + text);
    }

    /** {@code conference <room> agc <level>} — Set automatic gain control level. */
    public CompletableFuture<ApiResponse> agc(int level) {
        return conf("agc " + level);
    }

    // ── Dialing ───────────────────────────────────────────────────────────────

    /**
     * {@code conference <room> dial <endpoint>} — Synchronous dial out from conference.
     *
     * @param endpoint dial string, e.g. {@code "sofia/default/1001@domain.com"}
     * @param profile  Sofia profile name, or null
     */
    public CompletableFuture<ApiResponse> dial(String endpoint, String profile) {
        String cmd = "dial " + endpoint;
        if (profile != null) cmd += " " + profile;
        return conf(cmd);
    }

    /** {@code conference <room> bgdial <endpoint> [profile]>} — Async dial out. */
    public CompletableFuture<ApiResponse> bgdial(String endpoint, String profile) {
        String cmd = "bgdial " + endpoint;
        if (profile != null) cmd += " " + profile;
        return conf(cmd);
    }

    // ── Info ──────────────────────────────────────────────────────────────────

    /** {@code conference <room> list} — List conference members. */
    public CompletableFuture<ApiResponse> list() {
        return conf("list");
    }

    /** {@code conference <room> list count} — Count conference members. */
    public CompletableFuture<ApiResponse> count() {
        return conf("list count");
    }

    // ── Video ─────────────────────────────────────────────────────────────────

    /** {@code conference <room> vid-floor <member_id> [force]} — Set video floor. */
    public CompletableFuture<ApiResponse> vidFloor(int memberId, boolean force) {
        return conf("vid-floor " + memberId + (force ? " force" : ""));
    }

    /** {@code conference <room> clear-vid-floor} — Clear video floor lock. */
    public CompletableFuture<ApiResponse> clearVidFloor() {
        return conf("clear-vid-floor");
    }

    /** {@code conference <room> vid-banner <member_id> <text>} */
    public CompletableFuture<ApiResponse> vidBanner(int memberId, String text) {
        return conf("vid-banner " + memberId + " " + text);
    }

    /** {@code conference <room> vid-layout [layout_name]} */
    public CompletableFuture<ApiResponse> vidLayout(String layoutName) {
        return conf("vid-layout " + layoutName);
    }

    /** {@code conference <room> vid-fps <fps>} */
    public CompletableFuture<ApiResponse> vidFps(int fps) {
        return conf("vid-fps " + fps);
    }

    /** {@code conference <room> vid-bandwidth <kbps>} */
    public CompletableFuture<ApiResponse> vidBandwidth(int kbps) {
        return conf("vid-bandwidth " + kbps);
    }

    /** {@code conference <room> vmute <member_id>} — Video mute. */
    public CompletableFuture<ApiResponse> vmute(int memberId) {
        return conf("vmute " + memberId);
    }

    /** {@code conference <room> unvmute <member_id>} */
    public CompletableFuture<ApiResponse> unvmute(int memberId) {
        return conf("unvmute " + memberId);
    }

    /** {@code conference <room> tvmute <member_id>} — Toggle video mute. */
    public CompletableFuture<ApiResponse> tvmute(int memberId) {
        return conf("tvmute " + memberId);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private CompletableFuture<ApiResponse> conf(String subCommand) {
        return apiExecutor.apply("conference " + room + " " + subCommand);
    }
}
