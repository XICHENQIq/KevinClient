package kevin.module.modules.movement

import com.yumegod.obfuscation.Native
import kevin.event.EventState
import kevin.event.EventTarget
import kevin.event.MotionEvent
import kevin.module.BooleanValue
import kevin.module.FloatValue
import kevin.module.Module
import kevin.module.ModuleCategory
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.*
import kotlin.math.*

/* Thanks to
* kid
* XiChenQi
*/

@Native
class EntitySpeed : Module(name = "EntitySpeed", description = "omg grimspeed - 7087z", category = ModuleCategory.MOVEMENT) {
    private val strafe = BooleanValue("Strafe", true)
    private val strafeStrength = FloatValue("StrafeStrength", 8f, 0.01f, 8f)
    private val speed = BooleanValue("Speed", true)
    private val speedStrength = FloatValue("SpeedStrength", 8f, 0.01f, 8f)
    private val onlyCountOne = BooleanValue("OnlyCountOne", true)

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState != EventState.POST)
            return
        val mc2 = mc
        val thePlayer = mc.player
        val playerBox = mc2.player.entityBoundingBox.grow(1.0)
        var c = 0
        if (onlyCountOne.get()) {
            for (entity in mc2.world.loadedEntityList) {
                if (entity.entityId == -1337)
                    return
                if ((entity is EntityLivingBase || entity is EntityBoat || entity is EntityMinecart) && entity !is EntityArmorStand && entity.entityId != mc2.player.entityId && playerBox.intersects(entity.entityBoundingBox)) {
                    c = 1
                    break
                }
            }
        } else {
            for (entity in mc2.world.loadedEntityList) {
                if (entity.entityId == -1337)
                    return
                if ((entity is EntityLivingBase || entity is EntityBoat || entity is EntityMinecart) && entity !is EntityArmorStand && entity.entityId != mc2.player.entityId && playerBox.intersects(entity.entityBoundingBox)) {
                    c++ // :D // :)
                }
            }
        }
        if (c > 0) {
            val strafeOffset = c * 0.01 * max(0.01, strafeStrength.get().toDouble())
            val speedOffset = c * 0.01 * max(0.01, speedStrength.get().toDouble())

            if (thePlayer.movementInput.moveForward == 0F && thePlayer.movementInput.moveStrafe == 0F) {
                if (strafe.get()) {
                    when {
                        thePlayer.motionX > strafeOffset -> thePlayer.motionX -= strafeOffset
                        thePlayer.motionX < -strafeOffset -> thePlayer.motionX += strafeOffset
                        else -> thePlayer.motionX = 0.0
                    }
                    when {
                        thePlayer.motionZ > strafeOffset -> thePlayer.motionZ -= strafeOffset
                        thePlayer.motionZ < -strafeOffset -> thePlayer.motionZ += strafeOffset
                        else -> thePlayer.motionZ = 0.0
                    }
                }
                return
            }
            val yaw = getMoveYaw().toDouble()

            var mx = -sin(Math.toRadians(yaw))
            when {
                mx < 0.0 -> {
                    when {
                        thePlayer.motionX > 0 -> {
                            if (thePlayer.motionX > strafeOffset) {
                                if (strafe.get())
                                    thePlayer.motionX -= strafeOffset
                            } else {
                                if (speed.get() && (0 > thePlayer.motionX + mx * speedOffset))
                                    thePlayer.motionX += mx * speedOffset
                                else if (strafe.get())
                                    thePlayer.motionX = 0.0
                            }
                        }
                        else -> {
                            if (speed.get())
                                thePlayer.motionX += mx * speedOffset
                        }
                    }
                }
                mx > 0.0 -> {
                    when {
                        thePlayer.motionX < 0 -> {
                            if (thePlayer.motionX < -strafeOffset) {
                                if (strafe.get())
                                    thePlayer.motionX += strafeOffset
                            } else {
                                if (speed.get() && (0 < thePlayer.motionX + mx * speedOffset))
                                    thePlayer.motionX += mx * speedOffset
                                else if (strafe.get())
                                    thePlayer.motionX = 0.0
                            }
                        }
                        else -> {
                            if (speed.get())
                                thePlayer.motionX += mx * speedOffset
                        }
                    }
                }
            }

            var mz = cos(Math.toRadians(yaw))
            when {
                mz < 0.0 -> {
                    when {
                        thePlayer.motionZ > 0 -> {
                            if (thePlayer.motionZ > strafeOffset) {
                                if (strafe.get())
                                    thePlayer.motionZ -= strafeOffset
                            } else {
                                if (speed.get() && (0 > thePlayer.motionZ + mz * speedOffset))
                                    thePlayer.motionZ += mz * speedOffset
                                else if (strafe.get())
                                    thePlayer.motionZ = 0.0
                            }
                        }
                        else -> {
                            if (speed.get())
                                thePlayer.motionZ += mz * speedOffset
                        }
                    }
                }
                mz > 0.0 -> {
                    when {
                        thePlayer.motionZ < 0 -> {
                            if (thePlayer.motionZ < -strafeOffset) {
                                if (strafe.get())
                                    thePlayer.motionZ += strafeOffset
                            } else {
                                if (speed.get() && (0 < thePlayer.motionZ + mz * speedOffset))
                                    thePlayer.motionZ += mz * speedOffset
                                else if (strafe.get())
                                    thePlayer.motionZ = 0.0
                            }
                        }
                        else -> {
                            if (speed.get())
                                thePlayer.motionZ += mz * speedOffset
                        }
                    }
                }
            }

        }
    }

    private fun getMoveYaw(): Float {
        val thePlayer = mc.player
        var moveYaw = thePlayer.rotationYaw
        if (thePlayer.moveForward != 0F && thePlayer.moveStrafing == 0F) {
            moveYaw += if (thePlayer.moveForward > 0) 0 else 180
        } else if (thePlayer.moveForward != 0F && thePlayer.moveStrafing != 0F) {
            if (thePlayer.moveForward > 0) {
                moveYaw += if (thePlayer.moveStrafing > 0) -45 else 45
            } else {
                moveYaw -= if (thePlayer.moveStrafing > 0) -45 else 45
            }
            moveYaw += if (thePlayer.moveForward > 0) 0 else 180
        } else if (thePlayer.moveStrafing != 0F && thePlayer.moveForward == 0F) {
            moveYaw += if (thePlayer.moveStrafing > 0) -90 else 90
        }
        return moveYaw
    }
}