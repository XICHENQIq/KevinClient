package com.diaoling.network.packet.impl.message;

import com.diaoling.network.buffer.PacketBuffer;
import com.diaoling.utils.misc.enums.ChannelType;
import com.diaoling.network.handler.ClientHandler;
import com.diaoling.network.packet.Packet;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author DiaoLing
 * @since 4/8/2024
 */
public class ChatMessagePacket extends Packet {
    private ChannelType channel;
    private String message;
    private long timestamp;

    public ChatMessagePacket() {
    }

    public ChatMessagePacket(ChannelType channel, String message, long timestamp) {
        this.channel = channel;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setChannel(ChannelType channel) {
        this.channel = channel;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeEnum(channel);
        buf.writeString(message);
        buf.writeLong(timestamp);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.channel = buf.readEnum(ChannelType.class);
        this.message = buf.readString();
        this.timestamp = buf.readLong();
    }

    @Override
    public void handler(ChannelHandlerContext ctx, ClientHandler handler) {

    }
}