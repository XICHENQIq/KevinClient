package kevin.module.modules.player

import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.module.FloatValue
import kevin.module.IntegerValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.EntityUtils
import kevin.utils.PacketUtils
import kevin.utils.Rotation
import kevin.utils.RotationUtils
import kevin.utils.timers.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class ProjectilesAura : Module(name = "ProjectilesAura", description = "投掷物光环", category = ModuleCategory.PLAYER) {


    private val range = FloatValue("Range",8f,0f,20f)
    private val hurtTime = IntegerValue("HurtTime",2,0,10)

    val msTimer = MSTimer()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        if (!msTimer.hasTimePassed(300))
            return

        var targetSlot = getItemFromHotbar(332)
        if (targetSlot == -1) {
            targetSlot = getItemFromHotbar(344)
        }
        if (targetSlot == -1)
            return

        for (entity in mc.world.loadedEntityList) {
            if (EntityUtils.isSelected(entity,true) && mc.player.getDistanceToEntity(entity) <= range.get() && (entity as EntityLivingBase).hurtTime <= hurtTime.get()) {
                val bb = entity.entityBoundingBox

                val rotation = RotationUtils.limitAngleChange(
                    Rotation(mc.player.rotationYaw, mc.player.rotationPitch),
                    RotationUtils.toRotation(RotationUtils.getCenter(entity.entityBoundingBox), true),
                    180f
                )
                if (mc.player.inventory.currentItem != targetSlot) {
                    mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange(targetSlot))
                }
                RotationUtils.setTargetRotation(rotation)
                PacketUtils.sendPacketNoEvent(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                if (mc.player.inventory.currentItem != targetSlot) {
                    mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
                }

                msTimer.reset()
            }
        }
    }

    private fun getItemFromHotbar(id: Int): Int {
        for (i in 0..8) {
            val a: ItemStack = mc.player!!.inventory.mainInventory[i]
            val item1 = a.item
            if (Item.getIdFromItem(item1) == id) {
                return i
            }
        }
        return -1
    }
}