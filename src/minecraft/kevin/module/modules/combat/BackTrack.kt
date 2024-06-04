package kevin.module.modules.combat


import com.yumegod.obfuscation.Native
import kevin.event.*
import kevin.module.*
import kevin.utils.RenderUtils
import kevin.utils.getDistanceToEntityBox
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EntityTracker
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.server.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread
import kotlin.math.sqrt

// 我趣 LinkedBlockingQueue是线程安全 不用加锁。。
// 还得加锁 哈哈 cnm
// 延迟所有入站数据包 然后根据packettime来

@Native
class BackTrack : Module(name = "BackTrack", description = "changed from XiChenQi", category = ModuleCategory.COMBAT) {
    private val packetTime = IntegerValue("PacketTime",200,0,2000)
    private val clientMinRange = FloatValue("ClientMinRange", 3f, 2f, 8f)
    private val clientMaxRange = FloatValue("ClientMaxRange", 4f, 2f, 8f)
    private val serverRange = FloatValue("ServerMaxRange", 8f, 2f, 15f)
    private val renderBox = BooleanValue("Box", true)
    private val renderOutline = BooleanValue("Outline", true)

    // 队列保存数据包和接收到它的时间
    private val packets = LinkedBlockingQueue<Pair<Packet<INetHandlerPlayClient>, Long>>()

    private var target: Entity? = null
    private val targetLock = Any()

    private var canCalcRealPos = false // 这些底下的变量只在主线程调用 不需要锁
    private var realPos = Vec3d(0.0, 0.0, 0.0)

    private var gotVelo = false

    override fun onEnable() {
        synchronized (targetLock) {
            packets.clear()
            target = null
        }
        canCalcRealPos = false
        realPos = Vec3d(0.0, 0.0, 0.0)
        gotVelo = false
    }

