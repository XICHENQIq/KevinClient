package kevin.module.modules.player

import kevin.event.EventTarget
import kevin.event.MoveEvent
import kevin.event.PacketEvent
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.PacketUtils
import kevin.utils.Rotation
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketEntityVelocity
import java.util.concurrent.LinkedBlockingQueue

class Stuck : Module("Stuck","卡空", category = ModuleCategory.PLAYER) {

    private var s12: SPacketEntityVelocity? = null
    private val c0fPackets = LinkedBlockingQueue<CPacketConfirmTransaction>()

    private var motionX = 0.0
    private var motionY = 0.0
    private var motionZ = 0.0

    private var c05Packets = 0
    private var lastRotation: Rotation? = null

    override fun onEnable() {
        motionX = mc.player.motionX
        motionY = mc.player.motionY
        motionZ = mc.player.motionZ
        lastRotation = Rotation(mc.player.rotationYaw,mc.player.rotationPitch)
    }

    override fun onDisable() {
        mc.player.motionX = motionX
        mc.player.motionY = motionY
        mc.player.motionZ = motionZ

        if (s12 != null) {
            s12!!.processPacket(mc.connection!!)
        }
        s12 = null
        packet()
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        event.zero()
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is CPacketConfirmTransaction) {
            event.cancelEvent()
            c0fPackets.add(packet)
        }
        if (packet is CPacketPlayer) {
            event.cancelEvent()
        }
        if (packet is SPacketEntityVelocity) {
            if (packet.entityID == mc.player.entityId) {
                s12 = packet
            }
        }
        if (packet is CPacketPlayerTryUseItemOnBlock) {
            event.cancelEvent()
            if (lastRotation != Rotation(mc.player.rotationYaw,mc.player.rotationPitch)) {
                lastRotation = Rotation(mc.player.rotationYaw,mc.player.rotationPitch)
                PacketUtils.sendPacketNoEvent(CPacketPlayer.Rotation(mc.player.rotationYaw,mc.player.rotationPitch,false))
                PacketUtils.sendPacketNoEvent(packet)
                c05Packets ++
            }
        }
        if (packet is CPacketPlayerDigging && mc.player.heldItemMainhand.item is ItemBow) {
            event.cancelEvent()
            if (lastRotation != Rotation(mc.player.rotationYaw,mc.player.rotationPitch)) {
                lastRotation = Rotation(mc.player.rotationYaw,mc.player.rotationPitch)
                PacketUtils.sendPacketNoEvent(CPacketPlayer.Rotation(mc.player.rotationYaw,mc.player.rotationPitch,false))
                PacketUtils.sendPacketNoEvent(packet)
                c05Packets ++
            }
        }
        if (packet is CPacketPlayerTryUseItem && mc.player.heldItemMainhand.item !is ItemBow) {
            event.cancelEvent()
            if (lastRotation != Rotation(mc.player.rotationYaw,mc.player.rotationPitch)) {
                lastRotation = Rotation(mc.player.rotationYaw,mc.player.rotationPitch)
                PacketUtils.sendPacketNoEvent(CPacketPlayer.Rotation(mc.player.rotationYaw,mc.player.rotationPitch,false))
                PacketUtils.sendPacketNoEvent(packet)
                c05Packets ++
            }
        }
    }
    private fun packet() {
        try {
            while (!c0fPackets.isEmpty()) {
                PacketUtils.sendPacketNoEvent(c0fPackets.take())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}