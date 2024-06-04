package net.minecraft.network;

import kevin.KevinClient;
import kevin.event.EventManager;
import kevin.event.PacketProcessEvent;
import net.minecraft.util.IThreadListener;

public class PacketThreadUtil
{
    public static <T extends INetHandler> void checkThreadAndEnqueue(final Packet<T> packetIn, final T processor, IThreadListener scheduler) throws ThreadQuickExitException
    {
        if (!scheduler.isCallingFromMinecraftThread())
        {
            scheduler.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    final PacketProcessEvent event = new PacketProcessEvent(packetIn);
                    EventManager.INSTANCE.callEvent(event);

                    if (!event.isCancelled()){
                        packetIn.processPacket(processor);
                    }
                }
            });
            throw ThreadQuickExitException.INSTANCE;
        }
    }
}
