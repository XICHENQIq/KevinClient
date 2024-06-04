package kevin.module.modules.misc

import io.netty.buffer.Unpooled
import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.hud.xichenqi.GermUI
import kevin.module.Module
import kevin.module.ModuleCategory
import net.minecraft.item.ItemSimpleFoiled
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketChatMessage
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import com.yumegod.obfuscation.Native

@Native
class Germ : Module("Germ", "萌芽引擎", category = ModuleCategory.MISC) {

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is CPacketPlayerTryUseItem) {
            if (mc.currentScreen == null && mc.player.heldItemMainhand.item is ItemSimpleFoiled) {

                mc.displayGuiScreen(GermUI())

            }
        }
        if (packet is CPacketChatMessage) {
            if (packet.message == "/game sw-1") {
                event.cancelEvent()
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 48, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 48, 44, 34, 115, 105, 100, 34, 58, 34, 83, 75, 89, 87, 65, 82, 47, 110, 115, 107, 121, 119, 97, 114, 34, 125)))))
            }
            if (packet.message == "/game sw-2") {
                event.cancelEvent()
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 49, 41, 123, 34, 101, 110, 116, 114, 121, 34, 58, 49, 44, 34, 115, 105, 100, 34, 58, 34, 83, 75, 89, 87, 65, 82, 47, 110, 115, 107, 121, 119, 97, 114, 45, 100, 111, 117, 98, 108, 101, 34, 125)))))
            }
            if (packet.message == "/germui") {
                event.cancelEvent()
                mc.displayGuiScreen(GermUI())
            }
        }
    }
}