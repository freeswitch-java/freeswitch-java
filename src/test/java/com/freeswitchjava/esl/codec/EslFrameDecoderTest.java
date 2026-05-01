package com.freeswitchjava.esl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EslFrameDecoder} covering all TCP delivery scenarios.
 */
class EslFrameDecoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new EslFrameDecoder());
    }

    // --- Helper ---

    private ByteBuf buf(String s) {
        return Unpooled.wrappedBuffer(s.getBytes(StandardCharsets.UTF_8));
    }

    private List<EslMessage> readAll() {
        List<EslMessage> result = new ArrayList<>();
        Object msg;
        while ((msg = channel.readInbound()) != null) {
            result.add((EslMessage) msg);
        }
        return result;
    }

    // --- Tests ---

    @Test
    void decodes_auth_request_no_body() {
        channel.writeInbound(buf("Content-Type: auth/request\n\n"));

        List<EslMessage> msgs = readAll();
        assertThat(msgs).hasSize(1);
        assertThat(msgs.get(0).getContentType()).isEqualTo(EslHeaders.CT_AUTH_REQUEST);
        assertThat(msgs.get(0).hasBody()).isFalse();
    }

    @Test
    void decodes_command_reply_no_body() {
        channel.writeInbound(buf(
                "Content-Type: command/reply\n" +
                "Reply-Text: +OK accepted\n\n"
        ));

        List<EslMessage> msgs = readAll();
        assertThat(msgs).hasSize(1);
        assertThat(msgs.get(0).getContentType()).isEqualTo(EslHeaders.CT_COMMAND_REPLY);
        assertThat(msgs.get(0).getHeader("reply-text")).isEqualTo("+OK accepted");
    }

    @Test
    void decodes_message_with_body() {
        String body = "Event-Name: CHANNEL_ANSWER\nUnique-ID: abc123\n\n";
        String frame = "Content-Type: text/event-plain\n" +
                       "Content-Length: " + body.length() + "\n\n" +
                       body;

        channel.writeInbound(buf(frame));

        List<EslMessage> msgs = readAll();
        assertThat(msgs).hasSize(1);
        assertThat(msgs.get(0).getContentType()).isEqualTo(EslHeaders.CT_EVENT_PLAIN);
        assertThat(msgs.get(0).getBody()).isEqualTo(body);
    }

    @Test
    void handles_tcp_fragmentation() {
        // Same message split into 3 separate reads
        String frame = "Content-Type: auth/request\n\n";
        channel.writeInbound(buf(frame.substring(0, 10)));
        assertThat(readAll()).isEmpty(); // incomplete

        channel.writeInbound(buf(frame.substring(10, 20)));
        assertThat(readAll()).isEmpty(); // still incomplete

        channel.writeInbound(buf(frame.substring(20)));
        List<EslMessage> msgs = readAll();
        assertThat(msgs).hasSize(1);
        assertThat(msgs.get(0).getContentType()).isEqualTo(EslHeaders.CT_AUTH_REQUEST);
    }

    @Test
    void handles_tcp_coalescing_two_messages_in_one_read() {
        String frame1 = "Content-Type: auth/request\n\n";
        String frame2 = "Content-Type: command/reply\nReply-Text: +OK\n\n";

        channel.writeInbound(buf(frame1 + frame2));

        List<EslMessage> msgs = readAll();
        assertThat(msgs).hasSize(2);
        assertThat(msgs.get(0).getContentType()).isEqualTo(EslHeaders.CT_AUTH_REQUEST);
        assertThat(msgs.get(1).getContentType()).isEqualTo(EslHeaders.CT_COMMAND_REPLY);
    }

    @Test
    void handles_body_split_across_reads() {
        String body = "result=hello world\n\n";
        String headers = "Content-Type: api/response\nContent-Length: " + body.length() + "\n\n";

        // Headers arrive first, then body in two chunks
        channel.writeInbound(buf(headers));
        assertThat(readAll()).isEmpty(); // waiting for body

        channel.writeInbound(buf(body.substring(0, 5)));
        assertThat(readAll()).isEmpty(); // partial body

        channel.writeInbound(buf(body.substring(5)));
        List<EslMessage> msgs = readAll();
        assertThat(msgs).hasSize(1);
        assertThat(msgs.get(0).getBody()).isEqualTo(body);
    }

    @Test
    void handles_crlf_line_endings() {
        channel.writeInbound(buf("Content-Type: auth/request\r\n\r\n"));

        List<EslMessage> msgs = readAll();
        assertThat(msgs).hasSize(1);
        assertThat(msgs.get(0).getContentType()).isEqualTo(EslHeaders.CT_AUTH_REQUEST);
    }

    @Test
    void headers_are_normalized_to_lowercase() {
        channel.writeInbound(buf("Content-Type: command/reply\nReply-Text: +OK\n\n"));

        EslMessage msg = (EslMessage) channel.readInbound();
        assertThat(msg.getHeader("content-type")).isEqualTo("command/reply");
        assertThat(msg.getHeader("Content-Type")).isEqualTo("command/reply"); // case-insensitive
        assertThat(msg.getHeader("reply-text")).isEqualTo("+OK");
    }

    @Test
    void decodes_multiple_messages_with_bodies() {
        String body1 = "var=value\n\n";
        String body2 = "other=stuff\n\n";
        String frame1 = "Content-Type: text/event-plain\nContent-Length: " + body1.length() + "\n\n" + body1;
        String frame2 = "Content-Type: text/event-plain\nContent-Length: " + body2.length() + "\n\n" + body2;

        channel.writeInbound(buf(frame1 + frame2));

        List<EslMessage> msgs = readAll();
        assertThat(msgs).hasSize(2);
        assertThat(msgs.get(0).getBody()).isEqualTo(body1);
        assertThat(msgs.get(1).getBody()).isEqualTo(body2);
    }

    @Test
    void message_with_no_headers_except_content_type() {
        channel.writeInbound(buf("Content-Type: text/disconnect-notice\n\n"));

        EslMessage msg = (EslMessage) channel.readInbound();
        assertThat(msg).isNotNull();
        assertThat(msg.getContentType()).isEqualTo(EslHeaders.CT_DISCONNECT_NOTICE);
    }
}
