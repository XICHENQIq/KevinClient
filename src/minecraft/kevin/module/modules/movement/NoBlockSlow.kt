package kevin.module.modules.movement

import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.module.*
import kevin.utils.BlockUtils
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockWeb
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing

class NoBlockSlow : Module(name = "NoBlockSlow", description = "bzd",category = ModuleCategory.MOVEMENT) {
    private val mode = ListValue("Mode", arrayOf("SuperRange","BoundingBox"),"BoundingBox")

    private val web = BooleanValue("Web",true)
    private val liquid = BooleanValue("Liquid",false)
    private val silent = BooleanValue("Silent",true)

    private val refreshTick = IntegerValue("RefreshTick",10,0,40)

    private var tick = 0


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (mode.get()) {
            "SuperRange" -> {
                tick++
                if (tick >= refreshTick.get()) {
                    tick = 0

                    val searchBlocks = BlockUtils.searchBlocks(10)
                    for (block in searchBlocks) {
                        if (block.value is BlockWeb && web.get() && BlockUtils.getCenterDistance(block.key) >= 7) {
                            mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block.key, EnumFacing.DOWN))
                            mc.world.setBlockToAir(block.key)
                        }
                        if (block.value is BlockLiquid && liquid.get() && BlockUtils.getCenterDistance(block.key) >= 7) {
                            mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block.key, EnumFacing.DOWN))
                            mc.world.setBlockToAir(block.key)
                        }
                    }
                }
            }
            "BoundingBox" -> {
                val searchBlocks = BlockUtils.searchBlocks(5)
                for (block in searchBlocks) {
                    if (block.value is BlockWeb && web.get()){
                        val blockBox = BlockUtils.getBlock(block.key)!!.getSelectedBoundingBox(mc.world.getBlockState(block.key),mc.world,block.key)
                        val playerBox = mc.player.entityBoundingBox
                        if (blockBox.maxX > playerBox.minX && blockBox.minX < playerBox.maxX && blockBox.maxZ > playerBox.minZ && blockBox.minZ < playerBox.maxZ && blockBox.maxY > playerBox.minY && blockBox.minY < playerBox.maxY) {
                            mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block.key, EnumFacing.DOWN))
                            if (silent.get()) {
                                mc.player.isInWeb = false
                            } else {
                                mc.world.setBlockToAir(block.key)
                            }
                        }
                    }
                    if (block.value is BlockLiquid && liquid.get()){
                        val blockBox = BlockUtils.getBlock(block.key)!!.getSelectedBoundingBox(mc.world.getBlockState(block.key),mc.world,block.key)
                        val playerBox = mc.player.entityBoundingBox
                        if (blockBox.maxX > playerBox.minX && blockBox.minX < playerBox.maxX && blockBox.maxZ > playerBox.minZ && blockBox.minZ < playerBox.maxZ && blockBox.maxY > playerBox.minY && blockBox.minY < playerBox.maxY) {
                            mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block.key, EnumFacing.DOWN))
                            if (silent.get()) {
                                mc.player.isInWeb = false
                            } else {
                                mc.world.setBlockToAir(block.key)
                            }
                        }
                    }
                }
            }
        }
    }
}