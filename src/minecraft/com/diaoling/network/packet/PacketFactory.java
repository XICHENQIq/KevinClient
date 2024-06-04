package com.diaoling.network.packet;

import com.diaoling.network.packet.impl.info.GameInfoPacket;
import com.diaoling.network.packet.impl.info.OnlineUsersPacket;
import com.diaoling.network.packet.impl.info.TokenPacket;
import com.diaoling.network.packet.impl.info.UserInfoPacket;
import com.diaoling.network.packet.impl.message.ChatMessagePacket;
import com.diaoling.network.packet.impl.message.MessagePacket;
import com.diaoling.network.packet.impl.operation.OperationPacket;
import com.diaoling.utils.misc.Logger;
import com.yumegod.obfuscation.Native;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DiaoLing
 * @since 4/7/2024
 */
@Native
public class PacketFactory {
    private static final Map<Integer, Class<? extends Packet>> packetMap = new HashMap<>();

    static {
        packetMap.put(1, TokenPacket.class);
        packetMap.put(2, UserInfoPacket.class);
        packetMap.put(3, GameInfoPacket.class);
        packetMap.put(4, OnlineUsersPacket.class);
        packetMap.put(5, MessagePacket.class);
        packetMap.put(6, ChatMessagePacket.class);
        packetMap.put(7, OperationPacket.class);
    }

    public static Class<? extends Packet> getPacketClass(int packetId) {
        return packetMap.get(packetId);
    }

    public static Packet createPacket(int packetId) {
        Class<? extends Packet> packetClass = packetMap.get(packetId);
        if (packetClass != null) {
            try {
                return packetClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                Logger.error("Error instantiating packet class for ID " + packetId + ": " + e.getMessage());
            }
        }
        return null;
    }

    public static Integer getPacketId(Class<? extends Packet> packetClass) {
        return packetMap.entrySet()
                .stream()
                .filter(entry -> packetClass.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
