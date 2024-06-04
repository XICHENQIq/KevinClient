/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package kevin.utils.skid

import kevin.utils.Rotation
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d


val Entity.rotation
    get() = Rotation(rotationYaw, rotationPitch)
val Entity.eyes: Vec3d
    get() = getPositionEyes(1f)


fun Entity.rayTraceCustom(blockReachDistance: Double, yaw: Float, pitch: Float): RayTraceResult? {
    val vec3: Vec3d = this.getPositionEyes(1.0f)
    val vec31: Vec3d = getVectorForRotation(yaw, pitch)
    val vec32: Vec3d = vec3.addVector(
        vec31.x * blockReachDistance,
        vec31.y * blockReachDistance,
        vec31.z * blockReachDistance
    )
    return this.world.rayTraceBlocks(vec3, vec32, false, false, true)
}

fun getVectorForRotation(pitch: Float, yaw: Float): Vec3d {
    val f: Float = MathHelper.cos(-yaw * 0.017453292f - Math.PI.toFloat())
    val f1: Float = MathHelper.sin(-yaw * 0.017453292f - Math.PI.toFloat())
    val f2: Float = -MathHelper.cos(-pitch * 0.017453292f)
    val f3: Float = MathHelper.sin(-pitch * 0.017453292f)
    return Vec3d((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
}

fun RayTraceResult.MovingObjectPosition(entityHitIn: Entity, hitVecIn: Vec3d) : RayTraceResult {
    this.typeOfHit = RayTraceResult.Type.ENTITY
    this.entityHit = entityHitIn
    this.hitVec = hitVecIn!!
    return this
}

