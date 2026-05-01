package com.freeswitchjava.esl.outbound;

import com.freeswitchjava.esl.model.EslEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link OutboundServer}.
 * The test acts as a FreeSWITCH client connecting to our server.
 */
@Timeout(10)
class OutboundServerIntegrationTest {

    private OutboundServer server;
    private static final int PORT = 18_084; // test port

    @BeforeEach
    void setUp() {}

    @AfterEach
    void tearDown() {
        if (server != null) server.close();
    }

    @Test
    void session_factory_is_called_on_connect() throws Exception {
        CompletableFuture<OutboundSession> sessionReceived = new CompletableFuture<>();

        server = OutboundServer.create(
                OutboundServerConfig.builder().port(PORT).build(),
                sessionReceived::complete
        );
        server.start().get(5, TimeUnit.SECONDS);

        // Simulate FreeSWITCH connecting
        try (Socket fsClient = new Socket("localhost", PORT)) {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(fsClient.getOutputStream(), StandardCharsets.UTF_8));

            // FreeSWITCH sends the connect response with channel data
            String channelBody = "Content-Type: command/reply\n" +
                                 "Reply-Text: +OK\n" +
                                 "Unique-ID: test-uuid-001\n\n";
            writer.write(channelBody);
            writer.flush();

            OutboundSession session = sessionReceived.get(5, TimeUnit.SECONDS);
            assertThat(session).isNotNull();
        }
    }

    @Test
    void session_connect_returns_channel_data() throws Exception {
        AtomicReference<EslEvent> channelDataRef = new AtomicReference<>();

        server = OutboundServer.create(
                OutboundServerConfig.builder().port(PORT + 1).build(),
                session -> {
                    EslEvent data = session.connect().join();
                    channelDataRef.set(data);
                }
        );
        server.start().get(5, TimeUnit.SECONDS);

        try (Socket fsClient = new Socket("localhost", PORT + 1)) {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(fsClient.getOutputStream(), StandardCharsets.UTF_8));
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(fsClient.getInputStream(), StandardCharsets.UTF_8));

            // Read the "connect" command sent by our session
            String connectCommand = reader.readLine(); // "connect"
            reader.readLine(); // empty line (end of command)

            // Send back channel data
            String body = "Event-Name: CHANNEL_DATA\nUnique-ID: call-789\nChannel-State: CS_EXECUTE\n\n";
            // myevents command will also come in after connect — respond to it
            String response = "Content-Type: command/reply\n" +
                              "Reply-Text: +OK\n" +
                              "Unique-ID: call-789\n\n";
            writer.write(response);
            // Also respond to myevents
            writer.write("Content-Type: command/reply\nReply-Text: +OK\n\n");
            writer.flush();

            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                    .until(() -> channelDataRef.get() != null);

            assertThat(connectCommand).isEqualTo("connect");
        }
    }
}
