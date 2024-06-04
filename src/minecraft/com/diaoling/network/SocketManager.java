package com.diaoling.network;

import com.diaoling.network.client.SocketClient;
import com.diaoling.network.packet.Packet;
import com.diaoling.network.packet.impl.message.ChatMessagePacket;
import com.diaoling.network.packet.impl.operation.OperationPacket;
import com.diaoling.utils.misc.enums.ChannelType;
import com.diaoling.utils.misc.enums.Operation;
import com.yumegod.obfuscation.Native;
import kevin.KevinClient;

/**
 * @author DiaoLing
 * @since 4/8/2024
 */
@Native
public class SocketManager {
    private final SocketClient client = new SocketClient();

    private static String prefix = "!";

    /**
     * Initializes the SocketManager.
     */
    /*@Override
    public void initialize() {
        Example.getEventBus().register(this);
    }*/

    public SocketClient getClient() {
        return client;
    }

    public String getPrefix() {
        return prefix;
    }

    public void send(Packet packet) {
        client.send(packet);
    }

    public void chat(String message) {
        this.send(new ChatMessagePacket(
                ChannelType.GLOBAL,
                message,
                System.currentTimeMillis()));
    }

    public void operation(Operation operation, String targetUsername, String message) {
        this.send(new OperationPacket(
                KevinClient.userManager.getUser().getUsername(),
                targetUsername,
                message,
                operation
        ));
    }
}
