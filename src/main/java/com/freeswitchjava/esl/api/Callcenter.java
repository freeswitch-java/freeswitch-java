package com.freeswitchjava.esl.api;

import com.freeswitchjava.esl.model.ApiResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Typed API for FreeSWITCH {@code mod_callcenter}.
 *
 * <p>Obtain via {@code client.callcenter()}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Callcenter cc = client.callcenter();
 *
 * // Add an agent
 * cc.agentAdd("agent1@default").join();
 * cc.agentSetStatus("agent1@default", "Available").join();
 *
 * // Add a tier (assign agent to queue)
 * cc.tierAdd("support_queue", "agent1@default", 1, 1).join();
 *
 * // List queue members
 * cc.membersList("support_queue").thenAccept(r -> System.out.println(r.getBody()));
 * }</pre>
 *
 * @see <a href="https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Modules/mod_callcenter_1049389/">
 *     mod_callcenter documentation</a>
 */
public final class Callcenter {

    private final Function<String, CompletableFuture<ApiResponse>> apiExecutor;

    public Callcenter(Function<String, CompletableFuture<ApiResponse>> apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    // ── Agents ────────────────────────────────────────────────────────────────

    /**
     * {@code callcenter_config agent add <name> callback} — Add a new agent.
     *
     * @param name agent name in the format {@code "user@domain"} or {@code "user"}
     */
    public CompletableFuture<ApiResponse> agentAdd(String name) {
        return api("callcenter_config agent add " + name + " callback");
    }

    /** {@code callcenter_config agent del <name>} — Remove an agent. */
    public CompletableFuture<ApiResponse> agentDel(String name) {
        return api("callcenter_config agent del " + name);
    }

    /**
     * {@code callcenter_config agent set status <name> <status>} — Set agent availability.
     *
     * @param status e.g. {@code "Available"}, {@code "On Break"}, {@code "Logged Out"}
     */
    public CompletableFuture<ApiResponse> agentSetStatus(String name, String status) {
        return api("callcenter_config agent set status " + name + " " + status);
    }

    /**
     * {@code callcenter_config agent set state <name> <state>} — Set agent state.
     *
     * @param state e.g. {@code "Waiting"}, {@code "Receiving"}, {@code "In a queue call"}
     */
    public CompletableFuture<ApiResponse> agentSetState(String name, String state) {
        return api("callcenter_config agent set state " + name + " " + state);
    }

    /** {@code callcenter_config agent set contact <name> <contact>} — Set agent contact string. */
    public CompletableFuture<ApiResponse> agentSetContact(String name, String contact) {
        return api("callcenter_config agent set contact " + name + " " + contact);
    }

    /** {@code callcenter_config agent set ready_time <name> <epoch>} — Set agent ready time. */
    public CompletableFuture<ApiResponse> agentSetReadyTime(String name, long epochSeconds) {
        return api("callcenter_config agent set ready_time " + name + " " + epochSeconds);
    }

    /** {@code callcenter_config agent set reject_delay_time <name> <seconds>} */
    public CompletableFuture<ApiResponse> agentSetRejectDelayTime(String name, int seconds) {
        return api("callcenter_config agent set reject_delay_time " + name + " " + seconds);
    }

    /** {@code callcenter_config agent set busy_delay_time <name> <seconds>} */
    public CompletableFuture<ApiResponse> agentSetBusyDelayTime(String name, int seconds) {
        return api("callcenter_config agent set busy_delay_time " + name + " " + seconds);
    }

    /**
     * {@code callcenter_config agent get <field> <name>} — Get a specific agent field.
     *
     * @param field e.g. {@code "status"}, {@code "state"}, {@code "uuid"}, {@code "contact"}
     */
    public CompletableFuture<ApiResponse> agentGet(String field, String name) {
        return api("callcenter_config agent get " + field + " " + name);
    }

    /** {@code callcenter_config agent list [<name>]} — List all agents or a specific one. */
    public CompletableFuture<ApiResponse> agentList() {
        return api("callcenter_config agent list");
    }

    public CompletableFuture<ApiResponse> agentList(String name) {
        return api("callcenter_config agent list " + name);
    }

    // ── Tiers ─────────────────────────────────────────────────────────────────

    /**
     * {@code callcenter_config tier add <queue> <agent> <level> <position>} — Assign an agent to a queue.
     *
     * @param queue    queue name
     * @param agent    agent name
     * @param level    tier level (lower = higher priority, starts at 1)
     * @param position position within tier (for round-robin)
     */
    public CompletableFuture<ApiResponse> tierAdd(String queue, String agent, int level, int position) {
        return api("callcenter_config tier add " + queue + " " + agent + " " + level + " " + position);
    }

    /** {@code callcenter_config tier del <queue> <agent>} — Remove agent from queue. */
    public CompletableFuture<ApiResponse> tierDel(String queue, String agent) {
        return api("callcenter_config tier del " + queue + " " + agent);
    }

    /** {@code callcenter_config tier set state <queue> <agent> <state>} */
    public CompletableFuture<ApiResponse> tierSetState(String queue, String agent, String state) {
        return api("callcenter_config tier set state " + queue + " " + agent + " " + state);
    }

    /** {@code callcenter_config tier set level <queue> <agent> <level>} */
    public CompletableFuture<ApiResponse> tierSetLevel(String queue, String agent, int level) {
        return api("callcenter_config tier set level " + queue + " " + agent + " " + level);
    }

    /** {@code callcenter_config tier set position <queue> <agent> <position>} */
    public CompletableFuture<ApiResponse> tierSetPosition(String queue, String agent, int position) {
        return api("callcenter_config tier set position " + queue + " " + agent + " " + position);
    }

    /** {@code callcenter_config tier list [<queue>]} — List all tiers or tiers for a specific queue. */
    public CompletableFuture<ApiResponse> tierList() {
        return api("callcenter_config tier list");
    }

    public CompletableFuture<ApiResponse> tierList(String queue) {
        return api("callcenter_config tier list " + queue);
    }

    // ── Queues ────────────────────────────────────────────────────────────────

    /**
     * {@code callcenter_config queue load <name>} — Load a queue from configuration.
     */
    public CompletableFuture<ApiResponse> queueLoad(String name) {
        return api("callcenter_config queue load " + name);
    }

    /** {@code callcenter_config queue reload <name>} — Reload queue configuration. */
    public CompletableFuture<ApiResponse> queueReload(String name) {
        return api("callcenter_config queue reload " + name);
    }

    /** {@code callcenter_config queue unload <name>} — Unload a queue. */
    public CompletableFuture<ApiResponse> queueUnload(String name) {
        return api("callcenter_config queue unload " + name);
    }

    /**
     * {@code callcenter_config queue set <field> <name> <value>} — Update a queue parameter at runtime.
     *
     * @param field e.g. {@code "moh-sound"}, {@code "time-base-score"}, {@code "max-wait-time"}
     */
    public CompletableFuture<ApiResponse> queueSet(String name, String field, String value) {
        return api("callcenter_config queue set " + field + " " + name + " " + value);
    }

    /**
     * {@code callcenter_config queue get <field> <name>} — Get a queue parameter value.
     */
    public CompletableFuture<ApiResponse> queueGet(String name, String field) {
        return api("callcenter_config queue get " + field + " " + name);
    }

    /** {@code callcenter_config queue list} — List all queues. */
    public CompletableFuture<ApiResponse> queueList() {
        return api("callcenter_config queue list");
    }

    /** {@code callcenter_config queue count} — Count active queues. */
    public CompletableFuture<ApiResponse> queueCount() {
        return api("callcenter_config queue count");
    }

    // ── Members (callers waiting in queue) ────────────────────────────────────

    /** {@code callcenter_config member list <queue>} — List waiting members in a queue. */
    public CompletableFuture<ApiResponse> membersList(String queue) {
        return api("callcenter_config member list " + queue);
    }

    /** {@code callcenter_config member count <queue>} — Count waiting members. */
    public CompletableFuture<ApiResponse> membersCount(String queue) {
        return api("callcenter_config member count " + queue);
    }

    /**
     * {@code callcenter_config member del <queue> <uuid>} — Remove a member (caller) from the queue.
     *
     * @param uuid the channel UUID of the caller to remove
     */
    public CompletableFuture<ApiResponse> memberDel(String queue, String uuid) {
        return api("callcenter_config member del " + queue + " " + uuid);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private CompletableFuture<ApiResponse> api(String command) {
        return apiExecutor.apply(command);
    }
}
