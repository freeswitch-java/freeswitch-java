# FreeSWITCH Java ESL Client

Java client library for the [FreeSWITCH Event Socket Layer (ESL)](https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Client-and-Developer-Interfaces/Event-Socket-Library/).

**Technology stack**: Netty 4.1 · Java 21 · CompletableFuture async API · Spring Boot starter

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Installation](#installation)
3. [Core Concepts](#core-concepts)
4. [Inbound Client](#inbound-client)
5. [Outbound Server](#outbound-server)
6. [Event Handling](#event-handling)
7. [Commands & API](#commands--api)
8. [Spring Boot Integration](#spring-boot-integration)
9. [Advanced Features](#advanced-features)
10. [Sub-APIs](#sub-apis)
11. [Configuration](#configuration)
12. [Troubleshooting](#troubleshooting)

---

## Getting Started

### What is ESL?

The **Event Socket Layer** (ESL) is FreeSWITCH's primary external control interface. It allows applications to:
- **Connect inbound**: Monitor events, issue commands, control calls
- **Accept outbound connections**: Handle call routing, IVR, call control in real-time

This library provides fully-typed, async-first Java bindings for ESL.

### 30-Second Example

```java
// Connect to FreeSWITCH
InboundClient client = InboundClient.create(
    InboundClientConfig.builder()
        .host("localhost")
        .port(8021)
        .password("ClueCon")
        .autoReconnect(true)
        .build());

client.login();
client.subscribe(EventName.CHANNEL_ANSWER, EventName.CHANNEL_HANGUP).join();

// Listen for events
client.addEventListener(new AbstractEslEventListener() {
    @Override
    public void onChannelAnswer(ChannelAnswerEvent event) {
        System.out.println("Call answered: " + event.getCallerIdNumber());
    }
});

// Send commands
client.api(new OriginateCommand("sofia/default/1001@domain.com")
    .extension("2000")
    .context("default")
    .timeout(30)).join();

// Graceful shutdown
client.shutdown();
```

---

## Installation

### Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.freeswitch-java</groupId>
    <artifactId>freeswitch-java</artifactId>
    <version>v0.2.0</version>
</dependency>
```

### Maven (Local Build)

```bash
git clone https://github.com/freeswitch-java/freeswitch-java.git
cd freeswitch-java
mvn install -DskipTests
```

Then add to `pom.xml`:
```xml
<dependency>
    <groupId>com.freeswitchjava</groupId>
    <artifactId>freeswitchjava</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Requirements

- Java 21+
- FreeSWITCH with `mod_event_socket` enabled (default port: 8021)

---

## Core Concepts

### Async-First API

All I/O is non-blocking using **CompletableFuture**:

```java
// These calls return immediately; execution is async
CompletableFuture<ApiResponse> future = client.api("status");

// Block and wait for result
ApiResponse response = future.join();

// Or use callbacks
future.thenAccept(response -> System.out.println(response.getBody()));
```

### Event-Driven Architecture

The client discovers events through **listeners**:

```java
client.addEventListener(event -> {
    if (event instanceof ChannelAnswerEvent) {
        ChannelAnswerEvent answer = (ChannelAnswerEvent) event;
        // handle...
    }
});
```

Or use typed subclass for pattern matching:

```java
client.addEventListener(new AbstractEslEventListener() {
    @Override
    public void onChannelAnswer(ChannelAnswerEvent event) { }
    
    @Override
    public void onChannelHangup(ChannelHangupEvent event) { }
    
    @Override
    public void onDtmf(DtmfEvent event) { }
});
```

### Connection States

The client tracks connection state and automatically reconnects:

```java
enum ConnectionState {
    DISCONNECTED,   // Not connected
    CONNECTING,     // Connection attempt in progress
    CONNECTED,      // Authenticated and ready
    RECONNECTING,   // Lost connection, retrying
    CLOSED          // Shutdown requested
}
```

Listen for state changes:
```java
client.onStateChange(state -> {
    if (state == ConnectionState.CONNECTED) {
        System.out.println("Ready for commands");
    }
});
```

---

## Inbound Client

The **InboundClient** connects to FreeSWITCH's event socket port and issues commands.

### Lifecycle

```java
// 1. Create configuration
InboundClientConfig config = InboundClientConfig.builder()
    .host("localhost")
    .port(8021)
    .password("ClueCon")
    .autoReconnect(true)
    .reconnectInitialDelayMs(1000)
    .reconnectMaxDelayMs(30000)
    .maxReconnectAttempts(10)
    .build();

// 2. Create client
InboundClient client = InboundClient.create(config);

// 3a. Setup listeners (before connect)
client.addEventListener(event -> { ... });

// 3b. Start in blocking mode (recommended for main thread)
client.startup();  // Blocks until shutdown via Ctrl+C
```

Or in non-blocking mode:

```java
// 3. Connect explicitly
client.connect().join();

// 4. Subscribe to events
client.subscribe(EventName.CHANNEL_ANSWER, EventName.CHANNEL_HANGUP).join();

// 5. Use client
client.api("status").join();

// 6. Graceful shutdown
client.shutdown();
```

### Login & Authentication

Login happens automatically on `connect()`:

```java
client.connect().join();  // Authenticates with password from config
```

To verify login succeeded, check connection state:
```java
if (client.getConnectionState() == ConnectionState.CONNECTED) {
    System.out.println("Authenticated");
}
```

### Subscribe to Events

Choose which events to receive:

```java
// Subscribe to specific events
client.subscribe(
    EventName.CHANNEL_ANSWER,
    EventName.CHANNEL_HANGUP,
    EventName.DTMF
).join();

// Subscribe to all events (verbose; use cautiously)
client.subscribe(EventName.ALL).join();

// Enable heartbeat to detect dead connections
client.subscribe(EventName.HEARTBEAT).join();
```

### Send API Commands

All FreeSWITCH API commands are available as typed classes:

```java
// Hangup a call
client.api(new HangupCommand("channel-uuid")).join();

// Originate a call
client.api(new OriginateCommand("sofia/default/1001@domain.com")
    .extension("2000")
    .context("default")
    .timeout(30)
    .variables(Map.of("foo", "bar"))
).join();

// Raw command (if no typed class exists)
client.api(new RawApiCommand("sofia status")).join();
```

### Background API (`bgapi`)

Long-running commands are non-blocking:

```java
// Returns immediately with job ID
CompletableFuture<CommandReply> future = 
    client.bgapi("originate sofia/default/user1 &bridge(sofia/default/user2)");

future.thenAccept(reply -> {
    String jobId = reply.getJobUuid();
    System.out.println("Job started: " + jobId);
});
```

Listen for job completion:
```java
client.addEventListener(new AbstractEslEventListener() {
    @Override
    public void onBackgroundJob(BackgroundJobEvent event) {
        System.out.println("Job " + event.getJobUuid() + " completed");
        System.out.println("Result: " + event.getResult());
    }
});
```

### Reconnect Strategy

Auto-reconnect is configurable:

```java
InboundClientConfig config = InboundClientConfig.builder()
    .host("localhost")
    .port(8021)
    .password("ClueCon")
    .autoReconnect(true)
    .reconnectInitialDelayMs(1000)    // Start with 1s delay
    .reconnectMaxDelayMs(30000)       // Back off to max 30s
    .maxReconnectAttempts(10)         // Give up after 10 tries
    .build();
```

Register a callback to re-setup after reconnect:

```java
client.onReconnect(() -> {
    // Re-subscribe, re-apply filters, etc.
    client.subscribe(EventName.CHANNEL_ANSWER, EventName.CHANNEL_HANGUP).join();
});
```

---

## Outbound Server

FreeSWITCH can initiate connections to your application for **call control** and **IVR**. The **OutboundServer** accepts these connections.

### Quick Start

```java
// Create server on port 8084
OutboundServer.create(
    OutboundServerConfig.builder()
        .port(8084)
        .build(),
    session -> {
        // Handle incoming call
        session.connect().join();
        session.answer().join();
        session.playback("/var/sounds/welcome.wav").join();
        
        // Get DTMF input
        String dtmfDigits = session.playRecording(
            "/var/sounds/prompt.wav",
            5,  // max 5 digits
            60  // timeout 60s
        ).join();
        
        session.hangup().join();
    }
).startup();
```

### FreeSWITCH Dialplan Configuration

Tell FreeSWITCH to route calls to your application:

```xml
<!-- conf/dialplan/default.xml -->
<extension name="outbound-ivr">
  <condition field="destination_number" expression="^999$">
    <action application="socket" 
            data="192.168.1.100:8084 async full"/>
  </condition>
</extension>
```

Parameters:
- `async` — FreeSWITCH doesn't block; your app controls the channel
- `full` — All channel info is sent to your application

### Session Lifecycle

Each incoming connection creates a session:

```java
OutboundServer server = OutboundServer.create(
    OutboundServerConfig.builder().port(8084).build(),
    session -> {
        // 1. You must call connect() to acknowledge
        session.connect().join();
        
        // 2. Control the call
        session.answer().join();
        session.say("Hello").join();
        
        // 3. Get caller info
        String callerId = session.getEvent().getCallerIdNumber();
        String dialPlan = session.getEvent().getDialPlan();
        
        // 4. Hangup
        session.hangup().join();
        
        // Session closes when callback returns
    }
).startup();
```

### Key Methods

```java
// Connection & channel
session.connect();                      // Acknowledge incoming call
session.answer();                       // Answer the call
session.hangup();                       // Hangup
session.disconnect();                   // Disconnect session

// Audio playback
session.playback("/var/sounds/hello.wav");
session.playback("silence:500");        // 500ms silence
session.playback("tone_stream://(...)", 10000);  // DTMF tones

// DTMF input
session.playRecording("/var/sounds/prompt.wav", 5, 60);
session.readDtmf(5, 60);                // Read 5 digits, 60s timeout

// Channel operations
session.bridge("sofia/default/1001@domain.com");
session.transfer("2000", "default");
session.setVariable("my_var", "value");

// Media
session.hold();
session.unhold();
session.record("/var/records/call.wav");
session.startDetectingSpeech();
```

### Routing with OutboundSessionRouter

For call center scenarios, route sessions to different handlers:

```java
OutboundSessionRouter router = new OutboundSessionRouter();

// Route by destination number
router.addRoute("999", session -> handleIvr(session));
router.addRoute("1001", session -> handleAA(session));

// Default handler
router.setDefaultHandler(session -> handleGeneric(session));

OutboundServer server = OutboundServer.create(
    OutboundServerConfig.builder().port(8084).build(),
    router
).startup();
```

---

## Event Handling

### Available Events

Channel lifecycle:
- `ChannelCreateEvent` — New channel created
- `ChannelAnswerEvent` — Channel answered
- `ChannelStateEvent` — Channel state changed
- `ChannelHangupEvent` — Channel hung up
- `ChannelHangupCompleteEvent` — Hangup completed
- `ChannelDestroyEvent` — Channel destroyed

Call media:
- `ChannelBridgeEvent` — Channels bridged (call connected)
- `ChannelUnbridgeEvent` — Bridge broken
- `PlaybackStartEvent`, `PlaybackStopEvent` — Audio playback
- `RecordStartEvent`, `RecordStopEvent` — Recording
- `DtmfEvent` — DTMF digit received

Advanced:
- `ChannelOutgoingEvent` — Outgoing call initiated
- `ChannelProgressEvent` — Ringing/progress media
- `ChannelProgressMediaEvent` — Early media received
- `ChannelOriginateEvent` — Originate command result
- `BackgroundJobEvent` — bgapi job result
- `HeartbeatEvent` — Heartbeat tick
- `DetectedSpeechEvent` — Speech detected (ASR)
- `CustomEvent` — Application-defined event
- `CdrEvent` — Call detail record

### Listener Patterns

**Lambda listener** (simple):
```java
client.addEventListener(event -> {
    if (event instanceof ChannelAnswerEvent) {
        System.out.println("Answered!");
    }
});
```

**Typed listener** (pattern matching):
```java
client.addEventListener(new AbstractEslEventListener() {
    @Override
    public void onChannelAnswer(ChannelAnswerEvent event) {
        System.out.println("Answered: " + event.getCallerIdNumber());
    }
    
    @Override
    public void onChannelHangup(ChannelHangupEvent event) {
        System.out.println("Hangup cause: " + event.getHangupCauseEnum());
    }
    
    @Override
    public void onDtmf(DtmfEvent event) {
        System.out.println("DTMF: " + event.getDtmfDigit());
    }
});
```

**Named listener** (when handler lifecycle must be managed):
```java
EslEventListener handler = new MyCallHandler();
client.addEventListener("my-handler", handler);
client.removeEventListener("my-handler");
```

### Custom Events

Register handler for custom FreeSWITCH events:

```java
client.addEventListener(new AbstractEslEventListener() {
    @Override
    public void onCustomEvent(CustomEvent event) {
        String eventType = event.getEventSubType();
        Map<String, String> data = event.getEventData();
        
        if ("my_event".equals(eventType)) {
            System.out.println("Custom event: " + data);
        }
    }
});
```

Or register a custom event class:

```java
EslEventFactory.registerCustomEventClass("my_event", MyCustomEvent.class);
```

### Event Filtering

Reduce event volume with filters:

```java
// Only receive events for a specific UUID
client.filter(FilterCommand.builder()
    .eventUuid("abc-123")
    .build()).join();

// Filter by multiple criteria
client.filter(FilterCommand.builder()
    .add(FilterCommand.Criterion.eventUuid("abc-123"))
    .add(FilterCommand.Criterion.callDirection("outbound"))
    .build()).join();
```

### Backpressure & Threading

By default, listeners run on Netty's I/O thread. For CPU-intensive work, use **EslEventListenerProxy**:

```java
EslEventListener handler = new AbstractEslEventListener() {
    @Override
    public void onChannelAnswer(ChannelAnswerEvent event) {
        // This runs on a separate executor (doesn't block I/O)
        performHeavyWork();
    }
};

// Wrap with bounded queue (max 1000 pending events)
EslEventListener proxy = new EslEventListenerProxy(
    handler,
    1000,           // queue size
    new ThreadPerTaskExecutor()  // executor
);

client.addEventListener(proxy);
```

---

## Commands & API

### Typed API Commands

All FreeSWITCH API commands have corresponding Java classes in the `api` package.

#### Call Control

```java
// Answer/hangup
client.api(new AnswerCommand("uuid")).join();
client.api(new HangupCommand("uuid")).join();
client.api(new PreAnswerCommand("uuid")).join();

// Originate (make a call)
client.api(new OriginateCommand("sofia/default/1001@domain.com")
    .extension("2000")
    .context("default")
    .timeout(30)
    .variables(Map.of(
        "foo", "bar",
        "callback_on_answer", "true"
    ))
).join();

// Transfer/bridge
client.api(new TransferCommand("uuid", "2000", "default")).join();
client.api(new BridgeCommand("uuid", "sofia/default/1001@domain.com")).join();
```

#### Media Control

```java
// Playback
client.api(new PlaybackCommand("uuid", "/var/sounds/hello.wav")).join();

// Record
client.api(new RecordCommand("uuid", "/var/recordings/call.wav")
    .limit(600000)  // 10 min max
).join();

// DTMF
client.api(new SendDtmfCommand("uuid", "1234#")).join();
client.api(new FlushDtmfCommand("uuid")).join();

// Audio effects
client.api(new AudioCommand("uuid", "start")).join();
client.api(new VolumeCommand("uuid", "+10")).join();
```

#### Channel State

```java
// Hold/unhold
client.api(new HoldCommand("uuid")).join();
client.api(new UnholdCommand("uuid")).join();

// Park
client.api(new ParkCommand("uuid")).join();
client.api(new UnparkCommand("uuid")).join();

// Ring
client.api(new RingReadyCommand("uuid")).join();

// Variables
client.api(new SetVarCommand("uuid", "my_var", "value")).join();
client.api(new GetVarCommand("uuid", "my_var")).join();
```

#### Global Operations

```java
// System info
client.api(new StatusCommand()).join();
client.api(new VersionCommand()).join();
client.api(new VersionCommand()).thenAccept(r ->
    System.out.println("FreeSWITCH: " + r.getBody())
).join();

// Global variables
client.api(new GlobalSetVarCommand("my_global", "value")).join();
client.api(new GlobalGetVarCommand("my_global")).join();

// Reload config
client.api(new ReloadXmlCommand()).join();
```

#### Utility

```java
// Hashing
client.api(new Md5Command("hello")).join();

// URL encoding
client.api(new UrlEncodeCommand("hello world")).join();
client.api(new UrlDecodeCommand("hello%20world")).join();

// Limits
client.api(new LimitCommand("db", "my_limit", "1", "per_sec")).join();
client.api(new LimitReleaseCommand("db", "my_limit")).join();
```

### Raw Commands

If a typed class doesn't exist, use `RawApiCommand`:

```java
CommandReply reply = client.api(new RawApiCommand("my_custom_cmd arg1 arg2")).join();
String result = reply.getResponse();
```

### Response Parsing

```java
CommandReply reply = client.api(new StatusCommand()).join();

if (reply.isOk()) {
    String status = reply.getResponse();
    System.out.println(status);
} else {
    System.err.println("Error: " + reply.getErrorMessage());
}
```

For bgapi (background) responses:

```java
CommandReply reply = client.bgapi("originate sofia/default/user &park()").join();
String jobUuid = reply.getJobUuid();
System.out.println("Job scheduled: " + jobUuid);
```

---

## Spring Boot Integration

Annotation-driven ESL configuration and event handling.

### Enable in Spring Boot App

Add dependency:
```xml
<dependency>
    <groupId>com.freeswitchjava</groupId>
    <artifactId>freeswitchjava</artifactId>
    <version>0.2.0</version>
</dependency>
```

Autoconfiguration is enabled automatically. Configure in `application.yml`:

```yaml
freeswitch:
  esl:
    host: localhost
    port: 8021
    password: ClueCon
    auto-reconnect: true
    reconnect-initial-delay-ms: 1000
    reconnect-max-delay-ms: 30000
    max-reconnect-attempts: 10
```

### Event Handler Beans

Use `@EslEventHandler` stereotype to mark event handler beans:

```java
@EslEventHandler
@Component
public class CallEventHandler {

    private static final Logger log = LoggerFactory.getLogger(CallEventHandler.class);

    @OnChannelCreate
    public void onChannelCreate(ChannelCreateEvent event) {
        log.info("Channel created: {}", event.getUniqueId());
    }

    @OnChannelAnswer
    public void onChannelAnswer(ChannelAnswerEvent event) {
        log.info("Call answered: {} -> {}", 
            event.getCallerIdNumber(), 
            event.getCalledNumber());
    }

    @OnChannelHangup
    public void onChannelHangup(ChannelHangupEvent event) {
        log.info("Hangup: {} ({})", 
            event.getUniqueId(),
            event.getHangupCauseEnum());
    }

    @OnDtmf
    public void onDtmf(DtmfEvent event) {
        log.info("DTMF received: {}", event.getDtmfDigit());
    }

    @OnCustomEvent
    public void onCustomEvent(CustomEvent event) {
        log.info("Custom event: {}", event.getEventSubType());
    }
}
```

### Inject InboundClient

```java
@Service
public class CallService {

    @Autowired
    private InboundClient eslClient;

    public void originateCall(String destination) {
        eslClient.api(new OriginateCommand(destination)
            .extension("2000")
            .context("default")
        ).join();
    }

    public void hangupCall(String uuid) {
        eslClient.api(new HangupCommand(uuid)).join();
    }
}
```

### Inject OutboundServer

```java
@Configuration
public class EslServerConfig {

    @Bean
    public OutboundServer outboundServer() {
        return OutboundServer.create(
            OutboundServerConfig.builder()
                .port(8084)
                .build(),
            session -> handleCall(session)
        ).startup();
    }

    private void handleCall(OutboundSession session) {
        try {
            session.connect().join();
            session.answer().join();
            session.playback("/var/sounds/welcome.wav").join();
            session.hangup().join();
        } catch (Exception e) {
            log.error("Error handling outbound call", e);
        }
    }
}
```

### Annotation Reference

| Annotation | Event |
|-----------|-------|
| `@OnChannelCreate` | New channel created |
| `@OnChannelAnswer` | Call answered |
| `@OnChannelHangup` | Call hung up |
| `@OnChannelBridge` | Channels bridged |
| `@OnChannelUnbridge` | Bridge broken |
| `@OnDtmf` | DTMF digit received |
| `@OnPlaybackStart` | Audio playback started |
| `@OnPlaybackStop` | Audio playback stopped |
| `@OnRecordStart` | Recording started |
| `@OnRecordStop` | Recording stopped |
| `@OnHeartbeat` | Heartbeat tick |
| `@OnCustomEvent` | Custom event |
| `@OnBackgroundJob` | bgapi job completed |

---

## Advanced Features

### TLS/SSL

Encrypt communication with FreeSWITCH:

```java
// Client-side (Inbound)
SslContext sslContext = SslContextBuilder.forClient()
    .trustManager(InsecureTrustManagerFactory.INSTANCE)  // For dev only!
    .build();

InboundClientConfig config = InboundClientConfig.builder()
    .host("freeswitch.example.com")
    .port(8021)
    .password("ClueCon")
    .sslContext(sslContext)
    .build();

InboundClient client = InboundClient.create(config);
```

For production, use proper certificate validation:

```java
KeyStore keyStore = KeyStore.getInstance("JKS");
keyStore.load(new FileInputStream("truststore.jks"), "password".toCharArray());

SslContext sslContext = SslContextBuilder.forClient()
    .trustManager(new TrustManagerFactory(keyStore))
    .build();
```

Server-side (Outbound) uses Netty's server SSL:

```java
SslContext serverSsl = SslContextBuilder.forServer(
    new File("cert.pem"),
    new File("key.pem")
).build();

OutboundServer.create(
    OutboundServerConfig.builder()
        .port(8084)
        .sslContext(serverSsl)
        .build(),
    session -> handleCall(session)
).startup();
```

### Connection Pooling

For high-concurrency scenarios, use multiple clients:

```java
// Create 5 client connections for load distribution
List<InboundClient> clients = IntStream.range(0, 5)
    .mapToObj(i -> InboundClient.create(
        InboundClientConfig.builder()
            .host("localhost")
            .port(8021)
            .password("ClueCon")
            .autoReconnect(true)
            .build()
    ))
    .collect(Collectors.toList());

// Connect all
clients.forEach(c -> c.connect().join());

// Round-robin for commands
int index = 0;
for (String uuid : uuidsToControl) {
    InboundClient client = clients.get(index++ % clients.size());
    client.api(new HangupCommand(uuid)).join();
}
```

### Backpressure & Flow Control

When event volume is high, use a bounded event queue:

```java
EslEventListener handler = new AbstractEslEventListener() {
    @Override
    public void onChannelAnswer(ChannelAnswerEvent event) {
        // Process event (may be slow)
        performHeavyAnalysis(event);
    }
};

EslEventListenerProxy proxy = new EslEventListenerProxy(
    handler,
    5000,                          // Max 5000 pending events
    Executors.newFixedThreadPool(4)  // Use 4 threads
);

client.addEventListener(proxy);
```

If queue is full, excess events are dropped (logged).

### Virtual Threads (Java 21+)

Use virtual threads for I/O-bound work:

```java
ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

EslEventListener handler = event -> {
    virtualExecutor.submit(() -> {
        // This runs on a virtual thread (cheap, scalable)
        doSlowDatabaseQuery();
    });
};

client.addEventListener(handler);
```

### Idle Connection Detection

Detect dead connections with heartbeat:

```java
client.subscribe(EventName.HEARTBEAT).join();

client.addEventListener(new AbstractEslEventListener() {
    @Override
    public void onHeartbeat(HeartbeatEvent event) {
        // Connection is alive, do health checks
    }
});
```

Or configure TCP keep-alive:

```java
InboundClientConfig config = InboundClientConfig.builder()
    .host("localhost")
    .port(8021)
    .password("ClueCon")
    // Netty will send TCP keep-alive probes
    .tcpNoDelay(true)
    .build();
```

---

## Sub-APIs

Specialized APIs for common FreeSWITCH modules.

### UUID API (Per-Channel Operations)

```java
Uuid uuidApi = client.uuid("abc-123");

uuidApi.hold().join();           // Put on hold
uuidApi.unhold().join();         // Resume
uuidApi.getVariable("foo").join();  // Get channel variable
uuidApi.setVariable("bar", "baz").join();  // Set channel variable
uuidApi.setVariableMultiple(Map.of(
    "x", "1",
    "y", "2"
)).join();

uuidApi.bridge("sofia/default/1001@domain.com").join();
uuidApi.transfer("2000", "default").join();
uuidApi.park().join();
uuidApi.unpark().join();
```

### Conference API

```java
Conference conf = client.conference("sales");

// Participant management
conf.kick(1).join();            // Remove participant ID 1
conf.mute(1).join();            // Mute participant 1
conf.unmute(1).join();          // Unmute participant 1
conf.deaf(1).join();            // Participant can't hear

// Playback
conf.playback("/var/sounds/announce.wav").join();

// Info
conf.info().thenAccept(info ->
    System.out.println("Members: " + info.getMembers())
).join();

// Destroy
conf.destroy().join();
```

### Sofia API (SIP Profile Control)

```java
Sofia sofia = client.sofia();

// Profile control
sofia.profile("internal").restart().join();
sofia.profile("external").rescan().join();
sofia.profile("internal").flush_inbound_reg().join();

// Registration monitoring
sofia.profile("internal").register_throttle().join();

// Gateway management
sofia.profile("carrier").unregister("mygw", true).join();
```

### Call Center API

```java
Callcenter cc = client.callcenter();

// Queue status
cc.info("support_queue").join();

// Agent management
cc.agent_add("1001", "external", "phone/1001").join();
cc.agent_del("1001").join();
cc.agent_status("1001", "idle").join();

// Tier control
cc.tier_add("support_queue", "1001", 1, 100).join();
```

### Database API

```java
Db db = client.db();

// Key-value store
db.insert("mydb", "user:1001", "active").join();
db.select("mydb", "user:1001").thenAccept(value ->
    System.out.println("Value: " + value)
).join();
db.delete("mydb", "user:1001").join();
```

### Hash Table API

```java
Hash hash = client.hash();

hash.insert("sessions", "user:1001", "session_data").join();
hash.select("sessions", "user:1001").join();
hash.delete("sessions", "user:1001").join();
```

### Voicemail API

```java
Voicemail vm = client.voicemail();

vm.check("1001", "default").join();  // Check voicemail
```

### Valet Parking

```java
ValetParking parking = client.valetParking();

parking.park("1001", "default").join();      // Park call
parking.unpark("1001", "default").join();    // Retrieve parked call
```

### Other Sub-APIs

```java
// Nibblebill (billing)
Nibblebill billing = client.nibblebill();
billing.creditadd("account1", "100.00").join();

// Distributor (load balancing)
Distributor dist = client.distributor();
dist.reload().join();

// Blacklist (call filtering)
Blacklist blacklist = client.blacklist();
blacklist.add("5551234567").join();
blacklist.remove("5551234567").join();
```

---

## Configuration

### InboundClientConfig

All configuration for the inbound client:

```java
InboundClientConfig.builder()
    // Connection
    .host("localhost")              // FreeSWITCH host
    .port(8021)                     // Default event socket port
    .password("ClueCon")            // Default password
    
    // Reconnection
    .autoReconnect(true)            // Auto-reconnect on disconnect
    .reconnectInitialDelayMs(1000)  // Initial backoff: 1 second
    .reconnectMaxDelayMs(30000)     // Max backoff: 30 seconds
    .maxReconnectAttempts(10)       // Give up after 10 attempts
    
    // Network
    .tcpNoDelay(true)               // Disable Nagle's algorithm
    .keepAliveIdleSeconds(60)       // TCP keep-alive interval
    
    // SSL/TLS
    .sslContext(sslContext)         // Optional SSL context
    
    // Event processing
    .eventExecutor(executor)        // Custom executor for events
    .maxPendingEvents(10000)        // Event queue size
    
    .build();
```

### OutboundServerConfig

Configuration for the outbound server (accepts FreeSWITCH connections):

```java
OutboundServerConfig.builder()
    .port(8084)                     // Listen port
    .bindAddress("0.0.0.0")         // Bind interface
    .maxConcurrentSessions(1000)    // Backpressure limit
    .tcpNoDelay(true)
    .sslContext(sslContext)         // Optional SSL
    .build();
```

### FreeSWITCH Configuration

Enable and configure mod_event_socket in FreeSWITCH:

```xml
<!-- conf/autoload_configs/event_socket.conf.xml -->
<configuration name="event_socket.conf" description="Event Socket">
  <settings>
    <param name="listen-ip" value="0.0.0.0"/>
    <param name="listen-port" value="8021"/>
    <param name="password" value="ClueCon"/>
    <param name="apply-inbound-acl" value="loopback.auto"/>
  </settings>
</configuration>
```

---

## Troubleshooting

### Connection Issues

**Connection refused**
```
java.net.ConnectException: Connection refused
```
- Check FreeSWITCH is running: `fs_cli -status`
- Check port: `netstat -an | grep 8021`
- Check IP/host: Is it reachable from your app?

**Authentication failed**
```
EslLoginException: Invalid password
```
- Verify password in config matches `event_socket.conf.xml`
- Check `apply-inbound-acl` allows your IP

**Connection timeout**
```
TimeoutException: Connection timeout
```
- Increase connection timeout in config
- Check network connectivity: `ping freeswitch_host`

### Event Issues

**Not receiving events**
- Did you call `subscribe()`? Events are dropped until subscribed.
- Check `application.conf` has `mod_event_socket` enabled
- Look at `/var/log/freeswitch/freeswitch.log` for errors

**High event latency**
- Use `EslEventListenerProxy` with bounded queue to avoid blocking I/O thread
- Reduce event volume with `filter()`
- Add more worker threads

**Memory usage growing**
- Check for event listener leaks (listeners not removed)
- Monitor pending event queue size
- Use backpressure limits

### Command Issues

**API command returns error**
```java
CommandReply reply = client.api(new HangupCommand("bad-uuid")).join();
if (!reply.isOk()) {
    System.err.println(reply.getErrorMessage());
}
```

**bgapi job doesn't complete**
- Check `/var/log/freeswitch/freeswitch.log`
- Listen for `BackgroundJobEvent` to see job result
- Job ID should match if tracking

**Originate fails**
```
-ERR Invalid endpoint
```
- Check endpoint string: `sofia/default/1001@domain.com`
- Verify SIP profile/gateway exists in FreeSWITCH
- Check dialplan/user configuration

### Performance Issues

**High latency**
- Use `tcpNoDelay(true)` to disable Nagle's algorithm
- Reduce event filtering overhead
- Profile with async traces

**Connection pool saturation**
- Increase `maxConcurrentSessions` in `OutboundServerConfig`
- Use multiple `InboundClient` instances
- Monitor active sessions

**Event queue overflow**
```
Event dropped: queue full
```
- Increase queue size in `EslEventListenerProxy`
- Use virtual threads for I/O-bound work
- Reduce event subscription volume

### Debugging

Enable debug logging:

```xml
<!-- logback.xml -->
<logger name="com.freeswitchjava.esl" level="DEBUG"/>
<logger name="io.netty" level="DEBUG"/>
```

Monitor connection state:

```java
client.onStateChange(state -> {
    System.out.println("State: " + state);
});
```

Inspect events:

```java
client.addEventListener(event -> {
    System.out.println("Event: " + event.getClass().getSimpleName());
});
```

---

## Examples

### Simple Call Monitoring

```java
public class CallMonitor {
    public static void main(String[] args) throws Exception {
        InboundClient client = InboundClient.create(
            InboundClientConfig.builder()
                .host("localhost")
                .port(8021)
                .password("ClueCon")
                .autoReconnect(true)
                .build());

        client.addEventListener(new AbstractEslEventListener() {
            @Override
            public void onChannelCreate(ChannelCreateEvent event) {
                System.out.println("[CREATE] " + event.getUniqueId());
            }

            @Override
            public void onChannelAnswer(ChannelAnswerEvent event) {
                System.out.println("[ANSWER] " + event.getCallerIdNumber() 
                    + " -> " + event.getCalledNumber());
            }

            @Override
            public void onChannelHangup(ChannelHangupEvent event) {
                System.out.println("[HANGUP] " + event.getUniqueId() 
                    + " (" + event.getHangupCauseEnum() + ")");
            }
        });

        client.subscribe(
            EventName.CHANNEL_CREATE,
            EventName.CHANNEL_ANSWER,
            EventName.CHANNEL_HANGUP
        ).join();

        client.startup();  // Blocks until Ctrl+C
    }
}
```

### IVR with Outbound Server

```java
public class IvrServer {
    public static void main(String[] args) {
        OutboundServer.create(
            OutboundServerConfig.builder().port(8084).build(),
            session -> handleCall(session)
        ).startup();
    }

    private static void handleCall(OutboundSession session) {
        try {
            session.connect().join();
            session.answer().join();

            // Welcome message
            session.playback("/var/sounds/welcome.wav").join();

            // Get menu selection (5 digits, 60s timeout)
            String input = session.playRecording(
                "/var/sounds/menu.wav",
                5,
                60
            ).join();

            if (input.isEmpty()) {
                session.playback("/var/sounds/timeout.wav").join();
            } else {
                switch (input.charAt(0)) {
                    case '1':
                        session.bridge("sofia/default/sales@internal").join();
                        break;
                    case '2':
                        session.bridge("sofia/default/support@internal").join();
                        break;
                    default:
                        session.playback("/var/sounds/invalid.wav").join();
                }
            }

            session.hangup().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Originate with Callback

```java
public class CallOriginator {
    public static void main(String[] args) throws Exception {
        InboundClient client = InboundClient.create(
            InboundClientConfig.builder()
                .host("localhost")
                .port(8021)
                .password("ClueCon")
                .build());

        client.login();

        // Listen for originate results
        client.addEventListener(new AbstractEslEventListener() {
            @Override
            public void onChannelOriginatEvent(ChannelOriginateEvent event) {
                if (event.isSuccess()) {
                    System.out.println("Call originated: " + event.getUniqueId());
                } else {
                    System.err.println("Originate failed");
                }
            }
        });

        client.subscribe(EventName.CHANNEL_ORIGINATE).join();

        // Originate a call
        client.api(new OriginateCommand("sofia/default/1001@domain.com")
            .extension("2000")
            .context("default")
        ).join();

        Thread.sleep(30000);
        client.shutdown();
    }
}
```

---

## API Reference Summary

See inline JavaDoc in the source code:
- `InboundClient` — Primary interface
- `InboundClientConfig` — Configuration builder
- `OutboundServer` — Inbound server for outbound connections
- `AbstractEslEventListener` — Event listener base class
- All command classes in `com.freeswitchjava.esl.api.*`

---

## Contributing

Issues & pull requests welcome on [GitHub](https://github.com/freeswitch-java/freeswitch-java).

---

## License

MIT License. See [LICENSE](LICENSE) in repository.
