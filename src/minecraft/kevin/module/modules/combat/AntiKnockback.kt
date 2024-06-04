package kevin.module.modules.combat

import com.viaversion.viaversion.api.type.types.minecraft.ItemType
import javafx.scene.effect.MotionBlur
import kevin.KevinClient
import kevin.event.*
import kevin.module.*
import kevin.utils.*
import kevin.utils.timers.MSTimer
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockBush
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class AntiKnockback : Module("AntiKnockback","Allows you to modify the amount of knockback you take.", category = ModuleCategory.COMBAT) {
    private val horizontalValue = FloatValue("Horizontal", 0F, 0F, 1F)
    private val verticalValue = FloatValue("Vertical", 0F, 0F, 1F)
    private val modeValue = ListValue("Mode", arrayOf("GrimNew","Simple", "AAC", "AACPush", "AACZero", "AACv4",
        "Reverse", "SmoothReverse", "Jump", "Glitch", "AAC5Packet"), "Simple")

    // Reverse
    private val reverseStrengthValue = FloatValue("ReverseStrength", 1F, 0.1F, 1F)
    private val reverse2StrengthValue = FloatValue("SmoothReverseStrength", 0.05F, 0.02F, 0.1F)

    // AAC Push
    private val aacPushXZReducerValue = FloatValue("AACPushXZReducer", 2F, 1F, 3F)
    private val aacPushYReducerValue = BooleanValue("AACPushYReducer", true)

    // AAc v4
    private val aacv4MotionReducerValue = FloatValue("AACv4MotionReducer", 0.62F,0F,1F)

    private val cancelExplosionPacket = BooleanValue("CancelExplosionPacket",false)
    private val noExplosionPush = BooleanValue("NoExplosionPush",true)
    private val explosionCheck = BooleanValue("ExplosionCheck",true)

    private val fireCheck = BooleanValue("FireCheck",true)

    private var velocityTimer = MSTimer()
    private var velocityInput = false

    private var explosion = false

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    // grimNew
    var needC09Back = false

    override val tag: String
        get() = if (modeValue.get() == "Simple") "H:${horizontalValue.get()*100}% V:${verticalValue.get()*100}%" else modeValue.get()

    override fun onDisable() {
        mc.player?.speedInAir = 0.02F
        needC09Back = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.player ?: return

        if (thePlayer.isInWater || thePlayer.isInLava || thePlayer.isInWeb)
            return

        when (modeValue.get().toLowerCase()) {
            "jump" -> if (thePlayer.hurtTime > 0 && thePlayer.onGround) {
                thePlayer.motionY = 0.42

                val yaw = thePlayer.rotationYaw * 0.017453292F

                thePlayer.motionX -= sin(yaw) * 0.2
                thePlayer.motionZ += cos(yaw) * 0.2
            }

            "glitch" -> {
                thePlayer.noClip = velocityInput

                if (thePlayer.hurtTime == 7)
                    thePlayer.motionY = 0.4

                velocityInput = false
            }

            "reverse" -> {
                if (!velocityInput)
                    return

                if (!thePlayer.onGround) {
                    MovementUtils.strafe(MovementUtils.speed * reverseStrengthValue.get())
                } else if (velocityTimer.hasTimePassed(80L))
                    velocityInput = false
            }

            "smoothreverse" -> {
                if (!velocityInput) {
                    thePlayer.speedInAir = 0.02F
                    return
                }

                if (thePlayer.hurtTime > 0)
                    reverseHurt = true

                if (!thePlayer.onGround) {
                    if (reverseHurt)
                        thePlayer.speedInAir = reverse2StrengthValue.get()
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                    reverseHurt = false
                }
            }

            "aac" -> if (velocityInput && velocityTimer.hasTimePassed(80L)) {
                thePlayer.motionX *= horizontalValue.get()
                thePlayer.motionZ *= horizontalValue.get()
                //mc.player.motionY *= verticalValue.get() ?
                velocityInput = false
            }

            "aacv4" -> {
                if (thePlayer.hurtTime>0 && !thePlayer.onGround){
                    val reduce=aacv4MotionReducerValue.get();
                    thePlayer.motionX *= reduce
                    thePlayer.motionZ *= reduce
                }
            }

            "aacpush" -> {
                if (jump) {
                    if (thePlayer.onGround)
                        jump = false
                } else {
                    // Strafe
                    if (thePlayer.hurtTime > 0 && thePlayer.motionX != 0.0 && thePlayer.motionZ != 0.0)
                        thePlayer.onGround = true

                    // Reduce Y
                    if (thePlayer.hurtResistantTime > 0 && aacPushYReducerValue.get()
                        && !KevinClient.moduleManager.getModule("Speed")!!.state)
                        thePlayer.motionY -= 0.014999993
                }

                // Reduce XZ
                if (thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducerValue.get()

                    thePlayer.motionX /= reduce
                    thePlayer.motionZ /= reduce
                }
            }

            "aaczero" -> if (thePlayer.hurtTime > 0) {
                if (!velocityInput || thePlayer.onGround || thePlayer.fallDistance > 2F)
                    return

                thePlayer.motionY -= 1.0
                thePlayer.isAirBorne = true
                thePlayer.onGround = true
            } else
                velocityInput = false
        }
    }
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (modeValue.get() == "GrimNew") {
            val slot = getWebFromHotbar()
            if (slot == -1)
                return

            val blockPos = BlockPos(mc.player)

            if (mc.player!!.hurtTime in 8..9) {
                if (event.eventState == EventState.PRE) {
                    if (BlockUtils.getBlock(blockPos.down()) !is BlockAir) {
                        if (mc.player.inventory.currentItem != slot) {
                            PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(slot))
                        }
                        RotationUtils.setTargetRotation(Rotation(mc.player.rotationYaw,90f),10)

                        PacketUtils.sendPacketNoEvent(CPacketHeldItemChange((slot + 1) % 9))
                        PacketUtils.sendPacketNoEvent(CPacketConfirmTransaction(0,0,true))

                        PacketUtils.sendPacketNoEvent(CPacketPlayerTryUseItemOnBlock(blockPos.down(),EnumFacing.UP,EnumHand.MAIN_HAND,(mc.player.posX - blockPos.down().x).toFloat(),1f,(mc.player.posZ - blockPos.down().z).toFloat()))
                        PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(slot))

                        mc.player.motionX = 0.0
                        mc.player.motionY = 0.0
                        mc.player.motionZ = 0.0
                        if (mc.player.inventory.currentItem != slot) {
                            needC09Back = true
                        }
                    } else if (BlockUtils.getBlock(blockPos.down().down()) !is BlockAir) {
                        if (mc.player.inventory.currentItem != slot) {
                            PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(slot))
                        }
                        RotationUtils.setTargetRotation(Rotation(mc.player.rotationYaw,90f))

                        PacketUtils.sendPacketNoEvent(CPacketHeldItemChange((slot + 1) % 9))
                        PacketUtils.sendPacketNoEvent(CPacketConfirmTransaction(0,0,true))
                        PacketUtils.sendPacketNoEvent(CPacketPlayerTryUseItemOnBlock(blockPos.down().down(),EnumFacing.UP,EnumHand.MAIN_HAND,(mc.player.posX - blockPos.down().down().x).toFloat(),1f,(mc.player.posZ - blockPos.down().down().z).toFloat()))
                        PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(slot))

                        PacketUtils.sendPacketNoEvent(CPacketHeldItemChange((slot + 1) % 9))
                        PacketUtils.sendPacketNoEvent(CPacketConfirmTransaction(0,0,true))
                        PacketUtils.sendPacketNoEvent(CPacketPlayerTryUseItemOnBlock(blockPos.down(),EnumFacing.UP,EnumHand.MAIN_HAND,(mc.player.posX - blockPos.down().x).toFloat(),1f,(mc.player.posZ - blockPos.down().z).toFloat()))
                        PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(slot))

                        mc.player.motionX = 0.0
                        mc.player.motionY = 0.0
                        mc.player.motionZ = 0.0
                        if (mc.player.inventory.currentItem != slot) {
                            needC09Back = true
                        }
                    }
                }
            }
            if (event.eventState == EventState.POST) {
                if (needC09Back) {
                    needC09Back = false

                    mc.player.motionX = 0.0
                    mc.player.motionY = 0.0
                    mc.player.motionZ = 0.0
                    PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(mc.player.inventory.currentItem))
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val thePlayer = mc.player ?: return

        val packet = event.packet


        if (packet is SPacketEntityVelocity) {
            val packetEntityVelocity = packet


            if ((mc.world?.getEntityByID(packetEntityVelocity.entityID) ?: return) != thePlayer)
                return

            velocityTimer.reset()

            when (modeValue.get().toLowerCase()) {
                "simple" -> {
                    if (explosion && explosionCheck.get()) {explosion=false;return}

                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    if (horizontal == 0F && vertical == 0F)
                        event.cancelEvent()

                    packetEntityVelocity.motionX = (packetEntityVelocity.motionX * horizontal).toInt()
                    packetEntityVelocity.motionY = (packetEntityVelocity.motionY * vertical).toInt()
                    packetEntityVelocity.motionZ = (packetEntityVelocity.motionZ * horizontal).toInt()
                }

                "aac", "reverse", "smoothreverse", "aaczero" -> velocityInput = true

                "aac5packet" -> {
                    if(mc.isIntegratedServerRunning) return
                    if (thePlayer.isBurning&&fireCheck.get()) return
                    PacketUtils.sendPacketNoEvent(CPacketPlayer.Position(mc.player.posX, Double.MAX_VALUE, mc.player.posZ, mc.player.onGround))
                    event.cancelEvent()
                }


                "glitch" -> {
                    if (!thePlayer.onGround)
                        return

                    velocityInput = true
                    event.cancelEvent()
                }
            }
        } else if (packet is SPacketExplosion) {
            if (packet.motionX != 0F ||
                packet.motionY != 0F ||
                packet.motionZ != 0F) explosion = true
            if (noExplosionPush.get()) {
                packet.motionX = 0F
                packet.motionY = 0F
                packet.motionZ = 0F
            }
            if (cancelExplosionPacket.get()) event.cancelEvent()
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        val thePlayer = mc.player

        if (thePlayer == null || thePlayer.isInWater || thePlayer.isInLava || thePlayer.isInWeb)
            return

        when (modeValue.get().toLowerCase()) {
            "aacpush" -> {
                jump = true

                if (!thePlayer.isCollidedVertically)
                    event.cancelEvent()
            }
            "aaczero" -> if (thePlayer.hurtTime > 0)
                event.cancelEvent()
        }
    }
    private fun getWebFromHotbar(): Int {
        for (i in 0..8) {
            if (mc.player.inventory.mainInventory[i] != null) {
                val a: ItemStack? = mc.player.inventory.mainInventory[i]
                val item = a!!.item
                if (item is ItemBlock) {
                    if (item.block == Blocks.WEB) {
                        return i
                    }
                }
            }
        }
        return -1
    }
}