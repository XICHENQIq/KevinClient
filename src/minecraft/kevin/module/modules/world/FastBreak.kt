package kevin.module.modules.world

import kevin.KevinClient
import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.event.UpdateEvent
import kevin.module.FloatValue
import kevin.module.Module
import kevin.module.ModuleCategory
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

class FastBreak : Module("FastBreak", "Allows you to break blocks faster.", category = ModuleCategory.WORLD) {

    private val breakDamage = FloatValue("BreakDamage", 0.8F, 0.1F, 1F)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.playerController.blockHitDelay = 0

        if (mc.playerController.curBlockDamageMP > breakDamage.get())
            mc.playerController.curBlockDamageMP = 1F

        val breaker = KevinClient.moduleManager.getModule("Breaker") as Breaker
        val nuker = KevinClient.moduleManager.getModule("Nuker") as Nuker

        if (breaker.currentDamage > breakDamage.get())
            breaker.currentDamage = 1F

        if (nuker.currentDamage > breakDamage.get())
            nuker.currentDamage = 1F
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is CPacketPlayerDigging){
            if (packet.action == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK){
                mc.connection!!.networkManager.sendPacket(
                        CPacketPlayerDigging(
                                CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
                                BlockPos(
                                        mc.player.posX,
                                        mc.player.posY + 10,
                                        mc.player.posZ
                                ),
                                EnumFacing.UP
                        )
                )
            }
        }
    }
}