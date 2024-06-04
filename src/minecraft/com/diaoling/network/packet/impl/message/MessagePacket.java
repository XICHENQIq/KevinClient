package com.diaoling.network.packet.impl.message;

import com.diaoling.network.buffer.PacketBuffer;
import com.diaoling.utils.misc.enums.ChannelType;
import com.diaoling.utils.misc.enums.ChatType;
import com.diaoling.utils.misc.enums.ClientType;
import com.diaoling.utils.misc.enums.Rank;
import com.diaoling.network.handler.ClientHandler;
import com.diaoling.network.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import kevin.utils.ChatUtils;
import net.minecraft.util.text.TextFormatting;
import com.yumegod.obfuscation.Native;

/**
 * @author DiaoLing
 * @since 4/8/2024
 */
@Native
public class MessagePacket extends Packet {
    private ClientType client;
    private Rank rank;
    private ChannelType channel;
    private ChatType chat;
    private String username;
    private String message;
    private long timestamp;

    public MessagePacket() {
    }

    public MessagePacket(ClientType client, Rank rank, ChannelType channel, ChatType chat, String username, String message, long timestamp) {
        this.client = client;
        this.rank = rank;
        this.channel = channel;
        this.chat = chat;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ClientType getClient() {
        return client;
    }

    public Rank getRank() {
        return rank;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public ChatType getChat() {
        return chat;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeEnum(client);
        buf.writeEnum(rank);
        buf.writeEnum(channel);
        buf.writeEnum(chat);
        buf.writeString(username);
        buf.writeString(message);
        buf.writeLong(timestamp);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.client = buf.readEnum(ClientType.class);
        this.rank = buf.readEnum(Rank.class);
        this.channel = buf.readEnum(ChannelType.class);
        this.chat = buf.readEnum(ChatType.class);
        this.username = buf.readString();
        this.message = buf.readString();
        this.timestamp = buf.readLong();
    }

    @Override
    public void handler(ChannelHandlerContext ctx, ClientHandler handler) {
        ChatUtils.INSTANCE.message("IRC >> " +  build("[%rank_colored%] " + TextFormatting.AQUA + "(%channel%)" + TextFormatting.RESET + " %username%" + TextFormatting.DARK_GRAY + " <%client%>" + TextFormatting.GRAY + " %message%"));
    }

    public String build(String template) {
        String formattedMessage = template;

        formattedMessage = formattedMessage.replace("%rank%", getRank().getName());
        formattedMessage = formattedMessage.replace("%rank_colored%", getRank().color() + getRank().getName() + TextFormatting.RESET);
        formattedMessage = formattedMessage.replace("%channel%", getChannel().getName());
        formattedMessage = formattedMessage.replace("%chat%", getChat().getName());
        formattedMessage = formattedMessage.replace("%username%", getUsername());
        formattedMessage = formattedMessage.replace("%client%", getClient().getName());
        formattedMessage = formattedMessage.replace("%message%", getMessage());

        return formattedMessage;
    }
}
