/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package kevin.module.modules.movement

import com.yumegod.obfuscation.Native
import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.event.WorldEvent
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.BlockUtils
import net.minecraft.block.BlockLadder
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

@Native
class FastLadder : Module(name = "FastLadder", description = "Grim - 在梯子上快速降落", category = ModuleCategory.MOVEMENT) {
    private val blockLadder = mutableListOf<BlockPos>()
    var cancel = false

    private var normalClimb = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!mc.player.isOnLadder) {
            normalClimb = false
            cancel = false
            blockLadder.clear()
        }
        if (!mc.player.isOnLadder && !cancel) {
            return
        }
        if (normalClimb && mc.player.isOnLadder) {
            return
        }
        if (mc.player.isOnLadder && mc.gameSettings.keyBindJump.isKeyDown) {
            blockLadder.clear()
            cancel = false
            normalClimb = true
            return
        }
        for (block in BlockUtils.searchBlocks(4)) {
            if (block.value is BlockLadder) {
                if (!blockLadder.contains(block.key)) {
                    blockLadder.add(block.key)
                }
            }
        }
        if (blockLadder.isNotEmpty()) {
            for (block in blockLadder) {
                mc.connection!!.networkManager.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,block,EnumFacing.DOWN))
            }
            if (mc.player.isOnLadder) {
                cancel = true
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        blockLadder.clear()
    }
}