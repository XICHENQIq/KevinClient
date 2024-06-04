package com.diaoling.network.packet.impl.info;

import com.diaoling.network.buffer.PacketBuffer;
import com.diaoling.utils.misc.enums.ClientType;
import com.diaoling.utils.misc.enums.Rank;
import com.diaoling.network.handler.ClientHandler;
import com.diaoling.network.info.record.OnlineUserInfo;
import com.diaoling.network.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import kevin.KevinClient;
import kevin.utils.ChatUtils;

import java.util.List;

/**
 * @author DiaoLing
 * @since 4/8/2024
 */
public class OnlineUsersPacket extends Packet {
    private List<OnlineUserInfo> onlineUsers;

    public OnlineUsersPacket() {
    }

    public OnlineUsersPacket(List<OnlineUserInfo> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeList(onlineUsers, (buffer, user) -> {
            buffer.writeEnum(user.client);
            buffer.writeString(user.username);
            buffer.writeString(user.inGameName);
            buffer.writeEnum(user.rank);
        });
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.onlineUsers = buf.readList(buffer -> new OnlineUserInfo(
                buffer.readEnum(ClientType.class),
                buffer.readString(),
                buffer.readString(),
                buffer.readEnum(Rank.class)
        ));
    }

    @Override
    public void handler(ChannelHandlerContext ctx, ClientHandler handler) {
        KevinClient.userManager.setOnlineUsers(getOnlineUsers());
        /*for (OnlineUserInfo user : onlineUsers) {
            String displayInfo = user.inGameName() + "(" + user.username() + ")";
            ChatUtils.INSTANCE.message(displayInfo);
        }*/
    }

    public List<OnlineUserInfo> getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(List<OnlineUserInfo> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }
}