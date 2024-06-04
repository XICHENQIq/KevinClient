package kevin.utils

import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import java.util.concurrent.LinkedBlockingQueue

object PacketUtils : Mc() {
    private val packetList = HashSet<Packet<*>>()

    val packets = LinkedBlockingQueue<Packet<INetHandlerPlayServer>>()

    fun sendPacketNoEvent(packet: Packet<*>){
        packetList.add(packet)
        mc.connection!!.sendPacket(packet)
    }
    @JvmStatic
    fun needReceiveEvent(packetIn: Packet<*>) = !packetList.contains(packetIn)


    fun packetAll() {
        try {
            while (!packets.isEmpty()) {
                val packet = packets.take()
                sendPacketNoEvent(packet as Packet<INetHandlerPlayServer>)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}