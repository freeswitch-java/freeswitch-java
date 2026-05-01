package com.freeswitchjava.esl.connection;

import com.freeswitchjava.esl.event.EslEventListener;
import com.freeswitchjava.esl.event.EventBus;
import com.freeswitchjava.esl.event.EventName;
import com.freeswitchjava.esl.inbound.ConnectionState;
import com.freeswitchjava.esl.inbound.EslLoginException;
import com.freeswitchjava.esl.inbound.InboundClient;
import com.freeswitchjava.esl.inbound.InboundClientConfig;
import com.freeswitchjava.esl.model.ApiResponse;
import com.freeswitchjava.esl.model.CommandReply;
import com.freeswitchjava.esl.model.EslEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Default {@link EslConnection} implementation backed by {@link InboundClient}.
 */
public final class DefaultEslConnection implements EslConnection {

    private final InboundClient client;

    public DefaultEslConnection(InboundClientConfig config) {
        this(InboundClient.create(config));
    }

    public DefaultEslConnection(InboundClient client) {
        this.client = client;
    }

    public static DefaultEslConnection create(InboundClientConfig config) {
        return new DefaultEslConnection(config);
    }

    /** Exposes the underlying client for advanced APIs not present on {@link EslConnection}. */
    public InboundClient unwrap() {
        return client;
    }

    @Override
    public String getHost() {
        return client.getConfig().getHost();
    }

    @Override
    public int getPort() {
        return client.getConfig().getPort();
    }

    @Override
    public ConnectionState getState() {
        return client.getState();
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public void login() throws EslLoginException {
        client.login();
    }

    @Override
    public void login(long timeout, TimeUnit unit) throws EslLoginException {
        client.login(timeout, unit);
    }

    @Override
    public void logoff() {
        client.close();
    }

    @Override
    public CompletableFuture<CommandReply> sendCommand(String command) {
        return client.sendCommand(command);
    }

    @Override
    public CompletableFuture<ApiResponse> api(String command) {
        return client.api(command);
    }

    @Override
    public CompletableFuture<ApiResponse> bgapi(String command) {
        return client.bgapi(command);
    }

    @Override
    public CompletableFuture<CommandReply> subscribe(EventName... eventNames) {
        return client.subscribe(eventNames);
    }

    @Override
    public CompletableFuture<CommandReply> subscribeXml(EventName... eventNames) {
        return client.subscribeXml(eventNames);
    }

    @Override
    public EventBus.EventRegistration addEventListener(EslEventListener listener) {
        return client.addEventListener(listener);
    }

    @Override
    public EventBus.EventRegistration addEventListener(EventName eventName, Consumer<EslEvent> listener) {
        return client.addEventListener(eventName, listener);
    }

    @Override
    public void removeEventListener(EventBus.EventRegistration registration) {
        client.removeEventListener(registration);
    }

    @Override
    public void awaitClose() throws InterruptedException {
        client.awaitClose();
    }

    @Override
    public void close() {
        client.close();
    }
}
