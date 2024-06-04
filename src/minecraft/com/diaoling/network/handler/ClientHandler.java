package com.diaoling.network.handler;

import com.diaoling.network.packet.Packet;
import com.diaoling.network.packet.impl.info.UserInfoPacket;
import com.diaoling.utils.misc.Logger;
import com.diaoling.utils.misc.enums.ClientType;
import com.diaoling.utils.misc.enums.Rank;
import com.yumegod.obfuscation.Native;
import com.yumegod.obfuscation.StringObfuscate;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import kevin.KevinClient;
import kevin.utils.*;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author DiaoLing
 * @since 4/7/2024
 */
@Native
@StringObfuscate
public class ClientHandler extends SimpleChannelInboundHandler<Packet> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        Logger.info("Received packet: " + packet.getClass().getSimpleName());

        packet.handler(ctx, this);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Logger.info("Connected to server: " + ctx.channel().remoteAddress());


        /*final String text1 = WebUtils.get(KevinClient.CLIENT_CLOUD + "ranks.json");
        String[] lines = text1.split(System.lineSeparator());
        List<String> texts = new ArrayList<>(Arrays.asList(lines));

        for (String text : texts) {
            if (StringUtils.INSTANCE.extractContent(text, "<", ">") == HWIDUtils.getHWID()) {
                ctx.writeAndFlush(new UserInfoPacket(
                        ClientType.KEVIN,
                        0,
                        StringUtils.INSTANCE.extractContent(text, "{", "}"),
                        Rank.ADMIN,
                        0,
                        0
                ));

                return;
            }
        }*/
        String hwid = HWIDUtils.getHWID();
        if (hwid.equals("3b3-3e8-30d-325-3c3-3dc-342-33d-375-3fa-3db-3e5-3a9-3ad-31a-3c3")) {
            ctx.writeAndFlush(new UserInfoPacket(
                    ClientType.KEVIN,
                    0,
                    "xichenqi",
                    Rank.ADMIN,
                    0,
                    0
            ));
            return;
        }

        ctx.writeAndFlush(new UserInfoPacket(
                ClientType.KEVIN,
                0,
                "KevinUser-" + RandomUtils.randomString(10),
                Rank.USER,
                0,
                0
        ));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Logger.warning("Disconnected from server.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SocketException) {
            Logger.error("Connection reset by peer or server shutdown.");
        } else {
            cause.printStackTrace();
        }
        ctx.close();
    }
}