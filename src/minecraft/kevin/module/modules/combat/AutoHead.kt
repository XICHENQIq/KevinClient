package kevin.module.modules.combat

import kevin.event.EventTarget
import kevin.event.MotionEvent
import kevin.event.UpdateEvent
import kevin.module.*
import kevin.utils.PacketUtils
import kevin.utils.TimeUtils
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.MobEffects
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand
import org.lwjgl.input.Mouse

class AutoHead : Module(name = "AutoHead", description = "自动吃金头",  category = ModuleCategory.COMBAT) {

    private var switched = -1
    private val timer = TimeUtils()
    private val eatHeads = BooleanValue("EatHead", true)
    private val health = FloatValue("Health", 10.0f, 1.0f, 20.0f)
    private val delay = IntegerValue("Delay", 750, 100, 2000)

    override fun onEnable() {
        switched = -1
        timer.reset()
        super.onEnable()
    }


    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.player == null) return
        val inventory = mc.player!!.inventory ?: return
        if (!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)) {
            if (!timer.hasReached(delay.get().toDouble())) {
                return
            }
            if (mc.player!!.capabilities.isCreativeMode || mc.player!!.isPotionActive(MobEffects.REGENERATION) || mc.player!!.health >= health.get()) {
                timer.reset()
                return
            }
            for (i in 0..1) {
                val doEatHeads = i != 0
                if (doEatHeads) {
                    if (!eatHeads.get()) continue
                } else {
                    continue
                }
                val slot: Int = if (doEatHeads) {
                    getItemFromHotbar(397)
                } else {
                    getItemFromHotbar(322)
                }
                if (slot == -1) continue
                val tempSlot = inventory.currentItem
                if (doEatHeads) {
                    PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(slot))
                    PacketUtils.sendPacketNoEvent(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                    PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(tempSlot))
                    timer.reset()
                } else {
                    inventory.currentItem = slot
                    switched = tempSlot
                }
            }
        }
    }


    private fun getItemFromHotbar(id: Int): Int {
        for (i in 0..8) {
            val a: ItemStack = mc.player!!.inventory.mainInventory[i]
            val item = a.item
            if (Item.getIdFromItem(item) == id) {
                return i
            }
        }
        return -1
    }
}