/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package kevin.module.spoof

import io.netty.buffer.Unpooled
import kevin.KevinClient
import kevin.event.EventTarget
import kevin.event.Listenable
import kevin.event.PacketEvent
import kevin.module.modules.misc.ClientSpoof
import kevin.utils.Mc
import kevin.utils.PacketUtils.sendPacketNoEvent
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import java.util.*

class ClientSpoofHandler : Mc(), Listenable {
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val clientSpoof = KevinClient.moduleManager.getModule(ClientSpoof::class.java) as ClientSpoof

        if (!mc.isIntegratedServerRunning) {
            if (packet is CPacketCustomPayload) {
                if ((event.packet as CPacketCustomPayload).channelName.equals("MC|Brand", ignoreCase = true)) {
                    if (Objects.requireNonNull(clientSpoof).modeValue.get() == "Vanilla") sendPacketNoEvent(CPacketCustomPayload("MC|Brand", PacketBuffer(Unpooled.buffer()).writeString("vanilla")))
                    if (Objects.requireNonNull(clientSpoof).modeValue.get() == "Fabric") sendPacketNoEvent(CPacketCustomPayload("MC|Brand", PacketBuffer(Unpooled.buffer()).writeString("fabric")))
                    if (Objects.requireNonNull(clientSpoof).modeValue.get() == "LabyMod") {
                        Objects.requireNonNull(mc.connection)?.sendPacket(CPacketCustomPayload("labymod3:main", this.info))
                        mc.connection!!.sendPacket(CPacketCustomPayload("LMC", this.info))
                    }
                    if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning && clientSpoof.modeValue.get() == "Custom") {
                        try {
                            if (packet is CPacketCustomPayload) {
                                packet.data = (PacketBuffer(Unpooled.buffer()).writeString(clientSpoof.CustomClient.get()))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (Objects.requireNonNull(clientSpoof).modeValue.get() == "CheatBreaker") sendPacketNoEvent(CPacketCustomPayload("MC|Brand", PacketBuffer(Unpooled.buffer()).writeString("CB")))
                    if (Objects.requireNonNull(clientSpoof).modeValue.get() == "PvPLounge") sendPacketNoEvent(CPacketCustomPayload("MC|Brand", PacketBuffer(Unpooled.buffer()).writeString("PLC18")))
                    if (Objects.requireNonNull(clientSpoof).modeValue.get() == "Lunar") sendPacketNoEvent(CPacketCustomPayload("MC|Brand", PacketBuffer(Unpooled.buffer()).writeString("lunarclient:v2.12.3-2351")))
                }
                event.cancelEvent()
            }
        }
    }

    private val info: PacketBuffer
        get() = PacketBuffer(Unpooled.buffer())
                .writeString("INFO")
                .writeString("""{  
   "version": "3.9.25",
   "ccp": {  
      "enabled": true,
      "version": 2
   },
   "shadow":{  
      "enabled": true,
      "version": 1
   },
   "addons": [  
      {  
         "uuid": "null",
         "name": "null"
      }
   ],
   "mods": [
      {  
         "hash":"sha256:null",
         "name":"null.jar"
      }
   ]
}""")

    override fun handleEvents(): Boolean {
        return true
    }

    companion object {
        const val enabled: Boolean = true
    }
}