package kevin.module.modules.world


import com.yumegod.obfuscation.Native
import kevin.event.*
import kevin.module.BooleanValue
import kevin.module.IntegerValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.module.modules.movement.Sprint
import kevin.utils.*
import kevin.utils.RotationUtils.wrapAngleTo180_float
import net.minecraft.block.BlockBush
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketBlockChange
import net.minecraft.network.play.server.SPacketConfirmTransaction
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper.atan2
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Native
class Scaffold3 : Module("Scaffold3", description = "sca", category = ModuleCategory.WORLD) {

    private val autoBlock = BooleanValue("AutoBlock", true)
    private val farSearch = BooleanValue("FarSearch", false)
    private val ghostBlock = BooleanValue("GhostBlock",false)
    private val tower = BooleanValue("Tower",false)
    private val keepRotation = BooleanValue("KeepRotation",true)
    private val placeTick = IntegerValue("PlaceTick", 4, 0, 8)
    private val render = BooleanValue("Mark",false)
    //private val strafe = BooleanValue("Strafe",false)

    private var targetBlock: BlockPos? = null
    private var targetFacing: EnumFacing? = null
    private var targetRotation: Vector2f? = null
    private var targetVec: Vec3d? = null
    private var playerIntY = 0
    private var airTick = 0

    private var blockPosList = mutableListOf<BlockPos>()

    private var groundYaw = 0f
    private var lastRotation = Rotation(0f,0f)

    private var sPackets = mutableListOf<Packet<INetHandlerPlayClient>>()

    var serverSlot = 0
    var a = false


    // tower
    private var lastY = 0.0
    private var needLag = false


    override fun onEnable() {
        serverSlot = mc.player.inventory.currentItem
        a = false
        airTick = 0
        playerIntY = mc.player.posY.toInt()
        groundYaw = mc.player.rotationYaw
    }

    override fun onDisable() {
        blockPosList.clear()
        lastRotation = Rotation(0f,0f)
        for (packet in sPackets) {
            packet.processPacket(mc.connection!!)
        }
        sPackets.clear()
    }


