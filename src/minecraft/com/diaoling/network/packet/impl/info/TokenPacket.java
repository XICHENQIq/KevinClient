package com.diaoling.network.packet.impl.info;

import com.diaoling.network.buffer.PacketBuffer;
import com.diaoling.utils.misc.enums.ClientType;
import com.diaoling.network.handler.ClientHandler;
import com.diaoling.network.packet.Packet;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author DiaoLing
 * @since 4/8/2024
 */
public class TokenPacket extends Packet {
    private ClientType client;
    private String token;

    public TokenPacket() {
    }

    public TokenPacket(ClientType client, String token) {
        this.client = client;
        this.token = token;
    }

    public ClientType getClient() {
        return client;
    }

    public String getToken() {
        return token;
    }

    public void setClient(ClientType client) {
        this.client = client;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeEnum(client);
        buf.writeString(token);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.client = buf.readEnum(ClientType.class);
        this.token = buf.readString();
    }

    @Override
    public void handler(ChannelHandlerContext ctx, ClientHandler handler) {

    }
}