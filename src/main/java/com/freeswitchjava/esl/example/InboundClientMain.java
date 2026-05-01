package com.freeswitchjava.esl.example;

import com.freeswitchjava.esl.event.EventName;
import com.freeswitchjava.esl.inbound.InboundClient;
import com.freeswitchjava.esl.inbound.InboundClientConfig;

import java.util.concurrent.CountDownLatch;

/**
 * Minimal runnable inbound client example.
 *
 * <p>Run with system properties:
 * <pre>
 *   -Desl.host=127.0.0.1 -Desl.port=8021 -Desl.password=ClueCon
 * </pre>
 */
public final class InboundClientMain {

    private InboundClientMain() {}

    public static void main(String[] args) throws Exception {
        String host = System.getProperty("esl.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("esl.port", "8021"));
        String password = System.getProperty("esl.password", "ClueCon");

        InboundClient client = InboundClient.create(
                InboundClientConfig.builder()
                        .host(host)
                        .port(port)
                        .password(password)
                        .autoReconnect(true)
                        .restoreSubscriptions(true)
                        .build());

        CountDownLatch shutdown = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.close();
            shutdown.countDown();
        }, "esl-example-shutdown"));

        client.login();
        client.subscribe(
                EventName.CHANNEL_ANSWER,
                EventName.CHANNEL_HANGUP,
                EventName.DTMF,
                EventName.BACKGROUND_JOB,
                EventName.HEARTBEAT
        ).join();
        client.addEventListener(new LoggingEventListener());

        System.out.printf("Connected to %s:%d and listening for events.%n", host, port);
        shutdown.await();
    }
}
