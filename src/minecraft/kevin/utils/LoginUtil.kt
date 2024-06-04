package kevin.utils

import com.diaoling.network.packet.impl.info.UserInfoPacket
import com.diaoling.utils.misc.enums.ClientType
import com.diaoling.utils.misc.enums.Rank
import io.netty.channel.ChannelHandlerContext
import kevin.KevinClient
import com.yumegod.obfuscation.Native

@Native
object LoginUtil {

    fun login(ctx: ChannelHandlerContext) {

        val text1 = WebUtils.get(KevinClient.CLIENT_CLOUD + "ranks.json")
        val lines = text1.split(System.lineSeparator()).toTypedArray()

        for (text in lines) {
            if (StringUtils.extractContent(text, "<", ">") == HWIDUtils.getHWID()) {
                ctx.writeAndFlush(
                    UserInfoPacket(
                        ClientType.KEVIN,
                        0,
                        StringUtils.extractContent(text, "{", "}"),
                        Rank.ADMIN,
                        0,
                        0.0
                    )
                )

                return
            }
        }
        ctx.writeAndFlush(
            UserInfoPacket(
                ClientType.KEVIN,
                0,
                "KevinUser-" + RandomUtils.randomString(10),
                Rank.USER,
                0,
                0.0
            )
        )
    }
}