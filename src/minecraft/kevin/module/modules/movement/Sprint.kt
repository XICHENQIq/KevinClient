package kevin.module.modules.movement

import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.event.UpdateEvent
import kevin.module.BooleanValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.MovementUtils
import kevin.utils.MovementUtils.isMoving
import kevin.utils.Rotation
import kevin.utils.RotationUtils
import net.minecraft.init.MobEffects
import net.minecraft.network.play.client.CPacketEntityAction
import org.lwjgl.input.Keyboard

object Sprint : Module("Sprint","Automatically sprints all the time.", Keyboard.KEY_NONE,ModuleCategory.MOVEMENT) {

    val rotationCheck = BooleanValue("RotationCheck",true)
    private val antiBadF = BooleanValue("AntiBadPacketF",false)

    var canSprint = true
    var packetSprinting = false
    var sprintTick = 0


    @EventTarget
    fun onUpdate(event: UpdateEvent?) {

        if (sprintTick > 0) {
            mc.player.isSprinting = getSprint()
            sprintTick --
            return
        }

        if (sprintTick == 0) {
            if (!canSprint) {
                mc.player.isSprinting = false
                if (RotationUtils.targetRotation == null) {
                    canSprint = true
                    sprintTick = 5
                }
                return
            }

            sprintTick = 5
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is CPacketEntityAction) {
            if (antiBadF.get()) {
                if (packetSprinting && packet.action == CPacketEntityAction.Action.START_SPRINTING) {
                    event.cancelEvent()
                }
                if (!packetSprinting && packet.action == CPacketEntityAction.Action.STOP_SPRINTING) {
                    event.cancelEvent()
                }
            }
            if (packet.action == CPacketEntityAction.Action.START_SPRINTING) {
                packetSprinting = true
            }
            if (packet.action == CPacketEntityAction.Action.STOP_SPRINTING) {
                packetSprinting = false
            }
        }
    }
    fun getSprint(): Boolean {
        return isMoving && !mc.player.isSneaking && (mc.player.foodStats.foodLevel > 6.0f || mc.player.capabilities.allowFlying) && mc.player.movementInput.moveForward >= 0.8f
    }
}