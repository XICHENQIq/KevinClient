package com.diaoling.network.handler;

import com.diaoling.network.buffer.PacketBuffer;
import com.diaoling.network.packet.Packet;
import com.diaoling.network.packet.PacketFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author DiaoLing
 * @since 4/7/2024
 */
public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int packetId = in.readInt();

        Packet packet = PacketFactory.createPacket(packetId);
        PacketBuffer buffer = new PacketBuffer(in);

        if (packet != null) {
            packet.decode(buffer);

            out.add(packet);
        }
    }
}