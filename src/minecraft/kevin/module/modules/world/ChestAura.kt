/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package kevin.module.modules.world

import kevin.KevinClient
import kevin.event.EventTarget
import kevin.event.StrafeEvent
import kevin.event.UpdateEvent
import kevin.event.WorldEvent
import kevin.module.FloatValue
import kevin.module.IntegerValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.module.modules.movement.Sprint
import kevin.module.modules.player.Blink
import kevin.module.modules.render.FreeCam
import kevin.utils.BlockUtils
import kevin.utils.Rotation
import kevin.utils.RotationUtils
import kevin.utils.timers.MSTimer
import net.minecraft.block.BlockChest
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameType
import com.yumegod.obfuscation.Native

@Native
class ChestAura : Module(name = "ChestAura", description = "自动打开你周围的箱子", category = ModuleCategory.WORLD) {

    private val range = FloatValue("ClickRange",4.5f,0f,6f)
    private val delay = IntegerValue("ClickDelay",800,0,2000)

    private val msTimer = MSTimer()

    val clickedBlocks = mutableListOf<BlockPos>()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.currentScreen is GuiChest || mc.playerController.currentGameType == GameType.ADVENTURE || (KevinClient.moduleManager.getModule(Stealer::class.java).state && (KevinClient.moduleManager.getModule(Stealer::class.java) as Stealer).hasWindow) || mc.gameSettings.keyBindUseItem.pressed) {
            msTimer.reset()
            return
        }
        if (KevinClient.moduleManager[Blink::class.java].state || KevinClient.moduleManager[FreeCam::class.java].state || !msTimer.hasTimePassed(delay.get().toLong()))
            return

        val searchBlocks = BlockUtils.searchBlocks(range.get().toInt() + 2)
        for (block in searchBlocks) {
            if (block.value is BlockChest && !clickedBlocks.contains(block.key) && BlockUtils.getCenterDistance(block.key) <= range.get() && !BlockUtils.getBlock(BlockPos(block.key.x,block.key.y + 1,block.key.z))!!.isFullBlock(mc.world.getBlockState(BlockPos(block.key.x,block.key.y + 1,block.key.z)))){
                Sprint.canSprint = false
                Sprint.sprintTick = 0
                RotationUtils.setTargetRotation((RotationUtils.faceBlock(block.key) ?: return).rotation,0)
                mc.connection!!.networkManager.sendPacket(CPacketPlayerTryUseItemOnBlock(block.key,EnumFacing.UP,EnumHand.MAIN_HAND,0f,0f,0f))
                mc.player.swingArm(EnumHand.MAIN_HAND)
                clickedBlocks.add(block.key)
                msTimer.reset()
                return
            }
        }
    }

    override fun onDisable() {
        clickedBlocks.clear()
    }
    @EventTarget
    fun onWorld(event: WorldEvent) {
        clickedBlocks.clear()
    }
}