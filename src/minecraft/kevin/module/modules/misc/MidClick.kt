/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package kevin.module.modules.misc

import kevin.KevinClient
import kevin.event.EventTarget
import kevin.event.Render2DEvent
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.ChatUtils
import kevin.utils.ColorUtils.stripColor
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.input.Mouse

class MidClick : Module(name = "MidClick", description = "允许你通过按下中键添加好友", category = ModuleCategory.MISC) {
    private var wasDown = false

    @EventTarget
    fun onRender(event: Render2DEvent?) {
        if (mc.currentScreen != null)
            return

        if (!wasDown && Mouse.isButtonDown(2)) {
            val entity = mc.objectMouseOver!!.entityHit

            if (entity is EntityPlayer) {
                val playerName = stripColor(entity.name)

                if (!KevinClient.isFriend(playerName!!)) {
                    KevinClient.friends.add(playerName)
                    ChatUtils.message("§a§l$playerName§c was added to your friends.")
                } else {
                    KevinClient.friends.remove(playerName)
                    ChatUtils.message("§a§l$playerName§c was removed from your friends.")
                }

            } else
                ChatUtils.message("§c§lError: §aYou need to select a player.")
        }
        wasDown = Mouse.isButtonDown(2)
    }
}