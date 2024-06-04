package kevin.module.modules.combat

import com.yumegod.obfuscation.Native
import kevin.KevinClient
import kevin.event.*
import kevin.hud.font.FontLoader
import kevin.module.BooleanValue
import kevin.module.IntegerValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.module.modules.movement.TntFly
import kevin.utils.PacketUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.item.Item
import net.minecraft.item.Item.getIdFromItem
import net.minecraft.item.ItemStack
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.concurrent.LinkedBlockingQueue


@Native
class DuelGod : Module(name = "DuelGod", description = "对刀神", category = ModuleCategory.COMBAT) {

    private val autoDis = BooleanValue("AutoDisable",true)
    private val fullUpdate = BooleanValue("FullC03",false)
    private val keepC03 = BooleanValue("KeepC03",true)
    private val c03tick = IntegerValue("C03Tick",4,0,20)
    val drop = BooleanValue("DropNoSlow",true)

    var i = 0
    val packets = LinkedBlockingQueue<Packet<INetHandlerPlayServer>>()
    private var waitingC03 = false
    var tick = 0
    private var disable = false

    override fun onEnable() {
        waitingC03 = false
        tick = 0
        i = 0

    }

    override fun onDisable() {

        val targetSlot = getItemFromHotbar(322)
        if (targetSlot != -1) {
            if (mc.player.inventory.currentItem != targetSlot) {
                PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(targetSlot))
                PacketUtils.sendPacketNoEvent(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                if (drop.get()) {
                    PacketUtils.sendPacketNoEvent(CPacketPlayerDigging(CPacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN,EnumFacing.DOWN))

                }
                blink()
                PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(mc.player.inventory.currentItem))
            } else {
                PacketUtils.sendPacketNoEvent(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                if (drop.get()) {
                    PacketUtils.sendPacketNoEvent(CPacketPlayerDigging(CPacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN,EnumFacing.DOWN))
                }
                blink()
            }
        } else {
            blink()
        }
        val tntFly = KevinClient.moduleManager.getModule(TntFly::class.java) as TntFly

        tntFly.state = true
        Thread {
            try {
                Thread.sleep(500)
                tntFly.state = false
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
            }
        }.start()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.player == null)
            return

        if (packet is CPacketPlayer) {
            waitingC03 = false
        }

        if (packet is CPacketPlayerTryUseItemOnBlock) {
            event.cancelEvent()
        }

        if (packet is CPacketPlayer || packet is CPacketConfirmTransaction || packet is CPacketEntityAction) {
            event.cancelEvent()
            packets.add(packet as Packet<INetHandlerPlayServer>)
            i = 0
            for (index in packets) {
                if (index is CPacketPlayer) {
                    i ++
                }
            }
            if (i >= 37 && autoDis.get()) {
                disable = true
                i = 0
            }
        }
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (keepC03.get()) {
            tick ++
            if (tick >= c03tick.get()) {
                tick = 0
                if (!send()) {
                    send()
                }
            }
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sr = ScaledResolution(mc)
        mc.fontRenderer.drawCenteredString(i.toString(),sr.scaledWidth / 2f ,sr.scaledHeight / 2f + 10, Color.WHITE.rgb,true)
    }

    @EventTarget
    fun onMotion(event: MotionEvent){
        if (event.eventState == EventState.PRE){
            if (disable) {
                disable = false
                this.state = false
            }
            waitingC03 = true
        } else if (waitingC03 && fullUpdate.get()) {
            mc.connection!!.networkManager.sendPacket(CPacketPlayer(mc.player.onGround))
        }
    }
    private fun blink() {
        try {
            while (!packets.isEmpty()) {
                val packet = packets.take()
                PacketUtils.sendPacketNoEvent(packet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun send(): Boolean {
        try {
            if (!packets.isEmpty()) {
                val packet = packets.take()
                PacketUtils.sendPacketNoEvent(packet)
                if (packet is CPacketPlayer) {
                    return true
                } else {
                    return false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
    private fun getItemFromHotbar(id: Int): Int {
        for (i in 0..8) {
            if (mc.player.inventory.mainInventory[i] != null) {
                val a: ItemStack? = mc.player.inventory.mainInventory[i]
                val item = a!!.item
                if (getIdFromItem(item) == id) {
                    return i
                }
            }
        }
        return -1
    }
}