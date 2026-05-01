package com.freeswitchjava.esl.model;

import com.freeswitchjava.esl.codec.EslMessage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EslEventFactoryTest {

    @Test
    void resolves_registered_custom_event_for_plain_message() {
        String subclass = "conference::maintenance";
        EslEventFactory.registerCustomEventClass(subclass, TestCustomEvent.class);
        try {
            String body = """
                    Event-Name: CUSTOM
                    Event-Subclass: conference::maintenance
                    Unique-ID: abc-123

                    payload
                    """;

            EslEvent event = EslEvent.fromPlainMessage(new EslMessage(Map.of(), body));
            assertThat(event).isInstanceOf(TestCustomEvent.class);
        } finally {
            EslEventFactory.deregisterCustomEventClass(subclass);
        }
    }

    @Test
    void resolves_registered_custom_event_for_json_message() {
        String subclass = "conference::maintenance";
        EslEventFactory.registerCustomEventClass(subclass, TestCustomEvent.class);
        try {
            String body = """
                    {
                      "Event-Name": "CUSTOM",
                      "Event-Subclass": "conference::maintenance",
                      "Unique-ID": "abc-123",
                      "_body": "payload"
                    }
                    """;

            EslEvent event = EslEvent.fromJsonMessage(new EslMessage(Map.of(), body));
            assertThat(event).isInstanceOf(TestCustomEvent.class);
        } finally {
            EslEventFactory.deregisterCustomEventClass(subclass);
        }
    }

    @Test
    void resolves_registered_custom_event_for_xml_message() {
        String subclass = "conference::maintenance";
        EslEventFactory.registerCustomEventClass(subclass, TestCustomEvent.class);
        try {
            String body = """
                    <event>
                      <headers>
                        <Event-Name>CUSTOM</Event-Name>
                        <Event-Subclass>conference::maintenance</Event-Subclass>
                        <Unique-ID>abc-123</Unique-ID>
                      </headers>
                      <body>payload</body>
                    </event>
                    """;

            EslEvent event = EslEvent.fromXmlMessage(new EslMessage(Map.of(), body));
            assertThat(event).isInstanceOf(TestCustomEvent.class);
        } finally {
            EslEventFactory.deregisterCustomEventClass(subclass);
        }
    }

    public static final class TestCustomEvent extends EslEvent {
        public TestCustomEvent(Map<String, String> eventHeaders, String eventBody) {
            super(eventHeaders, eventBody);
        }
    }
}
