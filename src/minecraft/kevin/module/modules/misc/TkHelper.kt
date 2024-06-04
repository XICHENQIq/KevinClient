package kevin.module.modules.misc

import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.module.*
import net.minecraft.inventory.ClickType
import net.minecraft.network.play.client.CPacketClickWindow

class TkHelper : Module(name = "TkHelper",  description = "自动逃逸", category = ModuleCategory.MISC) {
    private val healthMode = ListValue("HealthMode", arrayOf("Custom","Auto"),"Custom")
    private val health = FloatValue("Health",4f,0f,20f)
    private val keepArmor = BooleanValue("KeepArmor",true)

    private var lastHealth = 0f

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (lastHealth != mc.player.health) {
            lastHealth = mc.player.health
            val changeHealth = lastHealth - mc.player.health
            if (changeHealth >= 0) {
                if (mc.player.health <= changeHealth && healthMode.get() == "Auto") {
                    if (keepArmor.get()) {
                        for (i in 5..8) {
                            mc.connection!!.networkManager.sendPacket(CPacketClickWindow(0,i,1,ClickType.QUICK_MOVE,mc.player.inventory.armorInventory[i - 5],0.toShort()))
                        }
                    }
                    mc.player.sendChatMessage("/hub")
                }
            }
        }
        if (mc.player.health <= health.get() && healthMode.get() == "Custom") {
            if (keepArmor.get()) {
                for (i in 5..8) {
                    mc.connection!!.networkManager.sendPacket(CPacketClickWindow(0,i,1,ClickType.QUICK_MOVE,mc.player.inventory.armorInventory[i - 5],0.toShort()))
                }
            }
            mc.player.sendChatMessage("/hub")
        }
    }
}