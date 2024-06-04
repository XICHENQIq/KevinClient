package kevin.module.modules.misc

import kevin.KevinClient
import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.event.UpdateEvent
import kevin.event.WorldEvent
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.module.modules.player.Blink
import kevin.utils.ChatUtils
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.network.play.server.SPacketSetSlot
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameType

class SwPhase : Module(name = "SwPhase", description = "穿墙", category = ModuleCategory.MISC) {

    var hasBlock = false
    var a = false
    override fun onEnable() {
        hasBlock = false
        a = false
    }
    @EventTarget
    fun onWorld(event: WorldEvent) {
        hasBlock = false
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (a) {
            mc.playerController.setGameType(GameType.SURVIVAL)
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet


        if (packet is SPacketChat) {
            val msg = packet.chatComponent.unformattedText
            if (msg.contains("你选择了") && msg.contains("附魔师")) {
                hasBlock = true
            }
            if (msg.contains("开始倒计时") && msg.contains("1")) {
                Thread {
                    try {
                        Thread.sleep(1000)
                        val blink = KevinClient.moduleManager.getModule(Blink::class.java) as Blink
                        blink.state = false
                        a = false
                    } catch (ex: InterruptedException) {
                        ex.printStackTrace()
                    }
                }.start()
            }
            if (msg.contains("开始倒计时") && msg.contains("5")) {
                val blink = KevinClient.moduleManager.getModule(Blink::class.java) as Blink

                if (blink.state) {
                    blink.state = false
                }

                blink.state = true
                Thread {
                    try {
                        Thread.sleep(6000)
                        val blink = KevinClient.moduleManager.getModule(Blink::class.java) as Blink
                        blink.state = false
                        a = false
                    } catch (ex: InterruptedException) {
                        ex.printStackTrace()
                    }
                }.start()

                mc.connection!!.networkManager.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ),EnumFacing.UP))
                mc.connection!!.networkManager.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos(mc.player.posX, mc.player.posY - 2, mc.player.posZ),EnumFacing.UP))
                mc.connection!!.networkManager.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos(mc.player.posX, mc.player.posY - 3, mc.player.posZ),EnumFacing.UP))
                mc.world.setBlockToAir(BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ))
                mc.world.setBlockToAir(BlockPos(mc.player.posX, mc.player.posY - 2, mc.player.posZ))
                mc.world.setBlockToAir(BlockPos(mc.player.posX, mc.player.posY - 3, mc.player.posZ))


                if (hasBlock) {
                    SPacketSetSlot(0,37, ItemStack(Item.getItemFromBlock(Blocks.BOOKSHELF),16)).processPacket(mc.connection!!)
                }

                a = true

            }
        }
    }

}