package kevin.module.modules.combat

import kevin.KevinClient
import kevin.event.*
import kevin.module.*
import kevin.module.modules.movement.Sprint
import kevin.utils.EntityUtils
import kevin.utils.Rotation
import kevin.utils.RotationUtils
import kevin.utils.getDistanceToEntityBox
import kevin.utils.timers.MSTimer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumHand

class KillAura2 : Module(name = "KillAura2", description = "攻击周围实体", category = ModuleCategory.COMBAT) {

    private val cps = IntegerValue("AttackDelay",300,0,1000)
    private val range = FloatValue("Range",3f,1f,8f)
    private val ridingCheck = BooleanValue("RidingCheck",false)
    private val ridingRange = FloatValue("RidingRange",6f,0f,8f)
    private val hurtTime = IntegerValue("HurtTime",4,0,10)
    private val swingValue = BooleanValue("Swing", true)
    private val keepSprintValue = BooleanValue("KeepSprint", true)
    private val attackReduce = BooleanValue("AttackReduce",false)

    private var attackTargets = mutableListOf<Entity>()
    var target: EntityLivingBase? = null
    val msTimer = MSTimer()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {


        if (attackTargets.size == 0 && msTimer.hasTimePassed(cps.get().toLong())){
            attackTargets.clear()
            updateTargets()
            msTimer.reset()
        }

        if (attackTargets.isEmpty())
            target = null

        val entity = attackTargets[0]
        attackTargets.removeAt(0)

        if (attackReduce.get()) {
            if (mc.player.hurtTime == 9) {

                val rotation = RotationUtils.limitAngleChange(
                    Rotation(mc.player.rotationYaw, mc.player.rotationPitch),
                    RotationUtils.toRotation(RotationUtils.getCenter(entity.entityBoundingBox), true),
                    360f
                )
                RotationUtils.setTargetRotation(rotation, 0)
                if (!Sprint.packetSprinting) {
                    mc.connection!!.sendPacket(CPacketEntityAction(
                        mc.player,
                        CPacketEntityAction.Action.START_SPRINTING
                    ))
                    mc.player.isSprinting = true
                    mc.player.serverSprintState = true
                    Sprint.canSprint = true
                    Sprint.sprintTick = 5
                }
                repeat(5) {

                    val duelGod = KevinClient.moduleManager.getModule(DuelGod::class.java) as DuelGod
                    if (duelGod.state) {
                        duelGod.packets.add(CPacketUseEntity(entity))
                        duelGod.packets.add(CPacketAnimation(EnumHand.MAIN_HAND))
                    } else {
                        mc.connection!!.sendPacket(CPacketUseEntity(entity))
                        mc.connection!!.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                    }

                    mc.player.motionX *= 0.6
                    mc.player.motionZ *= 0.6
                }
                return
            }
        }

        val r = if (ridingCheck.get() && mc.player.isRiding) ridingRange.get() else range.get()
        if (mc.player.getDistanceToEntity(entity) > r || (entity as EntityLivingBase).hurtTime >= hurtTime.get())
            return

        val rotation = RotationUtils.limitAngleChange(
            Rotation(mc.player.rotationYaw, mc.player.rotationPitch),
            RotationUtils.toRotation(RotationUtils.getCenter(entity.entityBoundingBox), true),
            180f
        )
        RotationUtils.setTargetRotation(rotation,0)
        attackEntity(entity)
    }
    @EventTarget
    fun onUpdae(event: UpdateEvent) {
        if (attackReduce.get()) {
            if (mc.player.hurtTime == 9) {
                val entity = mc.world.loadedEntityList
                    .filter {
                        EntityUtils.isSelected(it, true) && mc.player.canEntityBeSeen(it) &&
                                mc.player.getDistanceToEntityBox(it) <= 3.0
                    }
                    .minBy { RotationUtils.getRotationDifference(it) } ?: return

                val rotation = RotationUtils.limitAngleChange(
                    Rotation(mc.player.rotationYaw, mc.player.rotationPitch),
                    RotationUtils.toRotation(RotationUtils.getCenter(entity.entityBoundingBox), true),
                    360f
                )
                RotationUtils.setTargetRotation(rotation, 0)
                if (!Sprint.packetSprinting) {
                    mc.connection!!.sendPacket(CPacketEntityAction(
                        mc.player,
                        CPacketEntityAction.Action.START_SPRINTING
                    ))
                    mc.player.isSprinting = true
                    mc.player.serverSprintState = true
                    Sprint.canSprint = true
                    Sprint.sprintTick = 5
                }
                repeat(5) {

                    val duelGod = KevinClient.moduleManager.getModule(DuelGod::class.java) as DuelGod
                    if (duelGod.state) {
                        duelGod.packets.add(CPacketUseEntity(entity))
                        duelGod.packets.add(CPacketAnimation(EnumHand.MAIN_HAND))
                    } else {
                        mc.connection!!.sendPacket(CPacketUseEntity(entity))
                        mc.connection!!.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                    }

                    mc.player.motionX *= 0.6
                    mc.player.motionZ *= 0.6
                }
            }
        }
    }


    private fun updateTargets() {
        for (entity in mc.world.loadedEntityList) {
            val r = if (ridingCheck.get() && mc.player.isRiding) ridingRange.get() else range.get()
            if (EntityUtils.isSelected(entity,true) && mc.player.getDistanceToEntity(entity) <= r) {
                if (!attackTargets.contains(entity)) {
                    attackTargets.add(entity)
                }
            }
        }
    }

    private fun attackEntity(entity: EntityLivingBase) {
        target = entity

        // Call attack event
        EventManager.callEvent(AttackEvent(entity))

        // Attack target
        if(keepSprintValue.get())
            mc.connection!!.sendPacket(CPacketUseEntity(entity))
        else
            mc.playerController.attackEntity(mc.player,entity)


        if(swingValue.get())
            mc.player.swingArm(EnumHand.MAIN_HAND)
        else
            mc.connection!!.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
    }

}