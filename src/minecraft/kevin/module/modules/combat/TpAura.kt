package kevin.module.modules.combat

import kevin.KevinClient
import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.event.Render3DEvent
import kevin.event.UpdateEvent
import kevin.module.BooleanValue
import kevin.module.FloatValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.module.modules.misc.Teams
import kevin.utils.EntityUtils
import kevin.utils.PathUtils
import kevin.utils.RenderUtils
import kevin.utils.timers.MSTimer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.jvm.internal.Intrinsics

class TpAura : Module(name = "TpAura",
        description = "Automatically attacks targets with infinity range.",
        category = ModuleCategory.COMBAT) {
    private var path: ArrayList<Vec3d?>? = null
    private val rangeValue: FloatValue
    private val cpsValue: FloatValue
    private val aac: BooleanValue
    private val hyt: BooleanValue
    private val grim: BooleanValue
    private val attackDelay = MSTimer()
    private var targets: MutableList<EntityLivingBase>
    private val packets: LinkedBlockingQueue<Packet<*>>
    private var disableLogger = false
    private var test: List<Vec3d?>?

    init {
        targets = ArrayList()
        test = ArrayList()
        packets = LinkedBlockingQueue()
        rangeValue = FloatValue("Range", 100.0f, 1.0f, 200.0f)
        cpsValue = FloatValue("CPS", 5.0f, 1.0f, 20.0f)
        aac = BooleanValue("AAC", false)
        hyt = BooleanValue("HYT", true)
        grim = BooleanValue("Grim",false)
    }

    override fun onEnable() {
        disableLogger = false
        attackDelay.reset()
        targets.clear()
    }

    override fun onDisable() {
        disableLogger = false
        attackDelay.reset()
        targets.clear()
        if (hyt.get()) {
            blink()
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (path!!.isNotEmpty()) {
            if (test != null) {
                GL11.glPushMatrix()
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                RenderUtils.glColor(Color.WHITE)
                GL11.glLineWidth(1.0f)
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glBegin(GL11.GL_LINE_STRIP)

                for (pos in test!!) {
                    if (pos != null) {
                        val x = pos.x - mc.renderManager.renderPosX
                        val y = pos.y - mc.renderManager.renderPosY
                        val z = pos.z - mc.renderManager.renderPosZ
                        GL11.glVertex3d(x, y, z)
                    }
                }
                GL11.glEnd()
                GL11.glDisable(GL11.GL_LINE_SMOOTH)
                GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GL11.glPopMatrix()
            }
            if (attackDelay.hasTimePassed(1000L)) {
                test = ArrayList()
                path!!.clear()
            }
        }
    }

    @EventTarget
    fun onUpdate(e: UpdateEvent?) {
        val delayValue = 20.0f / cpsValue.get() * 50.0f
        targets = getTargets()
        if (targets.isEmpty()) {
            attackDelay.reset()
            blink()
            test = ArrayList()
        }
        if (attackDelay.hasTimePassed(delayValue.toLong()) && targets.size > 0) {
            val topFrom = Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ)
            val to = Vec3d(targets[0].posX, targets[0].posY, targets[0].posZ)
            path = PathUtils.computePath(topFrom, to)
            test = path
            for (pathElm in path!!) {
                if (grim.get()){
                    mc.timer.renderPartialTicks -= 1f
                }
                mc.connection!!.networkManager.sendPacket(
                        CPacketPlayer.Position(pathElm!!.x, pathElm.y, pathElm.z, true)
                )
            }
            if (aac.get() && !hyt.get()) {
                if (grim.get()){
                    mc.timer.renderPartialTicks -= 1f
                }
                mc.connection!!.networkManager.sendPacket(
                        CPacketPlayer.Position(targets[0].posX, targets[0].posY, targets[0].posZ, true)
                )
            }
            if (hyt.get()) {
                blink()
            }
            mc.connection!!.networkManager.sendPacket(
                    CPacketUseEntity(targets[0],CPacketUseEntity.Action.ATTACK,EnumHand.MAIN_HAND)
            )
            mc.player!!.swingArm(EnumHand.MAIN_HAND)
            Collections.reverse(path)
            for (pathElm in path!!) {
                if (grim.get()){
                    mc.timer.renderPartialTicks -= 1f
                }
                mc.connection!!.networkManager.sendPacket(
                    CPacketPlayer.Position(
                        pathElm!!.x,
                        pathElm.y,
                        pathElm.z,
                        true
                    )
                )
            }
            if (hyt.get()) {
                blink()
            }
            attackDelay.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (hyt.get()) {
            Intrinsics.checkParameterIsNotNull(event, "event")
            val packet = event.packet
            if (mc.player == null || disableLogger) {
                return
            }
            if (packet is CPacketPlayer) {
                event.cancelEvent()
            }
            if (packet is CPacketPlayer.Position || packet is CPacketPlayer.PositionRotation || packet is CPacketPlayerTryUseItemOnBlock || packet is CPacketAnimation || packet is CPacketEntityAction || packet is CPacketUseEntity
            ) {
                event.cancelEvent()
                packets.add(packet)
            }
        }
    }

    private fun isEnemy(entity: Entity?): Boolean {
        if (entity !is EntityLivingBase || entity == null || !EntityUtils.targetDeath && !isAlive(entity) || entity == mc.player) {
            return false
        }
        if (!EntityUtils.targetInvisible && entity.isInvisible) {
            return false
        }
        if (!EntityUtils.targetPlayer || entity !is EntityPlayer) {
            return EntityUtils.targetMobs && entity is EntityMob || EntityUtils.targetAnimals && entity is EntityAnimal
        }
        val player = entity
        if (player.isSpectator) {
            return false
        }
        val teams = KevinClient.moduleManager.getModule(Teams::class.java) as Teams
        return !teams.state || !teams.isInYourTeam(entity)
    }

    private fun isAlive(entity: EntityLivingBase): Boolean {
        return entity.isEntityAlive && entity.height > 0.0f
    }

    private fun getTargets(): MutableList<EntityLivingBase> {
        val targets: MutableList<EntityLivingBase> = ArrayList()
        for (entity in mc.world.loadedEntityList) {
            if (entity.getDistanceToEntity(mc.player) <= rangeValue.get()) {
                if (!isEnemy(entity)) {
                    continue
                }
                targets.add(entity as EntityLivingBase)
            }
        }
        targets.sortWith(Comparator { o1: EntityLivingBase, o2: EntityLivingBase ->
            (o1.getDistanceToEntity(
                mc.player!!
            ) * 1000.0f - o2.getDistanceToEntity(mc.player) * 1000.0f).toInt()
        })
        return targets
    }

    private fun blink() {
        try {
            disableLogger = true
            while (!packets.isEmpty()) {
                val networkManager = mc.connection!!.networkManager
                val take = packets.take()
                Intrinsics.checkExpressionValueIsNotNull(take, "packets.take()")
                networkManager.sendPacket(take)
            }
            disableLogger = false
        } catch (e: Exception) {
            e.printStackTrace()
            disableLogger = false
        }
    }

    override val tag: String
        get() = rangeValue.get().toString()
}
