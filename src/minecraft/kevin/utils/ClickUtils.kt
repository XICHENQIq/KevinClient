package kevin.utils

import kevin.utils.Mc.Companion.mc
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.network.play.client.CPacketClickWindow

object ClickUtils {
    fun windowClick(
        windowID: Int,
        slotID: Int,
        bottom: Int,
        clickType: ClickType,
        player: EntityPlayer
    ) {
        val short1 = player.openContainer.getNextTransactionID(player.inventory)
        val itemstack = player.openContainer.slotClick(
            slotID,
            bottom,
            clickType,
            player
        )
        mc.connection!!.sendPacket(
            CPacketClickWindow(
                windowID,
                slotID,
                bottom,
                clickType,
                itemstack,
                short1
            )
        )
    }
}