package com.freeswitchjava.esl.live;

import com.freeswitchjava.esl.event.AbstractEslEventListener;
import com.freeswitchjava.esl.event.ChannelCreateEvent;
import com.freeswitchjava.esl.event.ChannelDestroyEvent;
import com.freeswitchjava.esl.event.ChannelEvent;
import com.freeswitchjava.esl.inbound.InboundClient;
import com.freeswitchjava.esl.model.EslEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Maintains a real-time map of all active {@link LiveChannel}s by subscribing to channel events.
 *
 * <p>Attach to an {@link InboundClient} after subscribing to events. The manager listens
 * for all channel lifecycle events and keeps {@link LiveChannel} objects up to date automatically.
 *
 * <pre>{@code
 * client.subscribe(EventName.ALL).join();
 *
 * LiveChannelManager manager = LiveChannelManager.attach(client);
 * manager.onChannelCreated(ch -> log.info("New call: {}", ch.getCallerIdNumber()));
 * manager.onChannelDestroyed(ch -> log.info("Call ended: {} cause={}", ch.getUniqueId(), ch.getHangupCause()));
 *
 * // At any time:
 * Collection<LiveChannel> active = manager.getChannels();
 * Optional<LiveChannel>   ch     = manager.getChannel("some-uuid");
 * }</pre>
 */
public final class LiveChannelManager {

    private static final Logger log = LoggerFactory.getLogger(LiveChannelManager.class);

    private final ConcurrentHashMap<String, LiveChannel> channels = new ConcurrentHashMap<>();

    private volatile Consumer<LiveChannel> onCreated;
    private volatile Consumer<LiveChannel> onDestroyed;

    private LiveChannelManager() {}

    /**
     * Attaches a new {@code LiveChannelManager} to the given client.
     * The client must already be connected; subscribe to channel events before calling this.
     */
    public static LiveChannelManager attach(InboundClient client) {
        LiveChannelManager manager = new LiveChannelManager();
        client.addEventListener(new AbstractEslEventListener() {
            @Override
            protected void onChannelCreate(ChannelCreateEvent event) {
                manager.handleCreate(event);
            }
            @Override
            protected void onChannelDestroy(ChannelDestroyEvent event) {
                manager.handleDestroy(event);
            }
            @Override
            protected void onUnhandledEvent(EslEvent event) {
                // Route all remaining channel events to the channel they belong to
                if (event instanceof ChannelEvent ce) {
                    manager.routeToChannel(ce);
                }
            }
        });
        log.info("[LIVE] LiveChannelManager attached");
        return manager;
    }

    // ── Lifecycle callbacks ───────────────────────────────────────────────────

    /** Called on the event-dispatch thread whenever a new channel is created. */
    public LiveChannelManager onChannelCreated(Consumer<LiveChannel> callback) {
        this.onCreated = callback;
        return this;
    }

    /** Called on the event-dispatch thread whenever a channel is fully destroyed. */
    public LiveChannelManager onChannelDestroyed(Consumer<LiveChannel> callback) {
        this.onDestroyed = callback;
        return this;
    }

    // ── Channel access ────────────────────────────────────────────────────────

    /** Returns a snapshot of all currently tracked channels (including recently hung-up). */
    public Collection<LiveChannel> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    /** Returns the channel for the given UUID, if it is currently tracked. */
    public Optional<LiveChannel> getChannel(String uuid) {
        return Optional.ofNullable(channels.get(uuid));
    }

    /** Number of active (non-destroyed) channels currently tracked. */
    public long getActiveCount() {
        return channels.values().stream().filter(LiveChannel::isActive).count();
    }

    // ── Internal event handlers ───────────────────────────────────────────────

    private void handleCreate(ChannelCreateEvent event) {
        String uuid = event.getUniqueId();
        if (uuid == null) return;
        LiveChannel ch = new LiveChannel(event);
        channels.put(uuid, ch);
        log.debug("[LIVE] [CREATE] uuid=[{}] from=[{}]", uuid, ch.getCallerIdNumber());
        Consumer<LiveChannel> cb = onCreated;
        if (cb != null) {
            try { cb.accept(ch); } catch (Exception e) {
                log.error("[LIVE] onChannelCreated callback failed", e);
            }
        }
    }

    private void handleDestroy(ChannelDestroyEvent event) {
        String uuid = event.getUniqueId();
        if (uuid == null) return;
        LiveChannel ch = channels.remove(uuid);
        if (ch != null) {
            ch.applyEvent(event);
            log.debug("[LIVE] [DESTROY] uuid=[{}] cause=[{}]", uuid, ch.getHangupCause());
            Consumer<LiveChannel> cb = onDestroyed;
            if (cb != null) {
                try { cb.accept(ch); } catch (Exception e) {
                    log.error("[LIVE] onChannelDestroyed callback failed", e);
                }
            }
        }
    }

    private void routeToChannel(EslEvent event) {
        String uuid = event.getUniqueId();
        if (uuid == null) return;
        LiveChannel ch = channels.get(uuid);
        if (ch != null) ch.applyEvent(event);
    }
}
