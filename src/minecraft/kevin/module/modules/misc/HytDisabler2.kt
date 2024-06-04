package kevin.module.modules.misc

import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.module.Module
import kevin.module.ModuleCategory
import net.minecraft.entity.player.PlayerCapabilities
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerAbilities

class HytDisabler2: Module(name = "HytDisabler2", description = "吃鸡", category = ModuleCategory.MISC) {
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is CPacketPlayer) {
            val capabilities = PlayerCapabilities()
            capabilities.disableDamage = false
            capabilities.isFlying = true
            capabilities.allowFlying = false
            capabilities.isCreativeMode = false
            mc.connection!!.networkManager.sendPacket(CPacketPlayerAbilities(capabilities))
            packet.onGround = false
        }
    }

}