    @EventTarget
    fun onTick(event: TickEvent) {
        if (mc.player.onGround) {
        } else {
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent?) {
    }

    @EventTarget
    fun onPreUpdate(event: UpdateEvent) {
        if (tower.get()) {
            if (mc.player!!.onGround) {
                lastY = mc.player.posY
            }
            if (mc.player!!.posY >= lastY + 1.0) {
                needLag = true
            }
        }
        if (mc.player.onGround && !mc.gameSettings.keyBindJump.pressed && mc.player.isSprinting) {
            mc.player.jump()
        }
        if (blockPosList.size > 5) {
            blockPosList.removeIf { pos: BlockPos -> mc.player.getDistanceSq(pos) > 5f }
        }
        if (mc.player.onGround) {
            RotationUtils.targetRotation = null
            RotationUtils.reset()
            Sprint.sprintTick = 5
            Sprint.canSprint = true

            playerIntY = mc.player.posY.toInt()
            if (airTick > 0) {
                airTick = 0
            }
        } else {
            Sprint.canSprint = false
            Sprint.sprintTick = 0
            Sprint.canSprint = false
            airTick++
            searchAndRotation()
            if (airTick >= placeTick.get()) {

                checkAndPlace()
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (lastRotation != Rotation(0f, 0f) && keepRotation.get() && !mc.player.onGround) {
            RotationUtils.setTargetRotation(lastRotation,0)
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is CPacketPlayer && needLag && tower.get()){
            lastY = mc.player.posY
            //PacketUtils.sendPacketNoEvent(CPacketPlayer.Position(mc.player.posX + if (mc.player.posX<0) 1000.0 else -1000.0, 0.0, 0.0, false))
            PacketUtils.sendPacketNoEvent(CPacketPlayer(true))
            event.cancelEvent()
            needLag = false
        }

        if (!ghostBlock.get())
            return

        if (packet is SPacketConfirmTransaction) {
            event.cancelEvent()
            mc.connection!!.networkManager.sendPacket(CPacketConfirmTransaction(0,0,true))
            sPackets.add(packet)
        }
        if (packet is SPacketBlockChange && packet.blockState.block == Blocks.AIR) {
            event.cancelEvent()
            sPackets.add(packet)
        }

        if (a) {
            return
        }

        if (packet is CPacketHeldItemChange) {
            serverSlot = packet.slotId
        } else if (packet is CPacketPlayerTryUseItemOnBlock) {
            if (packet.hand == EnumHand.OFF_HAND) {
                return
            }

            event.cancelEvent()
            a = true
            PacketUtils.sendPacketNoEvent(CPacketHeldItemChange((serverSlot + 1) % 9))
            PacketUtils.sendPacketNoEvent(CPacketConfirmTransaction(0,0,true))
            PacketUtils.sendPacketNoEvent(packet)
            PacketUtils.sendPacketNoEvent(CPacketHeldItemChange(serverSlot))
            a = false
        }
    }


    private fun checkAndPlace() {
        if (targetBlock == null || targetFacing == null || targetVec == null) return

        if (targetFacing == EnumFacing.UP && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) return
        val player: EntityPlayer = mc.player
        val eyesPos: Vec3d = Vec3d(player.posX, player.entityBoundingBox.minY + player.eyeHeight, player.posZ)
        val rotVec: Vec3d = Voids.getVectorForRotation(Vector2f(RotationUtils.targetRotation.yaw, RotationUtils.targetRotation.pitch))
        // val rotVec: Vec3d = Voids.getVectorForRotation(Vector2f(player.rotationYaw, player.rotationPitch))
        val vector: Vec3d = eyesPos.addVector(
                rotVec.x * 4.5,
                rotVec.y * 4.5,
                rotVec.z * 4.5
        )

        val movingObjectPosition: RayTraceResult? = mc.world.rayTraceBlocks(
                eyesPos,
                vector,
                false,
                false,
                true
        )

        var itemStack = player.heldItemMainhand
        val lastSlot = mc.player.inventory.currentItem
        var a = false
        if (itemStack == null || itemStack.item !is ItemBlock || (itemStack.item!! as ItemBlock).block is BlockBush || player.heldItemMainhand!!.stackSize <= 0) {
            val blockSlot = InventoryUtils.findAutoBlockBlock()

            if (blockSlot == -1) {
                return
            }

            if (autoBlock.get()) {

                a = true

                //mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange(blockSlot - 36))
                mc.player.inventory.currentItem = blockSlot - 36
                mc.playerController.updateController()
            }

        }

        if (movingObjectPosition != null) {
            if (movingObjectPosition.getBlockPos().getY() === targetBlock!!.getY() && movingObjectPosition.getBlockPos().getX() === targetBlock!!.getX() && movingObjectPosition.getBlockPos().getZ() === targetBlock!!.getZ() && movingObjectPosition.sideHit === targetFacing) {
                if (mc.playerController.processRightClickBlock(mc.player, mc.world, targetBlock!!, targetFacing!!, targetVec!!, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS) {
                    blockPosList.add(targetBlock!!)
                    mc.player.swingArm(EnumHand.MAIN_HAND)
                }
            }
        }

        if (autoBlock.get() && a) {
            //mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange(lastSlot))
            //mc.player.inventory.currentItem = lastSlot
            //mc.playerController.updateController()
        }
    }

    private fun searchAndRotation() {
        val vec3: Vec3d = Voids.getPlacePossibility(0.0, 0.1, 0.0) ?: return

        var pos: BlockPos = BlockPos(0, 0, 0)
        if (farSearch.get()) {
            pos = BlockPos(vec3.x, vec3.y, vec3.z)
        } else {
            val player = mc.player ?: return

            val posx = player.posX
            val posz = player.posZ
            val blockPosition =
                    if (!mc.gameSettings.keyBindJump.pressed) {
                        BlockPos(posx, playerIntY - 1.0, posz)
                    } else (if (player.posY == player.posY.roundToInt() + 0.5) {
                        BlockPos(player)
                    } else {
                        BlockPos(posx, player.posY, posz).down()
                    })

            pos = blockPosition
        }

        val player: EntityPlayer = mc.player
        val world: World = mc.world

        if (!mc.world.getBlockState(pos).getBlock().getMaterial(mc.world.getBlockState(pos)).isReplaceable()) return

        //System.out.println(1);
        for (facingType in EnumFacing.entries) {
            val neighbor: BlockPos = pos.offset(facingType)


            val dirVec = Vec3d(facingType.directionVec)
            var xSearch = 0.5
            while (xSearch <= 0.5) {
                var ySearch = 0.5
                while (ySearch <= 0.5) {
                    var zSearch = 0.5
                    while (zSearch <= 0.5) {
                        val eyesPos = Vec3d(player.posX, player.entityBoundingBox.minY + player.eyeHeight, player.posZ)
                        val posVec: Vec3d = Vec3d(pos).addVector(xSearch, ySearch, zSearch)
                        val hitVec: Vec3d = posVec.add(Vec3d(dirVec.x * 0.5, dirVec.y * 0.5, dirVec.z * 0.5))
                        val distanceSqPosVec: Double = eyesPos.squareDistanceTo(posVec)

                        if (eyesPos.distanceTo(hitVec) > 4.5 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                        posVec.add(dirVec)
                                ) || world.rayTraceBlocks(
                                        eyesPos,
                                        hitVec,
                                        false,
                                        true,
                                        false
                                ) != null
                        ) {
                            zSearch += 0.01
                            continue
                        }

                        val diffX: Double = hitVec.x - eyesPos.x
                        val diffY: Double = hitVec.y - eyesPos.y
                        val diffZ: Double = hitVec.z - eyesPos.z
                        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)

                        if (facingType != EnumFacing.UP && facingType != EnumFacing.DOWN) {
                            var diff = if (facingType == EnumFacing.NORTH || facingType == EnumFacing.SOUTH) {
                                abs(diffZ)
                            } else {
                                abs(diffX)
                            }

                            if (diff < 0) {
                                println(3)
                                zSearch += 0.01
                                continue
                            }
                        }


                        val rotation = Vector2f(
                                wrapAngleTo180_float((Math.toDegrees(atan2(diffZ, diffX)) - 90f).toFloat()),
                                wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                        )
                        // System.out.println(34);
                        val rotVec: Vec3d = Voids.getVectorForRotation(rotation)
                        val vector: Vec3d = eyesPos.addVector(
                                rotVec.x * 4.5,
                                rotVec.y * 4.5,
                                rotVec.z * 4.5
                        )

                        val obj: RayTraceResult? = world.rayTraceBlocks(
                                eyesPos,
                                vector,
                                false,
                                false,
                                true
                        )
                        if (obj == null) {
                            continue
                        }

                        // System.out.println(obj.sideHit != facingType.getOpposite());
                        if (obj.typeOfHit !== RayTraceResult.Type.BLOCK || (obj.getBlockPos().getX() !== neighbor.getX() || obj.getBlockPos().getZ() !== neighbor.getZ() || obj.getBlockPos().getY() !== neighbor.getY()) || (obj.sideHit !== facingType.opposite)) {
                            zSearch += 0.01
                            continue
                        }

                        targetBlock = neighbor
                        targetFacing = facingType.opposite
                        targetVec = obj.hitVec
                        targetRotation = rotation

                        zSearch += 0.01
                    }
                    ySearch += 0.01
                }
                xSearch += 0.01
            }

            if (targetBlock == null || targetFacing == null || targetVec == null) return

            RotationUtils.setTargetRotation(Rotation(targetRotation!!.x, targetRotation!!.y), 15)
            lastRotation = Rotation(targetRotation!!.x, targetRotation!!.y)

        }
    }
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (render.get() && blockPosList.isNotEmpty()) {
            for (block in blockPosList) {
                RenderUtils.drawBlockBox(block,RenderUtils.skyRainbow(1, 1F, 1F),true)
            }
        }
    }
}