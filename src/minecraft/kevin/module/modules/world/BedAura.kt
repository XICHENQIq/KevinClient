package kevin.module.modules.world

import kevin.event.*
import kevin.module.BooleanValue
import kevin.module.FloatValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.*
import kevin.utils.timers.MSTimer
import net.minecraft.block.BlockBed
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.collections.ArrayList

class BedAura : Module(name = "BedAura", description = "tp damage all bed", category = ModuleCategory.WORLD) {

    private val rangeValue = FloatValue("Range", 20f, 1.0f, 50f)

    private var isBreaking = false
    private var targetBlockPos: BlockPos? = null
    private val msTimer = MSTimer()


    val msTimer2 = MSTimer()

    var needSend1 = false
    var needSend2 = false
    var needSend3 = false



    override fun onEnable() {
        isBreaking = false
        targetBlockPos = null

    }

    @EventTarget
    fun onUpdate2(event: UpdateEvent) {
        if (targetBlockPos == null && !isBreaking && msTimer2.hasTimePassed(200L)) {
            targetBlockPos = findBad(rangeValue.get())
            msTimer2.reset()
        }
        if (targetBlockPos != null && !isBreaking) {
            isBreaking = true
            msTimer.reset()

            needSend1 = true
            needSend2 = true

        }
        if (targetBlockPos != null && isBreaking) {
            if (msTimer.hasTimePassed(500L)) {
                isBreaking = false
                msTimer.reset()

                needSend3 = true
                needSend2 = true


                targetBlockPos = null
            }
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is CPacketPlayer) {
            if (isBreaking) {
                event.cancelEvent()
            }
            if (needSend1) {
                needSend1 = false
                ChatUtils.message("send break start")
                PacketUtils.sendPacketNoEvent(CPacketPlayer.Position(targetBlockPos!!.x.toDouble(), targetBlockPos!!.y.toDouble() + 1.0,targetBlockPos!!.z.toDouble(),true))
                PacketUtils.sendPacketNoEvent(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,targetBlockPos!!,EnumFacing.UP))
                return
            }
            if (needSend2) {
                needSend2 = false
                ChatUtils.message("send back")
                PacketUtils.sendPacketNoEvent(CPacketPlayer.Position(mc.player.posX, mc.player.posY,mc.player.posZ,true))
                return
            }
            if (needSend3) {
                needSend3 = false
                ChatUtils.message("send break stop")
                PacketUtils.sendPacketNoEvent(CPacketPlayer.PositionRotation(targetBlockPos!!.x.toDouble(), targetBlockPos!!.y.toDouble() + 1.0,targetBlockPos!!.z.toDouble(),mc.player.rotationYaw,90f,true))
                PacketUtils.sendPacketNoEvent(CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, BlockPos(mc.player.posX.toInt(),mc.player.posY.toInt() + 10,mc.player.posZ.toInt()),EnumFacing.UP))
                PacketUtils.sendPacketNoEvent(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,targetBlockPos!!,EnumFacing.UP))
                return
            }
        }

    }



    private fun findBad(range: Float) : BlockPos? {
        val blocks = BlockUtils.searchBlocks(range.toInt())
        for (block in blocks) {
            if (block.value is BlockBed) {
                return block.key
            }
        }
        return null
    }
}