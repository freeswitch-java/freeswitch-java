package com.freeswitchjava.esl.connection;

import com.freeswitchjava.esl.event.EslEventListener;
import com.freeswitchjava.esl.event.EventBus;
import com.freeswitchjava.esl.event.EventName;
import com.freeswitchjava.esl.inbound.ConnectionState;
import com.freeswitchjava.esl.inbound.EslLoginException;
import com.freeswitchjava.esl.model.ApiResponse;
import com.freeswitchjava.esl.model.CommandReply;
import com.freeswitchjava.esl.model.EslEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Stable, connection-oriented abstraction for FreeSWITCH ESL clients.
 *
 * <p>This is useful for applications that prefer depending on an interface
 * (similar to AMI-style connection abstractions) rather than a concrete client class.
 */
public interface EslConnection extends AutoCloseable {

    String getHost();

    int getPort();

    ConnectionState getState();

    boolean isConnected();

    void login() throws EslLoginException;

    void login(long timeout, TimeUnit unit) throws EslLoginException;

    void logoff();

    CompletableFuture<CommandReply> sendCommand(String command);

    CompletableFuture<ApiResponse> api(String command);

    CompletableFuture<ApiResponse> bgapi(String command);

    CompletableFuture<CommandReply> subscribe(EventName... eventNames);

    CompletableFuture<CommandReply> subscribeXml(EventName... eventNames);

    EventBus.EventRegistration addEventListener(EslEventListener listener);

    EventBus.EventRegistration addEventListener(EventName eventName, Consumer<EslEvent> listener);

    void removeEventListener(EventBus.EventRegistration registration);

    void awaitClose() throws InterruptedException;

    @Override
    void close();
}
