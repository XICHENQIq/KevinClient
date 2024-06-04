/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package kevin.module.modules.combat

import com.viaversion.viaversion.api.type.types.minecraft.ItemType
import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.module.*
import kevin.utils.InventoryUtils
import kevin.utils.PacketUtils
import kevin.utils.timers.MSTimer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
import net.minecraft.item.ItemSoup
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

class AutoSoup : Module(name = "AutoSoup", description = "在你血量较低时自动喝蘑菇煲.", category = ModuleCategory.COMBAT) {

    private val healthValue = FloatValue("Health", 15f, 0f, 20f)
    private val delayValue = IntegerValue("Delay", 150, 0, 500)
    private val openInventoryValue = BooleanValue("OpenInv", false)
    private val bowlValue = ListValue("Bowl", arrayOf("Drop", "Move", "Stay"), "Drop")

    private val timer = MSTimer()

    override val tag: String
        get() = healthValue.get().toString()

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (!timer.hasTimePassed(delayValue.get().toLong()))
            return

        val thePlayer = mc.player ?: return

        val soupInHotbar = InventoryUtils.findItem(36, 45, Items.MUSHROOM_STEW)

        if (thePlayer.health <= healthValue.get() && soupInHotbar != -1) {
            PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(soupInHotbar - 36))
            PacketUtils.sendPacketNoEvent(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))

            if (bowlValue.get().equals("Drop", true))
                PacketUtils.sendPacketNoEvent(CPacketPlayerDigging(CPacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))

            PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(thePlayer.inventory.currentItem))
            timer.reset()
            return
        }

        val bowlInHotbar = InventoryUtils.findItem(36, 45, Items.BOWL)
        if (bowlValue.get().equals("Move", true) && bowlInHotbar != -1) {
            if (openInventoryValue.get() && mc.currentScreen !is GuiInventory)
                return

            var bowlMovable = false

            for (i in 9..36) {
                val itemStack = thePlayer.inventory.getStackInSlot(i)

                if (itemStack == null) {
                    bowlMovable = true
                    break
                } else if (itemStack.item == Items.BOWL && itemStack.stackSize < 64) {
                    bowlMovable = true
                    break
                }
            }

            if (bowlMovable) {

                mc.playerController.windowClick(0, bowlInHotbar, 0, ClickType.QUICK_MOVE, thePlayer)
            }
        }

        val soupInInventory = InventoryUtils.findItem(9, 36, Items.MUSHROOM_STEW)

        if (soupInInventory != -1 && InventoryUtils.hasSpaceHotbar()) {
            if (openInventoryValue.get() && mc.currentScreen !is GuiInventory)
                return

            mc.playerController.windowClick(0, soupInInventory, 0, ClickType.QUICK_MOVE, thePlayer)

            timer.reset()
        }
    }

}