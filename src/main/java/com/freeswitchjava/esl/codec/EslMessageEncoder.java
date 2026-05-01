package com.freeswitchjava.esl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * Netty encoder that writes a command string as UTF-8 bytes followed by {@code \n\n}.
 *
 * <p>The ESL protocol requires all client-to-server messages to end with a blank line.
 * Simple commands (e.g. {@code "auth ClueCon"}) are passed without trailing newlines and
 * this encoder appends {@code \n\n}.
 *
 * <p>Multi-line frames (e.g. {@code sendmsg} produced by {@link com.freeswitchjava.esl.command.SendMsg})
 * must NOT already end with {@code \n} — the encoder always appends exactly {@code \n\n}.
 * The {@code SendMsg} builder strips its own trailing newline for this reason.
 */
@io.netty.channel.ChannelHandler.Sharable
public class EslMessageEncoder extends MessageToByteEncoder<String> {

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) {
        out.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
        out.writeByte('\n');
        out.writeByte('\n');
    }
}
