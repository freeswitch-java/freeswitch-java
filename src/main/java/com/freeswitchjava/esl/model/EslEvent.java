package com.freeswitchjava.esl.model;

import com.freeswitchjava.esl.codec.EslHeaders;
import com.freeswitchjava.esl.codec.EslMessage;
import com.freeswitchjava.esl.event.EventName;
import com.freeswitchjava.esl.util.EslMessageParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.xml.sax.InputSource;

/**
 * A parsed FreeSWITCH event — the base class for all typed event subclasses.
 *
 * <p>Use the typed subclasses (e.g. {@link com.freeswitchjava.esl.event.ChannelHangupEvent})
 * for full type safety and discoverability. This base class exposes headers common to every event.
 *
 * <h2>Factory</h2>
 * {@link #fromPlainMessage(EslMessage)} automatically returns the most specific subclass:
 * <pre>{@code
 * client.addEventListener(EventName.CHANNEL_HANGUP, evt -> {
 *     ChannelHangupEvent hangup = (ChannelHangupEvent) evt;
 *     System.out.println(hangup.getHangupCause());
 * });
 * }</pre>
 */
public class EslEvent {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final Map<String, String> eventHeaders;
    private final String eventBody;

    public EslEvent(Map<String, String> eventHeaders, String eventBody) {
        this.eventHeaders = Collections.unmodifiableMap(eventHeaders);
        this.eventBody    = eventBody == null ? "" : eventBody;
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Parses a {@code text/event-plain} ESL message and returns the most specific subclass.
     */
    public static EslEvent fromPlainMessage(EslMessage message) {
        String[] parts   = EslMessageParser.splitEventBody(message.getBody());
        Map<String, String> headers = EslMessageParser.parseHeaders(parts[0]);
        String body      = parts[1];
        String eventName = headers.get(EslHeaders.EVENT_NAME);
        return EslEventFactory.create(eventName, headers, body);
    }

    /**
     * Parses a {@code text/event-json} ESL message and returns the most specific subclass.
     *
     * <p>In JSON format, FreeSWITCH sends the entire event as a flat JSON object.
     * The optional body (e.g. for BACKGROUND_JOB) is in the {@code "_body"} key.
     */
    public static EslEvent fromJsonMessage(EslMessage message) {
        try {
            Map<String, Object> raw = JSON.readValue(
                    message.getBody(), new TypeReference<Map<String, Object>>() {});
            Map<String, String> headers = new LinkedHashMap<>();
            String body = "";
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT);
                Object val = entry.getValue();
                if ("_body".equals(key)) {
                    body = val != null ? val.toString() : "";
                } else {
                    headers.put(key, val != null ? val.toString() : "");
                }
            }
            String eventName = headers.get(EslHeaders.EVENT_NAME);
            return EslEventFactory.create(eventName, headers, body);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON event: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a {@code text/event-xml} ESL message and returns the most specific subclass.
     */
    public static EslEvent fromXmlMessage(EslMessage message) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);

            Document doc = f.newDocumentBuilder().parse(new InputSource(new StringReader(message.getBody())));
            Element root = doc.getDocumentElement();
            if (root == null) {
                return new EslEvent(Collections.emptyMap(), "");
            }

            Map<String, String> headers = new LinkedHashMap<>();
            String body = "";