    override fun onDisable() {
        synchronized (targetLock) {
            process(mc.connection!!)
            target = null
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val targetEntity = event.targetEntity ?: return

        // 希望没有啥比触发这个
        if (targetEntity == mc.player) return

        if (targetEntity is EntityLivingBase) {
            synchronized (targetLock) {
                val lastTarget = target
                if (lastTarget == null || lastTarget != targetEntity) {
                    target = targetEntity
                    // 我们需要在入站数据包清空后追踪那个实体的位置...
                    canCalcRealPos = false
                
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is SPacketChat
                || packet is SPacketUpdateHealth
                || packet is SPacketHeldItemChange
                || (packet is SPacketAnimation && mc.world != null && packet.entityID != mc.player?.entityId ?: -1 && mc.world!!.getEntityByID(packet.entityID) != null)
                || (packet is SPacketEntityStatus && mc.world != null && packet.getEntity(mc.world!!) != mc.player && (packet.opCode == 33.toByte() || packet.opCode == 36.toByte() || packet.opCode == 37.toByte()))
        ) {
            
        } else if (packet is SPacketDisconnect || packet is SPacketJoinGame || packet is SPacketRespawn) {
            synchronized (targetLock) {
                if (target == null)
                    return
                process(mc.connection!!)
            }
        } else if (packet.javaClass.name.startsWith("net.minecraft.network.play.server.S")) {
            synchronized (targetLock) {
                if (target == null)
                    return
                packets.add(Pair(packet as Packet<INetHandlerPlayClient>, System.currentTimeMillis()))
            }
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onPacketProcess(event: PacketProcessEvent) {
        val packet = event.packet

        if ((packet is SPacketEntityVelocity && mc.player != null && mc.player!!.entityId == packet.entityID)
                || (packet is SPacketExplosion && (packet.motionX != 0f || packet.motionY != 0f || packet.motionZ != 0f))
        ) {
            gotVelo = true
        }
    }

    @EventTarget
    fun onTick(event: TickEvent?) {
        val connection = mc.connection!!

        val targetEntity = target
        if (targetEntity != null) {
            // 如果玩家处理过数据包且没有计算真实坐标，就计算真实坐标
            if (canCalcRealPos) {
                calcRealPos(targetEntity)
            }
            // 如果玩家距离目标很远，或者目标不在世界上，就释放所有入站数据包
            if (mc.world!!.getEntityByID(targetEntity.entityId) != targetEntity || mc.player.getDistanceToEntityBox(targetEntity) >= clientMaxRange.get() || (canCalcRealPos && getDistanceToRealPos(mc.player) >= serverRange.get())) {
                synchronized (targetLock) {
                    process(connection, false)
                    target = null
                }
                realPos = Vec3d(0.0, 0.0, 0.0)
            }
            // 或者玩家距离目标的真实距离很近 就释放所有入站数据包
            else if (getDistanceToRealPos(mc.player) <= clientMinRange.get()) {
                synchronized (targetLock) {
                    process(connection, false)
                }
                realPos = Vec3d(0.0, 0.0, 0.0)
            }
            // 或者玩家接受到过kb 且realpos比fakepos到玩家的距离短 就释放所有入站数据包
            else if (gotVelo && getDistanceToRealPos(mc.player) < mc.player.getDistanceToEntity(targetEntity)) {
                synchronized (targetLock) {
                    process(connection, false)
                }
                realPos = Vec3d(0.0, 0.0, 0.0)
            }
        }
        // 释放即将超时的入站数据包
        synchronized (targetLock) {
            process(connection, false, System.currentTimeMillis() - packetTime.get())
        }
        gotVelo = false
    }

    @EventTarget
    fun onRender3d(event: Render3DEvent) {
        if (canCalcRealPos) {
            // 客户端已经处理过数据包，渲染它们
            val renderEntity = target ?: return

            // 如果玩家处理了数据包，在这里计算真实坐标
            calcRealPos(renderEntity)

            // 获取正在渲染的实体的碰撞箱，然后减去偏移量，渲染它们
            val renderManager = mc.renderManager
            val oldBox = renderEntity.entityBoundingBox.expand(0.05, 0.15, 0.05).expand(-0.05, 0.0, -0.05)
            val newBox = oldBox.offset(realPos.x - renderEntity.posX - renderManager.renderPosX, realPos.y - renderEntity.posY - renderManager.renderPosY, realPos.z - renderEntity.posZ - renderManager.renderPosZ)
            // 没人帮我修渲染 还得我自己找 气死我了
            this.drawAxisAlignedBB(newBox, Color.WHITE, renderOutline.get(), renderBox.get())
        } else {
            // 客户端没有处理过数据包，等待客户端下一gameloop处理
            canCalcRealPos = true
        }
    }

    private fun process(connection: INetHandlerPlayClient, instant: Boolean = false, time: Long? = null) {
        // instant是立即处理数据包，反之是加入队列下个tick处理
        // 如果是在netty线程上调用，那instant是否的效果都一样
        // 理论上 为了合法 在主线程上应该始终使用非instant

        // time参数是终止的时间，如果是null就不终止

        val canBreak = time != null
        val timeNullSafe: Long = time ?: 0 // cnm kotlin

        if (instant) {
            var pair = packets.peek()
            while (pair != null) {
                if (canBreak && pair.second > timeNullSafe) break
                packets.poll()
                val packet = pair.first
                try {
                    packet.processPacket(connection)
                } catch (e: Exception) {} // 数据包处理过程中 或者非主线程执行数据包处理这个操作可能会抛出异常，不用管
                pair = packets.peek()
            }
        } else {
            thread {
                var pair = packets.peek()
                while (pair != null) {
                    if (canBreak && pair.second > timeNullSafe) break
                    packets.poll()
                    val packet = pair.first
                    try {
                        packet.processPacket(connection)
                    } catch (e: Exception) {}
                    pair = packets.peek()
                }
            }.join()
        }
    }

    private fun getDistanceToRealPos(player: Entity): Double {
        // 返回玩家距离实体的真实距离
        val xD = player.posX - realPos.x
        val yD = player.posY - realPos.y
        val zD = player.posZ - realPos.z
        return sqrt(xD * xD + yD * yD + zD * zD);
    }

    private fun calcRealPos(targetEntity: Entity) {
        // 计算这个实体的真实坐标并保存到realPos
        // 客户端调用这个方法的时候，未保存的入站数据包应该是已经处理干净了的
        val world = mc.world!!
        val targetId = targetEntity.entityId
        var oldX = targetEntity.serverPosX
        var oldY = targetEntity.serverPosY
        var oldZ = targetEntity.serverPosZ

        val packetsIter = packets.iterator()
        while (packetsIter.hasNext()) {
            val packet = packetsIter.next().first
            when (packet) {
                is SPacketEntity -> {
                    val packetEntity = packet.getEntity(world)
                    if (packetEntity != null && packetEntity.entityId == targetId) {
                        oldX += packet.x.toLong()
                        oldY += packet.y.toLong()
                        oldZ += packet.z.toLong()
                    }
                }
                is SPacketEntityTeleport -> {
                    if (packet.entityId == targetId) {
                        oldX = EntityTracker.getPositionLong(packet.x)
                        oldY = EntityTracker.getPositionLong(packet.y)
                        oldZ = EntityTracker.getPositionLong(packet.z)
                    }
                }
            }
        }
        this.realPos = Vec3d(oldX.toDouble() / 4096.0, oldY.toDouble() / 4096.0, oldZ.toDouble() / 4096.0)
    }

    // from FDPClient
    private fun drawAxisAlignedBB(aabb: AxisAlignedBB, color: Color, outline: Boolean = false, box: Boolean = true, outlineWidth: Float = 2f) {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glLineWidth(outlineWidth)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        RenderUtils.glColor(color)

        if (outline) {
            GL11.glLineWidth(outlineWidth)
            RenderUtils.enableGlCap(GL11.GL_LINE_SMOOTH)
            RenderUtils.glColor(color.red, color.green, color.blue, 95)
            RenderUtils.drawSelectionBoundingBox(aabb)
        }

        if (box) {
            RenderUtils.glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
            RenderUtils.drawFilledBox(aabb)
        }

        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }
}

