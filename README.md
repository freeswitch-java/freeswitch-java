# freeswitch-java

Java client library for the [FreeSWITCH](https://freeswitch.org) Event Socket Layer (ESL).

Built on [Netty](https://netty.io) · Java 21 · `CompletableFuture` API · Spring Boot starter included

---

## Installation

**JitPack:**
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

**Local build:**
```bash
git clone https://github.com/freeswitch-java/freeswitch-java.git
cd freeswitch-java && mvn install -DskipTests
```
```xml
<dependency>
    <groupId>com.freeswitchjava</groupId>
    <artifactId>freeswitchjava</artifactId>
    <version>0.2.0</version>
</dependency>
```

---

## Connect

```java
InboundClient client = InboundClient.create(
    InboundClientConfig.builder()
        .host("localhost")
        .port(8021)
        .password("ClueCon")
        .autoReconnect(true)
        .build());

client.login();
client.subscribe(EventName.ALL).join();
```

---

## Send commands

```java
// Hangup a channel
client.api(new HangupCommand("uuid")).join();

// Originate a call
client.api(new OriginateCommand("sofia/default/1001@domain.com")
    .extension("2000").context("default").timeout(30)).join();

// Any fs_cli command
client.api(new RawApiCommand("sofia status")).join();
```

---

## Handle events

```java
client.addEventListener(new AbstractEslEventListener() {

    @Override
    public void onChannelAnswer(ChannelAnswerEvent event) {
        System.out.println("Answered: " + event.getCallerIdNumber());
    }

    @Override
    public void onChannelHangup(ChannelHangupEvent event) {
        System.out.println("Hangup: " + event.getHangupCauseEnum());
    }

    @Override
    public void onDtmf(DtmfEvent event) {
        System.out.println("DTMF: " + event.getDtmfDigit());
    }
});
```

---

## Spring Boot

```yaml
freeswitch:
  esl:
    host: localhost
    password: ClueCon
```

```java
@EslEventHandler
public class CallHandler {

    @OnChannelAnswer
    public void onAnswer(ChannelAnswerEvent event) { ... }

    @OnChannelHangup
    public void onHangup(ChannelHangupEvent event) { ... }

    @OnDtmf
    public void onDtmf(DtmfEvent event) { ... }
}
```

---

## Outbound server

```java
OutboundServer.create(
    OutboundServerConfig.builder().port(8084).build(),
    session -> {
        session.connect().join();
        session.answer().join();
        session.playback("/var/sounds/welcome.wav").join();
        session.hangup().join();
    }
).startup();
```

FreeSWITCH dialplan:
```xml
<action application="socket" data="192.168.1.10:8084 async full"/>
```

---

## Documentation

Full documentation → **[GitHub Pages](https://freeswitch-java.github.io/freeswitch-java/)**

---

## License

MIT License — see [LICENSE](LICENSE).
