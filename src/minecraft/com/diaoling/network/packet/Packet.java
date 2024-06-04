package com.diaoling.network.packet;

import com.diaoling.network.buffer.PacketBuffer;
import com.diaoling.network.handler.ClientHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author DiaoLing
 * @since 4/7/2024
 */
public abstract class Packet {
    public abstract void encode(PacketBuffer buf);

    public abstract void decode(PacketBuffer buf);

    public abstract void handler(ChannelHandlerContext ctx, ClientHandler handler);
}