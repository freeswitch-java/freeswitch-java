package com.freeswitchjava.esl.inbound;

import com.freeswitchjava.esl.event.EventName;
import com.freeswitchjava.esl.model.ApiResponse;
import com.freeswitchjava.esl.model.CommandReply;
import com.freeswitchjava.esl.model.EslEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link InboundClient} using an embedded mock FreeSWITCH server.
 */
@Timeout(10)
class InboundClientIntegrationTest {

    private ServerSocket mockServer;
    private ExecutorService serverExecutor;
    private InboundClient client;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new ServerSocket(0); // random free port
        port = mockServer.getLocalPort();
        serverExecutor = Executors.newCachedThreadPool();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (client != null) client.close();
        mockServer.close();
        serverExecutor.shutdownNow();
    }

    // --- Helper: accept one connection and write scripted ESL frames ---

    private void acceptAndScript(ThrowingConsumer<MockEslServer> script) {
        serverExecutor.submit(() -> {
            try (Socket socket = mockServer.accept()) {
                MockEslServer server = new MockEslServer(socket);
                script.accept(server);
            } catch (Exception e) {
                // Normal on test teardown
            }
        });
    }

    // --- Tests ---

    @Test
    void connects_and_authenticates_successfully() throws Exception {
        acceptAndScript(server -> {
            server.sendAuthRequest();
            assertThat(server.readLine()).startsWith("auth ");
            server.sendCommandReply("+OK accepted");
        });

        client = InboundClient.create(
                InboundClientConfig.builder().host("localhost").port(port).build());
        client.connect().get(5, TimeUnit.SECONDS);
    }

    @Test
    void throws_on_wrong_password() throws Exception {
        acceptAndScript(server -> {
            server.sendAuthRequest();
            server.readLine(); // consume auth command
            server.sendCommandReply("-ERR invalid");
        });

        client = InboundClient.create(
                InboundClientConfig.builder().host("localhost").port(port).password("wrong").build());

        assertThatThrownBy(() -> client.connect().get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(SecurityException.class)
                .hasMessageContaining("auth failed");
    }

    @Test
    void sends_api_command_and_receives_response() throws Exception {
        acceptAndScript(server -> {
            server.sendAuthRequest();
            server.readLine(); // auth
            server.sendCommandReply("+OK accepted");
            server.readLine(); // api status
            server.sendApiResponse("FreeSWITCH Status\nUP 0 years, 0 days, 0:00:10");
        });

        client = authenticated();
        ApiResponse response = client.api("status").get(5, TimeUnit.SECONDS);
        assertThat(response.getBody()).contains("FreeSWITCH Status");
    }

    @Test
    void sends_command_and_receives_reply() throws Exception {
        acceptAndScript(server -> {
            server.sendAuthRequest();
            server.readLine(); // auth
            server.sendCommandReply("+OK accepted");
            server.readLine(); // event plain ALL
            server.sendCommandReply("+OK event listener enabled plain");
        });

        client = authenticated();
        CommandReply reply = client.subscribe(EventName.ALL).get(5, TimeUnit.SECONDS);
        assertThat(reply.isOk()).isTrue();
    }

    @Test
    void sends_xml_subscribe_command_and_receives_reply() throws Exception {
        acceptAndScript(server -> {
            server.sendAuthRequest();
            server.readLine(); // auth
            server.sendCommandReply("+OK accepted");
            String cmd = server.readLine(); // event xml ALL
            assertThat(cmd).isEqualTo("event xml ALL");
            server.sendCommandReply("+OK event listener enabled xml");
        });

        client = authenticated();
        CommandReply reply = client.subscribeXml(EventName.ALL).get(5, TimeUnit.SECONDS);
        assertThat(reply.isOk()).isTrue();
    }

    @Test
    void delivers_events_to_registered_listeners() throws Exception {
        String eventBody = "Event-Name: CHANNEL_ANSWER\nUnique-ID: call-123\n\n";
        acceptAndScript(server -> {
            server.sendAuthRequest();
            server.readLine(); // auth
            server.sendCommandReply("+OK accepted");
            Thread.sleep(100); // small delay so listener is registered
            server.sendEventPlain(eventBody);
        });

        client = authenticated();
        List<EslEvent> received = new CopyOnWriteArrayList<>();
        client.addEventListener(EventName.CHANNEL_ANSWER, received::add);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> received.size() == 1);

        assertThat(received.get(0).getEventName()).isEqualTo(EventName.CHANNEL_ANSWER.wireValue());
        assertThat(received.get(0).getUniqueId()).isEqualTo("call-123");
    }

    @Test
    void delivers_xml_events_to_registered_listeners() throws Exception {
        String eventXml = """
                <event>
                  <headers>
                    <Event-Name>CHANNEL_ANSWER</Event-Name>
                    <Unique-ID>xml-123</Unique-ID>
                  </headers>
                  <body></body>
                </event>
                """;
        acceptAndScript(server -> {
            server.sendAuthRequest();
            server.readLine(); // auth
            server.sendCommandReply("+OK accepted");
            Thread.sleep(100);
            server.sendEventXml(eventXml);
        });

        client = authenticated();
        List<EslEvent> received = new CopyOnWriteArrayList<>();
        client.addEventListener(EventName.CHANNEL_ANSWER, received::add);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> received.size() == 1);

        assertThat(received.get(0).getEventName()).isEqualTo(EventName.CHANNEL_ANSWER.wireValue());
        assertThat(received.get(0).getUniqueId()).isEqualTo("xml-123");
    }

    @Test
    void wildcard_listener_receives_all_events() throws Exception {
        String body1 = "Event-Name: CHANNEL_ANSWER\nUnique-ID: u1\n\n";
        String body2 = "Event-Name: CHANNEL_HANGUP\nUnique-ID: u1\n\n";
        acceptAndScript(server -> {
            server.sendAuthRequest();
            server.readLine(); // auth
            server.sendCommandReply("+OK accepted");
            Thread.sleep(100);
            server.sendEventPlain(body1);
            Thread.sleep(50);
            server.sendEventPlain(body2);
        });

        client = authenticated();
        List<EslEvent> received = new CopyOnWriteArrayList<>();
        client.addEventListener("*", received::add);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> received.size() == 2);
        assertThat(received).extracting(EslEvent::getEventName)
                .containsExactly(EventName.CHANNEL_ANSWER.wireValue(), EventName.CHANNEL_HANGUP.wireValue());
    }

    @Test
    void reconnects_with_exponential_backoff_and_restores_subscriptions() throws Exception {
        // First connection: auth OK, then close abruptly
        serverExecutor.submit(() -> {
            try (Socket s1 = mockServer.accept()) {
                MockEslServer srv = new MockEslServer(s1);
                srv.sendAuthRequest();
                srv.readLine(); // auth
                srv.sendCommandReply("+OK accepted");
                srv.readLine(); // event plain ALL (subscription)
                srv.sendCommandReply("+OK event listener enabled plain");
                // abruptly close — triggers reconnect
            } catch (Exception ignored) {}

            // Second connection: accept and auth again
            try (Socket s2 = mockServer.accept()) {
                MockEslServer srv = new MockEslServer(s2);
                srv.sendAuthRequest();
                srv.readLine(); // auth
                srv.sendCommandReply("+OK accepted");
                // subscription should be auto-restored
                String restoredSub = srv.readLine();
                assertThat(restoredSub).startsWith("event plain");
                srv.sendCommandReply("+OK event listener enabled plain");
            } catch (Exception ignored) {}
        });

        client = InboundClient.create(
                InboundClientConfig.builder()
                        .host("localhost").port(port)
                        .autoReconnect(true)
                        .reconnectInitialDelayMs(50)   // fast for tests
                        .reconnectMaxDelayMs(100)
                        .restoreSubscriptions(true)
                        .build());

        client.connect().get(5, TimeUnit.SECONDS);
        client.subscribe(EventName.ALL).get(5, TimeUnit.SECONDS);

        // Wait for reconnect to complete (second auth)
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> client.channel != null && client.channel.isActive());
    }

    @Test
    void gives_up_after_max_reconnect_attempts() throws Exception {
        acceptAndScript(server -> {
            server.sendAuthRequest();
            server.readLine();
            server.sendCommandReply("+OK accepted");
            // close immediately
        });

        java.util.concurrent.atomic.AtomicInteger reconnectCount = new java.util.concurrent.atomic.AtomicInteger();

        client = InboundClient.create(
                InboundClientConfig.builder()
                        .host("localhost").port(port)
                        .autoReconnect(true)
                        .reconnectInitialDelayMs(20)
                        .reconnectMaxDelayMs(50)
                        .maxReconnectAttempts(2)
                        .build())
                .onReconnect(reconnectCount::incrementAndGet);

        client.connect().get(5, TimeUnit.SECONDS);

        // After 2 failed attempts the client stops retrying
        Thread.sleep(500);
        assertThat(reconnectCount.get()).isLessThanOrEqualTo(2);
    }

    // --- Helpers ---

    private InboundClient authenticated() throws Exception {
        InboundClient c = InboundClient.create(
                InboundClientConfig.builder().host("localhost").port(port).build());
        c.login(5, TimeUnit.SECONDS);
        return c;
    }

    @FunctionalInterface
    interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }

    /**
     * Simple scripted mock FreeSWITCH ESL server for testing.
     */
    static class MockEslServer {
        private final BufferedWriter writer;
        private final java.io.BufferedReader reader;

        MockEslServer(Socket socket) throws IOException {
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            this.reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        }

        void send(String frame) throws IOException {
            writer.write(frame);
            writer.flush();
        }

        void sendAuthRequest() throws IOException {
            send("Content-Type: auth/request\n\n");
        }

        void sendCommandReply(String replyText) throws IOException {
            send("Content-Type: command/reply\nReply-Text: " + replyText + "\n\n");
        }

        void sendApiResponse(String body) throws IOException {
            send("Content-Type: api/response\nContent-Length: " + body.length() + "\n\n" + body);
        }

        void sendEventPlain(String eventBody) throws IOException {
            send("Content-Type: text/event-plain\nContent-Length: " + eventBody.length() + "\n\n" + eventBody);
        }

        void sendEventXml(String eventXml) throws IOException {
            send("Content-Type: text/event-xml\nContent-Length: " + eventXml.length() + "\n\n" + eventXml);
        }

        /** Reads a complete command (lines until empty line). */
        String readLine() throws IOException {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) break;
                if (sb.length() > 0) sb.append('\n');
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