            Element headersNode = firstChildElementByTag(root, "headers");
            if (headersNode != null) {
                NodeList children = headersNode.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node n = children.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        String name = n.getNodeName().toLowerCase(Locale.ROOT);
                        headers.put(name, n.getTextContent());
                    }
                }
            } else {
                NodeList children = root.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node n = children.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE && !"body".equalsIgnoreCase(n.getNodeName())) {
                        headers.put(n.getNodeName().toLowerCase(Locale.ROOT), n.getTextContent());
                    }
                }
            }

            Element bodyNode = firstChildElementByTag(root, "_body");
            if (bodyNode == null) bodyNode = firstChildElementByTag(root, "body");
            if (bodyNode != null) {
                body = bodyNode.getTextContent();
            }

            String eventName = headers.get(EslHeaders.EVENT_NAME);
            return EslEventFactory.create(eventName, headers, body);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse XML event: " + e.getMessage(), e);
        }
    }

    private static Element firstChildElementByTag(Element root, String tag) {
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && tag.equalsIgnoreCase(n.getNodeName())) {
                return (Element) n;
            }
        }
        return null;
    }

    // ── Universal getters ─────────────────────────────────────────────────────

    /** Raw event name string, e.g. {@code "CHANNEL_ANSWER"}. */
    public String getEventName() {
        return eventHeaders.get(EslHeaders.EVENT_NAME);
    }

    /**
     * Event name as a type-safe enum. Returns {@code null} for unknown/custom events.
     */
    public EventName getEventNameEnum() {
        return EventName.fromWire(getEventName());
    }

    /** The channel UUID ({@code Unique-ID} header). */
    public String getUniqueId() {
        return eventHeaders.get(EslHeaders.UNIQUE_ID);
    }

    /** FreeSWITCH core UUID. */
    public String getCoreUuid() {
        return eventHeaders.get(EslHeaders.CORE_UUID);
    }

    /** Unix epoch timestamp in microseconds ({@code Event-Date-Timestamp}). */
    public String getTimestamp() {
        return eventHeaders.get(EslHeaders.EVENT_DATE_TIMESTAMP);
    }

    /** Sequential event ID on this FreeSWITCH instance (resets on reboot). */
    public String getEventSequence() {
        return eventHeaders.get(EslHeaders.EVENT_SEQUENCE);
    }

    /** Source file in FreeSWITCH that fired this event. */
    public String getCallingFile() {
        return eventHeaders.get(EslHeaders.EVENT_CALLING_FILE);
    }

    /** Source function in FreeSWITCH that fired this event. */
    public String getCallingFunction() {
        return eventHeaders.get(EslHeaders.EVENT_CALLING_FUNCTION);
    }

    /** Source line number in FreeSWITCH that fired this event. */
    public String getCallingLineNumber() {
        return eventHeaders.get(EslHeaders.EVENT_CALLING_LINE);
    }

    /** Local date/time string from the FreeSWITCH instance. */
    public String getEventDateLocal() {
        return eventHeaders.get(EslHeaders.EVENT_DATE_LOCAL);
    }

    /** GMT date/time string from the FreeSWITCH instance. */
    public String getEventDateGmt() {
        return eventHeaders.get(EslHeaders.EVENT_DATE_GMT);
    }

    // ── FreeSWITCH instance info ──────────────────────────────────────────────

    /** IPv4 address of the FreeSWITCH instance that fired this event. */
    public String getFreeswitchIpv4() {
        return eventHeaders.get(EslHeaders.FREESWITCH_IPV4);
    }

    /** IPv6 address of the FreeSWITCH instance. */
    public String getFreeswitchIpv6() {
        return eventHeaders.get(EslHeaders.FREESWITCH_IPV6);
    }

    /** Hostname of the FreeSWITCH machine. */
    public String getFreeswitchHostname() {
        return eventHeaders.get(EslHeaders.FREESWITCH_HOSTNAME);
    }

    /** Switch name (usually same as hostname unless multi-homed). */
    public String getFreeswitchSwitchname() {
        return eventHeaders.get(EslHeaders.FREESWITCH_SWITCHNAME);
    }

    // ── Channel identity ──────────────────────────────────────────────────────

    /** SIP/channel name, e.g. {@code sofia/internal/1001@domain}. */
    public String getChannelName() {
        return eventHeaders.get("channel-name");
    }

    /** Channel state string, e.g. {@code CS_EXECUTE}, {@code CS_HANGUP}. */
    public String getChannelState() {
        return eventHeaders.get("channel-state");
    }

    /** Answer state: {@code answered}, {@code ringing}, {@code early}, {@code hangup}. */
    public String getAnswerState() {
        return eventHeaders.get("answer-state");
    }

    /** Call direction: {@code inbound} or {@code outbound}. */
    public String getCallDirection() {
        return eventHeaders.get("call-direction");
    }

    // ── Caller / Callee ───────────────────────────────────────────────────────

    /** Caller ID name. */
    public String getCallerIdName() {
        return eventHeaders.get("caller-caller-id-name");
    }

    /** Caller ID number. */
    public String getCallerIdNumber() {
        return eventHeaders.get("caller-caller-id-number");
    }

    /** Destination (dialled) number. */
    public String getDestinationNumber() {
        return eventHeaders.get("caller-destination-number");
    }

    /** Network address of the caller. */
    public String getNetworkAddr() {
        return eventHeaders.get("caller-network-addr");
    }

    /** SIP profile context. */
    public String getContext() {
        return eventHeaders.get("caller-context");
    }

    // ── Hangup ────────────────────────────────────────────────────────────────

    /**
     * Hangup cause as a string (available on {@code CHANNEL_HANGUP} and {@code CHANNEL_HANGUP_COMPLETE}).
     * Use {@link com.freeswitchjava.esl.event.ChannelHangupEvent#getHangupCauseEnum()} for the typed enum.
     */
    public String getHangupCause() {
        return eventHeaders.get("hangup-cause");
    }

    // ── Custom event subclass ─────────────────────────────────────────────────

    /**
     * For {@code CUSTOM} events: the event subclass identifier, e.g.
     * {@code "conference::maintenance"} or {@code "sofia::register"}.
     */
    public String getEventSubclass() {
        return eventHeaders.get(EslHeaders.EVENT_SUBCLASS);
    }

    // ── Generic access ────────────────────────────────────────────────────────

    /** Returns the value of any event header (case-insensitive). */
    public String getHeader(String name) {
        return eventHeaders.get(name.toLowerCase());
    }

    public boolean hasHeader(String name) {
        return eventHeaders.containsKey(name.toLowerCase());
    }

    /** All event headers (lowercase-keyed, unmodifiable). */
    public Map<String, String> getHeaders() {
        return eventHeaders;
    }

    /** Application-specific body that follows the event headers, if any. */
    public String getEventBody() {
        return eventBody;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{eventName=" + getEventName()
                + ", uniqueId=" + getUniqueId() + "}";
    }
}
