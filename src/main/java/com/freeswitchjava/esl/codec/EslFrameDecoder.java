package com.freeswitchjava.esl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Netty decoder that translates raw TCP bytes into {@link EslMessage} objects.
 *
 * <p>ESL wire format:
 * <pre>
 *   Header-Name: header-value\n
 *   Header-Name: header-value\n
 *   \n
 *   [optional body of Content-Length bytes]
 * </pre>
 *
 * <p>The decoder uses a two-state machine:
 * <ul>
 *   <li>{@code READ_HEADERS} – scans for the blank line ({@code \n\n}) that terminates headers</li>
 *   <li>{@code READ_BODY}    – reads exactly {@code Content-Length} bytes as the body</li>
 * </ul>
 *
 * <p>Both TCP fragmentation (message split across multiple reads) and
 * coalescing (multiple messages in one read) are handled correctly.
 */
public class EslFrameDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(EslFrameDecoder.class);

    private enum State { READ_HEADERS, READ_BODY }

    private State state = State.READ_HEADERS;
    private final Map<String, String> currentHeaders = new LinkedHashMap<>();
    private int bodyLength = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        while (in.isReadable()) {
            if (state == State.READ_HEADERS) {
                if (!decodeHeaders(in)) {
                    return; // need more data
                }
                // After parsing headers, either emit (no body) or transition to READ_BODY
                String contentLengthStr = currentHeaders.get(EslHeaders.CONTENT_LENGTH);
                if (contentLengthStr != null) {
                    try {
                        bodyLength = Integer.parseInt(contentLengthStr.trim());
                    } catch (NumberFormatException e) {
                        log.warn("Invalid Content-Length '{}', treating as 0", contentLengthStr);
                        bodyLength = 0;
                    }
                }
                if (bodyLength > 0) {
                    state = State.READ_BODY;
                } else {
                    out.add(buildMessage(null));
                }
            }

            if (state == State.READ_BODY) {
                if (in.readableBytes() < bodyLength) {
                    return; // need more data
                }
                String body = in.readCharSequence(bodyLength, StandardCharsets.UTF_8).toString();
                out.add(buildMessage(body));
                state = State.READ_HEADERS;
            }
        }
    }

    /**
     * Reads header lines from the buffer until the blank line is found.
     *
     * @return true if the blank line was found and headers are complete, false if more data is needed
     */
    private boolean decodeHeaders(ByteBuf in) {
        while (true) {
            int lineEnd = indexOf(in, (byte) '\n');
            if (lineEnd < 0) {
                return false; // no newline yet, wait for more data
            }

            int lineLength = lineEnd - in.readerIndex();

            if (lineLength == 0 || (lineLength == 1 && in.getByte(in.readerIndex()) == '\r')) {
                // Blank line — end of headers (handles both \n\n and \r\n\r\n)
                in.skipBytes(lineLength + 1); // skip the \n (and optional \r)
                return true;
            }

            // Read the header line
            byte[] lineBytes = new byte[lineLength];
            in.readBytes(lineBytes);
            in.skipBytes(1); // skip \n

            String line = new String(lineBytes, StandardCharsets.UTF_8).trim();
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                String name = line.substring(0, colonIdx).trim().toLowerCase();
                String value = line.substring(colonIdx + 1).trim();
                currentHeaders.put(name, value);
            } else if (!line.isEmpty()) {
                log.debug("Ignoring malformed header line: {}", line);
            }
        }
    }

    private EslMessage buildMessage(String body) {
        Map<String, String> headers = new LinkedHashMap<>(currentHeaders);
        currentHeaders.clear();
        bodyLength = 0;
        return new EslMessage(headers, body);
    }

    /**
     * Finds the index of {@code needle} in {@code buf} starting from the reader index,
     * without advancing the reader index.
     */
    private static int indexOf(ByteBuf buf, byte needle) {
        int start = buf.readerIndex();
        int end = buf.writerIndex();
        for (int i = start; i < end; i++) {
            if (buf.getByte(i) == needle) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("ESL decoder error on channel {}", ctx.channel(), cause);
        ctx.close();
    }
}
