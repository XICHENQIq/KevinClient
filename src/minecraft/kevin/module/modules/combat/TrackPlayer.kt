package kevin.module.modules.combat

import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.event.UpdateEvent
import kevin.module.IntegerValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.PacketUtils
import kevin.utils.timers.MSTimer
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketConfirmTransaction
import net.minecraft.network.play.server.SPacketEntity
import net.minecraft.util.math.Vec3d
import java.util.concurrent.LinkedBlockingQueue

class TrackPlayer : Module (name = "TrackPlayer", description = "更快追上逃跑的小老鼠 :)", category = ModuleCategory.COMBAT) {

    private val delay = IntegerValue("Delay",2000,0,5000)

    private val packets = LinkedBlockingQueue<Pair<Packet<INetHandlerPlayClient>?, Vec3d?>>()

    var target: EntityLivingBase? = null
    var fakePlayer: EntityOtherPlayerMP? = null

    val msTimer = MSTimer()

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is CPacketUseEntity) {
            if (fakePlayer == null) {
                target = packet.getEntityFromWorld(mc.world) as EntityLivingBase?
                val faker = fakePlayer

                if (faker != null) {
                    mc.world?.removeEntityFromWorld(faker.entityId)
                    fakePlayer = null
                }
                blink()
                msTimer.reset()
            } else {
                target = null
                blink()
                event.cancelEvent()
            }


        }
        if (target == null || !msTimer.hasTimePassed(delay.get().toLong())) {

            return
        }

        if (packet is SPacketEntity) {
            if (packet.getEntity(mc.world) == target) {

                packets.add(Pair(null, Vec3d(packet.getEntity(mc.world).posX,packet.getEntity(mc.world).posY,packet.getEntity(mc.world).posZ)))

            }
            if (fakePlayer == null) {
                val faker = EntityOtherPlayerMP(mc.world!!, (target as EntityPlayer).gameProfile)

                faker.posX = target!!.posX
                faker.posY = target!!.posY
                faker.posZ = target!!.posZ

                faker.rotationYawHead = target!!.rotationYawHead
                faker.renderYawOffset = target!!.renderYawOffset
                faker.rotationYawHead = target!!.rotationYawHead
                mc.world!!.addEntityToWorld(-1338, faker)


                fakePlayer = faker
            }
        }
        if (packet is SPacketConfirmTransaction) {
            event.cancelEvent()
            packets.add(Pair(packet, null))
            PacketUtils.sendPacketNoEvent(CPacketConfirmTransaction(0,0,true))
        }
        if (packet is CPacketPlayer) {
            if (mc.player.getDistanceToEntity(fakePlayer!!) >= 1) {
                processPacketsBeforeMin(packets)

            }
        }
    }
    private fun blink() {
        try {

            while (!packets.isEmpty()) {
                val packet = packets.take()
                if (packet.first != null) {
                    packet.first!!.processPacket(mc.connection!!)
                }
            }
            val faker = fakePlayer

            if (faker != null) {
                mc.world?.removeEntityFromWorld(faker.entityId)
                fakePlayer = null
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getMinValueFromList(list: LinkedBlockingQueue<Pair<Packet<INetHandlerPlayClient>?, Vec3d?>>): Pair<Packet<INetHandlerPlayClient>?, Vec3d?> {
        var minValue = list.peek()
        for (value in list) {
            if (mc.player.getDistanceToVec3d(value.second!!) < mc.player.getDistanceToVec3d(minValue.second!!)) {
                minValue = value
            }
        }
        return minValue
    }
    fun processPacketsBeforeMin(list: LinkedBlockingQueue<Pair<Packet<INetHandlerPlayClient>?, Vec3d?>>) {
        val minValue = getMinValueFromList(list)
        if (mc.player.getDistanceToVec3d(minValue.second!!) >= 2)
            return
        for (value in list) {
            if (value == minValue) {
                break // 找到最小值，停止处理
            }
            packet(value) // 应用 packet() 方法处理先于最小值存储的元素
            list.remove(value)
        }
    }
    fun packet(pair: Pair<Packet<INetHandlerPlayClient>?, Vec3d?>) {
        if (pair.first != null) {
            pair.first!!.processPacket(mc.connection!!)
            return
        }
        if (fakePlayer != null) {
            if (pair.second != null) {
                fakePlayer!!.posX = pair.second!!.x
                fakePlayer!!.posY = pair.second!!.y
                fakePlayer!!.posZ = pair.second!!.z
            }
        }
    }
}