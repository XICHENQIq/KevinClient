/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */

package kevin.module.modules.world

import kevin.KevinClient
import kevin.event.*
import kevin.module.*
import kevin.module.modules.movement.Parkour
import kevin.module.modules.movement.Sprint
import kevin.module.modules.render.BlockOverlay
import kevin.utils.*
import kevin.utils.BlockUtils.canBeClicked
import kevin.utils.BlockUtils.isReplaceable
import kevin.utils.RotationUtils.wrapAngleTo180_float
import kevin.utils.skid.Skid
import kevin.utils.timers.MSTimer
import kevin.utils.timers.TickTimer
import net.minecraft.block.BlockBush
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.settings.GameSettings
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.server.SPacketMultiBlockChange
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*

class Scaffold2 : Module(name = "Scaffold2",
        description = "Automatically places blocks beneath your feet.",
        category = ModuleCategory.WORLD) {

    private val modeValue = ListValue("Mode", arrayOf("Normal", "Rewinside", "Expand"), "Normal")

    private val test = BooleanValue("Test",false)


    // Delay
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minDelay = minDelayValue.get()
            if (minDelay > newValue) {
                set(minDelay)
            }
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelay = maxDelayValue.get()
            if (maxDelay < newValue) {
                set(maxDelay)
            }
        }
    }

    // Placeable delay
    private val placeDelay = BooleanValue("PlaceDelay", true)

    // Autoblock
    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Off", "Pick", "Spoof", "Switch"), "Spoof")

    private val swingValue = BooleanValue("Swing", true)
    private val searchValue = BooleanValue("Search", true)
    private val downValue = BooleanValue("Down", true)
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre", "Post","PreAndPost"), "Post")
    private val placeCheck = BooleanValue("PlaceCheck",false)
    private val placeCheckMode = ListValue("PlaceCheckMod", arrayOf("A","B","Both","BothA"),"Both")
    private val eatBlock = BooleanValue("EatCheck",false)
    private val strict = BooleanValue("Strict",false)

    // Eagle
    private val eagleValue = ListValue("Eagle", arrayOf("Normal", "Silent", "Off"), "Normal")
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10)
    private val edgeDistanceValue = FloatValue("EagleEdgeDistance", 0f, 0f, 0.5f)

    // Expand
    private val omniDirectionalExpand = BooleanValue("OmniDirectionalExpand", false)
    private val expandLengthValue = IntegerValue("ExpandLength", 1, 1, 6)

    // Rotation Options
    private val onPreUpdateMode = BooleanValue("onPreUpdate",true)
    private val strafeTest = BooleanValue("StrafeTest",false)
    private val strafeMode = ListValue("Strafe", arrayOf("Off", "AAC"), "AAC")
    private val rotationsValue = BooleanValue("Rotations", true)
    private val silentRotationValue = BooleanValue("SilentRotation", true)
    private val keepRotationValue = BooleanValue("KeepRotation", true)
    private val keepLengthValue = IntegerValue("KeepRotationLength", 0, 0, 20)
    private val sprintJump = BooleanValue("SprintJump",false)

    // XZ/Y range
    private val searchMode = ListValue("XYZSearch", arrayOf("AutoA","AutoB","Auto", "AutoCenter", "Manual"), "AutoCenter")
    private val xzRangeValue = FloatValue("xzRange", 0.8f, 0f, 1f)
    private var yRangeValue = FloatValue("yRange", 0.8f, 0f, 1f)
    private val minDistValue = FloatValue("MinDist", 0.0f, 0.0f, 0.2f)

    private val searchBlockPosMode = ListValue("BlockPosSearch", arrayOf("Normal","Rise","Test","Test2","Test3"), "Normal")

    // Search Accuracy
    private val searchAccuracyValue: IntegerValue = object : IntegerValue("SearchAccuracy", 8, 1, 16) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }

    // Turn Speed
    private val maxTurnSpeedValue: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeedValue.get()
            if (v > newValue) set(v)
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }
    private val minTurnSpeedValue: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeedValue.get()
            if (v < newValue) set(v)
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }

    // Zitter
    private val zitterMode = ListValue("Zitter", arrayOf("Off", "Teleport", "Smooth"), "Off")
    private val zitterSpeed = FloatValue("ZitterSpeed", 0.13f, 0.1f, 0.3f)
    private val zitterStrength = FloatValue("ZitterStrength", 0.05f, 0f, 0.2f)

    // Game
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 10f)
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f)
    private val slowValue = BooleanValue("Slow", false)
    private val slowSpeed = FloatValue("SlowSpeed", 0.6f, 0.2f, 0.8f)

    // Safety
    private val debug = BooleanValue("Debug",false)
    private val sameYValue = BooleanValue("SameY", false)
    private val safeWalkValue = BooleanValue("SafeWalk", true)
    private val airSafeValue = BooleanValue("AirSafe", false)
    private val enableLockRotation = BooleanValue("LockRotation",false)

    // Visuals
    private val counterDisplayValue = BooleanValue("Counter", true)
    private val markValue = BooleanValue("Mark", false)

    // Target block
    private var targetPlace: PlaceInfo? = null

    // Rotation lock
    private var lockRotation: Rotation? = null
    private var lockRotationTimer = TickTimer()

    // Launch position
    private var launchY = 0

    // AutoBlock
    private var slot = 0

    // Zitter Direction
    private var zitterDirection = false

    // Delay
    private val delayTimer = MSTimer()
    private val zitterTimer = MSTimer()
    private var delay = 0L

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    //Telly (ShitCode)
    @JvmField
    val tellyMode = BooleanValue("Telly", true)
    private val airRotDelay = IntegerValue("AirRotTickDelay",0,0,10)
    private val airPlaceDelay = IntegerValue("AirPlaceTickDelay",0,0,10)
    private var canPlace = false
    private var canRot = false
    private var unFaceBlock = false
    private var yaw = 0f
    private var airticks = 0

    // Downwards
    private var shouldGoDown = false

    // Enabling module
    override fun onEnable() {
        val player = mc.player ?: return

        launchY = player.posY.roundToInt()
        slot = player.inventory.currentItem
    }

    @EventTarget
    fun onJump(event: JumpEvent){
        if(sprintJump.get() && mc.player!!.movementInput.moveForward >= 0.8f){
            RotationUtils.targetRotation = null
            RotationUtils.reset()
            lockRotation = null
            targetPlace = null

            mc.player!!.isSprinting = true
        }
    }


    @EventTarget
    fun preUpdate(event: UpdateEvent) {
        if (onPreUpdateMode.get()) {
            if (!tellyMode.get() || !mc.player.onGround) {
                update()

                if (targetPlace != null && faceBlock(
                                RotationUtils.targetRotation,
                                targetPlace!!,
                                targetPlace!!.enumFacing,
                                true
                        ) && canPlace
                )
                    place(false)

                if (!enableLockRotation.get())
                    return

                if (rotationsValue.get() && (keepRotationValue.get() || !lockRotationTimer.hasTimePassed(keepLengthValue.get())) && (canRot || !tellyMode.get())) {
                    val rotation = lockRotation ?: return
                    setRotation(rotation)
                    lockRotationTimer.update()
                    return
                }
            } else {
                RotationUtils.reset()
                Sprint.canSprint = true
            }
        }
    }

    // Events
    @EventTarget
    private fun onUpdate(event: UpdateEvent) {

        val player = mc.player ?: return

        if(tellyMode.get()){
            if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) {
                sameYValue.set(false)
            } else {
                sameYValue.set(true)
                if (mc.player!!.onGround) {
                    mc.player!!.jump()
                }
            }
        }

        mc.timer.timerSpeed = timerValue.get()
        shouldGoDown =
            downValue.get() && !sameYValue.get() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1
        if (shouldGoDown) {
            mc.gameSettings.keyBindSneak.pressed = false
        }
        if (slowValue.get()) {
            player.motionX = player.motionX * slowSpeed.get()
            player.motionZ = player.motionZ * slowSpeed.get()
        }
        // Eagle
        if (!eagleValue.get().equals("Off", true) && !shouldGoDown) {
            var dif = 0.5
            val blockPos = BlockPos(player.posX, player.posY - 1.0, player.posZ)
            if (edgeDistanceValue.get() > 0) {
                for (facingType in EnumFacing.values()) {
                    if (facingType == EnumFacing.UP || facingType == EnumFacing.DOWN) {
                        continue
                    }
                    val neighbor = blockPos.offset(facingType)
                    if (isReplaceable(neighbor)) {
                        val calcDif = (if (facingType == EnumFacing.NORTH || facingType == EnumFacing.SOUTH) {
                            abs((neighbor.z + 0.5) - player.posZ)
                        } else {
                            abs((neighbor.x + 0.5) - player.posX)
                        }) - 0.5

                        if (calcDif < dif) {
                            dif = calcDif
                        }
                    }
                }
            }
            if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                val shouldEagle =
                    isReplaceable(blockPos) || (edgeDistanceValue.get() > 0 && dif < edgeDistanceValue.get())
                if (eagleValue.get().equals("Silent", true)) {
                    if (eagleSneaking != shouldEagle) {
                        mc.connection!!.sendPacket(
                            CPacketEntityAction(
                                player, if (shouldEagle) {
                                    CPacketEntityAction.Action.START_SNEAKING
                                } else {
                                    CPacketEntityAction.Action.STOP_SNEAKING
                                }
                            )
                        )
                    }
                    eagleSneaking = shouldEagle
                } else {
                    mc.gameSettings.keyBindSneak.pressed = shouldEagle
                }
                placedBlocksWithoutEagle = 0
            } else {
                placedBlocksWithoutEagle++
            }
        }

        if (player.onGround) {
            Sprint.canSprint = true
            Sprint.sprintTick = 5
            launchY = mc.player.posY.roundToInt()
            if(tellyMode.get())
                delayTimer.reset()

            airticks = 0
                canPlace = false
                canRot = false


            yaw = ((mc.player.rotationYaw - 180) / 90).roundToInt() * 90f

            when (modeValue.get().toLowerCase()) {
                "rewinside" -> {
                    MovementUtils.strafe(0.2F)
                    player.motionY = 0.0
                }
            }
            when (zitterMode.get().toLowerCase()) {

                "off" -> {
                    return
                }
                "smooth" -> {
                    if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) {
                        mc.gameSettings.keyBindRight.pressed = false
                    }
                    if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) {
                        mc.gameSettings.keyBindLeft.pressed = false
                    }
                    if (zitterTimer.hasTimePassed(100)) {
                        zitterDirection = !zitterDirection
                        zitterTimer.reset()
                    }
                    if (zitterDirection) {
                        mc.gameSettings.keyBindRight.pressed = true
                        mc.gameSettings.keyBindLeft.pressed = false
                    } else {
                        mc.gameSettings.keyBindRight.pressed = false
                        mc.gameSettings.keyBindLeft.pressed = true
                    }
                }
                "teleport" -> {
                    MovementUtils.strafe(zitterSpeed.get())
                    val yaw = Math.toRadians(player.rotationYaw + if (zitterDirection) 90.0 else -90.0)
                    player.motionX = player.motionX - sin(yaw) * zitterStrength.get()
                    player.motionZ = player.motionZ + cos(yaw) * zitterStrength.get()
                    zitterDirection = !zitterDirection
                }
            }
        } else {
            Sprint.sprintTick = 0
            Sprint.canSprint = false
            if(airticks >= airRotDelay.get())
                canRot = true

            if(airticks >= airPlaceDelay.get())
                canPlace  = true
        }

    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.player == null) {
            return
        }

        val packet = event.packet
        if (packet is CPacketHeldItemChange) {
            slot = packet.slotId
        }
        if (packet is SPacketMultiBlockChange && packet.changedBlocks.size == 1 && eatBlock.get()){
            val parkour = KevinClient.moduleManager.getModule(Parkour::class.java) as Parkour
            if(parkour.state)
                parkour.state = false
            state = false
        }

    }

    @EventTarget
    fun onTick(event: TickEvent) {

        if (mc.player.onGround) {
            airticks = 0
        } else {
            airticks++
        }

        if(targetPlace == null)
            return

        when (placeCheckMode.get().toLowerCase()){
            "a"->{
                unFaceBlock =
                    (!faceBlock(RotationUtils.serverRotation , targetPlace!!, targetPlace!!.enumFacing ,strict.get()))
            }
            "b"->{
                unFaceBlock =
                    !faceBlock(RotationUtils.lastRotation , targetPlace!!, targetPlace!!.enumFacing ,strict.get())
            }
            "both"->{
                unFaceBlock =
                    (!faceBlock(RotationUtils.serverRotation , targetPlace!!, targetPlace!!.enumFacing ,strict.get()) )&&
                            !faceBlock(RotationUtils.lastRotation , targetPlace!!, targetPlace!!.enumFacing ,strict.get())
            }
        }


    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {

        update()


        val rotation = lockRotation ?: return



        if (rotationsValue.get() && (keepRotationValue.get() || !lockRotationTimer.hasTimePassed(keepLengthValue.get())) && (canRot || !tellyMode.get())) {

            setRotation(rotation)
            lockRotationTimer.update()

            return

        }

    }



    @EventTarget
    fun onMotion(event: MotionEvent) {

        if(onPreUpdateMode.get())
            return

        val eventState = event.eventState
        // Lock Rotation
        if (rotationsValue.get() && (keepRotationValue.get() || !lockRotationTimer.hasTimePassed(keepLengthValue.get())) && lockRotation != null && strafeMode.get().equals("Off", true)
        ) {
            setRotation(lockRotation!!)
            if (eventState == EventState.POST) {
                lockRotationTimer.update()
            }
        }



        // Face block
        if ((placeModeValue.get().equals(eventState.stateName, true) || placeModeValue.get().equals("PreAndPost", true))) {
                place()
        }

        // Update and search for a new block
        if (eventState == EventState.PRE && (strafeMode.get().equals("Off", true))  ) {
              update()
        }

        // Reset placeable delay
        if (targetPlace == null && placeDelay.get()) {
            delayTimer.reset()
        }
    }

    fun update() {
        val player = mc.player ?: return


            val holdingItem = player.heldItemMainhand != null && player.heldItemMainhand!!.item is ItemBlock
            if (if (!autoBlockValue.get().equals("off", true)) InventoryUtils.findAutoBlockBlock() == -1 && !holdingItem else !holdingItem) {
                return
            }


        findBlock(modeValue.get().equals("expand", true))
    }

    private fun setRotation(rotation: Rotation) {
        val player = mc.player ?: return
        if(tellyMode.get() && !canRot){
            return
        }
        if (silentRotationValue.get()) {

            if(strafeMode.get().equals("Test", true)){
                RotationUtils.setTargetRotation(rotation, 0)
            } else RotationUtils.setTargetRotation(rotation, 0)

        } else {
            player.rotationYaw = rotation.yaw
            player.rotationPitch = rotation.pitch
        }
    }



    // Search for new target block
    private fun findBlock(expand: Boolean) {
        val player = mc.player ?: return

        val posx = player.posX
        val posz = player.posZ

        when (searchBlockPosMode.get().toLowerCase()) {
            "test2"->{

                if(!sameYValue.get()){
                    val targetBlock = Skid.getPlacePossibility(0.0, 0.0, 0.0)

                    val blockPos = BlockPos(targetBlock.x, targetBlock.y, targetBlock.z)

                    if (search(blockPos, true)) {
                        return
                    }
                    return
                }

                val targetBlock = Skid.getPlacePossibilityCustomY(0.0, launchY.toDouble() - 1, 0.0)

                val blockPos = BlockPos(targetBlock.x, targetBlock.y, targetBlock.z)

                if (search(blockPos, true)) {
                    return
                }

            }

            "test" -> {
                if(!sameYValue.get()){
                    val targetBlock = Skid.getPlacePossibility(0.0, 0.0, 0.0)

                    val blockPos = BlockPos(targetBlock.x, targetBlock.y, targetBlock.z)

                    if (search(blockPos, true)) {
                        return
                    }
                    return
                }

                val blockPosition =
                    if (shouldGoDown) {
                        (if (player.posY == player.posY.roundToInt() + 0.5) {
                            BlockPos(posx, player.posY - 0.6, posz)
                        } else {
                            BlockPos(posx, player.posY - 0.6, posz).down()
                        })
                    } else (if ((sameYValue.get()) && launchY <= player.posY) {
                        BlockPos(posx, launchY - 1.0, posz)
                    } else (if (player.posY == player.posY.roundToInt() + 0.5) {
                        BlockPos(player)
                    } else {
                        BlockPos(posx, player.posY, posz).down()
                    }))

                if (!expand && (!isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown))) {
                    return

                }


                if (expand) {
                    val yaw = Math.toRadians(player.rotationYaw.toDouble())
                    val x =
                        if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
                    val z =
                        if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
                    for (i in 0 until expandLengthValue.get()) {
                        if (search(blockPosition.add(x * i, 0, z * i), false)) {
                            return
                        }
                    }
                } else if (searchValue.get()) {
                    for (x in -1..1) {
                        for (z in -1..1) {
                            if (search(blockPosition.add(x, 0, z), !shouldGoDown)) {
                                return
                            }
                        }
                    }
                }


                val targetBlock = Skid.getPlacePossibility(0.0, 0.0, 0.0)

                val blockPos = BlockPos(targetBlock.x, targetBlock.y, targetBlock.z)

                if (search(blockPos, true)) {
                    return
                }
            }
            "rise" -> {

                val targetBlock = Skid.getPlacePossibility(0.0, 0.0, 0.0)

                val blockPos = BlockPos(targetBlock.x,targetBlock.y,targetBlock.z)

                if(search(blockPos, true)) {
                    return
                }
            }
            "test3" -> {

                val targetBlock = Skid.getPlacePossibility2(0.0, 0.0, 0.0)

                val blockPos = BlockPos(targetBlock.x,targetBlock.y,targetBlock.z)

                if(search(blockPos, true)) {
                    return
                }
            }
            "normal" -> {
                val blockPosition =
                    if (shouldGoDown) {
                        (if (player.posY == player.posY.roundToInt() + 0.5) {
                            BlockPos(posx, player.posY - 0.6, posz)
                        } else {
                            BlockPos(posx, player.posY - 0.6, posz).down()
                        })
                    } else (if ((sameYValue.get()) && launchY <= player.posY) {
                        BlockPos(posx, launchY - 1.0, posz)
                    } else (if (player.posY == player.posY.roundToInt() + 0.5) {
                        BlockPos(player)
                    } else {
                        BlockPos(posx, player.posY, posz).down()
                    }))

                if (!expand && (!isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown))) {
                    return

                }


                if (expand) {
                    val yaw = Math.toRadians(player.rotationYaw.toDouble())
                    val x =
                        if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
                    val z =
                        if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
                    for (i in 0 until expandLengthValue.get()) {
                        if (search(blockPosition.add(x * i, 0, z * i), false)) {
                            return
                        }
                    }
                } else if (searchValue.get()) {
                    for (x in -1..1) {
                        for (z in -1..1) {
                            if (search(blockPosition.add(x, 0, z), !shouldGoDown)) {
                                return
                            }
                        }
                    }
                }
            }
        }
    }

    private fun faceBlock(rotation: Rotation, placeInfo: PlaceInfo, enumFacing: EnumFacing, checkEnumFacing : Boolean): Boolean{
        val player = mc.player ?: return false
        val world = mc.world ?: return false
        val eyesPos = Vec3d(player.posX, player.entityBoundingBox.minY + player.eyeHeight, player.posZ)
        val rotationVector = RotationUtils.getVectorForRotation(rotation)
        val vector = eyesPos.addVector(
            rotationVector.x * 4.5, rotationVector.y * 4.5, rotationVector.z * 4.5
        )
        val obj = world.rayTraceBlocks(
            eyesPos,
            vector,
            false,
            false,
            true
        ) ?: return false


        if(obj.blockPos != placeInfo.blockPos) {
            return false
        }

        if(checkEnumFacing && obj.sideHit != enumFacing){
            return false
        }

        return true
    }

    private fun place(){
        place(true)
    }

    private fun place(boolean: Boolean) {
        val player = mc.player ?: return
        val world = mc.world ?: return
        val check = placeCheck.get()

        if (targetPlace == null) {
            if (placeDelay.get()) {
                delayTimer.reset()
            }
            return
        }

        if(mc.player.onGround && tellyMode.get()){
            return
        }

        if (!delayTimer.hasTimePassed(delay) || (sameYValue.get() ) && (launchY - 1 != targetPlace!!.vec3.y.toInt() && launchY - 1 < targetPlace!!.vec3.y)) {
            return
        }

        var itemStack = player.heldItemMainhand
        if (itemStack == null || itemStack.item !is ItemBlock|| (itemStack.item!! as ItemBlock).block is BlockBush || player.heldItemMainhand!!.stackSize <= 0) {
            val blockSlot = InventoryUtils.findAutoBlockBlock()

            if (blockSlot == -1) {
                return
            }

            when (autoBlockValue.get().toLowerCase()) {
                "off" -> {
                    return
                }
                "pick" -> {
                    mc.player!!.inventory.currentItem = blockSlot - 36
                    mc.playerController.updateController()
                }
                "spoof" -> {
                    if (blockSlot - 36 != slot) {
                        mc.connection!!.sendPacket(CPacketHeldItemChange(blockSlot - 36))
                    }
                }
                "switch" -> {
                    if (blockSlot - 36 != slot) {
                        mc.connection!!.sendPacket(CPacketHeldItemChange(blockSlot - 36))
                    }
                }
            }
            itemStack = player.inventoryContainer.getSlot(blockSlot).stack
        }

        if(check && unFaceBlock && boolean){

            return
        }

        if (mc.playerController.processRightClickBlock(
                player, world, targetPlace!!.blockPos, targetPlace!!.enumFacing, targetPlace!!.vec3,EnumHand.MAIN_HAND
            ) == EnumActionResult.SUCCESS
        ) {
            delayTimer.reset()
            delay = if (!placeDelay.get()) 0 else TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())

            if (player.onGround) {
                val modifier = speedModifierValue.get()
                player.motionX = player.motionX * modifier
                player.motionZ = player.motionZ * modifier
            }

            if (swingValue.get()) {
                player.swingArm(EnumHand.MAIN_HAND)
            }
        }
        if (autoBlockValue.get().equals("Switch", true)) {
            if (slot != player.inventory.currentItem) {
                mc.connection!!.sendPacket(CPacketHeldItemChange(player.inventory.currentItem))
            }
        }
        targetPlace = null
    }

    // Disabling module
    override fun onDisable() {
        val player = mc.player ?: return


        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking) {
                mc.connection!!.sendPacket(
                    CPacketEntityAction(
                        player, CPacketEntityAction.Action.STOP_SNEAKING
                    )
                )
            }
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) {
            mc.gameSettings.keyBindRight.pressed = false
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) {
            mc.gameSettings.keyBindLeft.pressed = false
        }

        lockRotation = null
        mc.timer.timerSpeed = 1f
        shouldGoDown = false
        targetPlace = null

        if (slot != player.inventory.currentItem) {
            mc.connection!!.sendPacket(CPacketHeldItemChange(player.inventory.currentItem))
        }
    }

    // Entity movement event
    @EventTarget
    fun onMove(event: MoveEvent) {
        val player = mc.player ?: return

        if (!safeWalkValue.get() || shouldGoDown) {
            return
        }
        if (airSafeValue.get() || player.onGround) {
            event.isSafeWalk = true
        }
    }

    // Scaffold visuals
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (counterDisplayValue.get()) {
            GL11.glPushMatrix()
            val blockOverlay = KevinClient.moduleManager.getModule(BlockOverlay::class.java) as BlockOverlay

            val info = "Blocks: §7$blocksAmount"
            val scaledResolution = ScaledResolution(mc)

            RenderUtils.drawBorderedRect(
                scaledResolution.scaledWidth / 2 - 2.toFloat(),
                scaledResolution.scaledHeight / 2 + 5.toFloat(),
                scaledResolution.scaledWidth / 2 + mc.fontRenderer.getStringWidth(info) + 2.toFloat(),
                scaledResolution.scaledHeight / 2 + 16.toFloat(),
                3f,
                Color.BLACK.rgb,
                Color.BLACK.rgb
            )

            GlStateManager.resetColor()

            mc.fontRenderer.drawString(
                info,
                scaledResolution.scaledWidth / 2,
                scaledResolution.scaledHeight / 2 + 7,
                Color.WHITE.rgb
            )
            GL11.glPopMatrix()
        }
    }

    // Visuals
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val player = mc.player ?: return
        if (!markValue.get()) {
            return
        }
        for (i in 0 until if (modeValue.get().equals("Expand", true)) expandLengthValue.get() + 1 else 2) {
            val yaw = Math.toRadians(player.rotationYaw.toDouble())
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            val blockPos = BlockPos(
                player.posX + x * i,
                if ((sameYValue.get()) && launchY <= player.posY) launchY - 1.0 else player.posY - (if (player.posY == player.posY + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
                player.posZ + z * i
            )
            val placeInfo = PlaceInfo.get(blockPos)
            if (isReplaceable(blockPos) && placeInfo != null) {
                RenderUtils.drawBlockBox(blockPos, Color(68, 117, 255, 100), false)
                break
            }
        }
    }



    private fun calcStepSize(range: Float): Double {
        var accuracy = searchAccuracyValue.get().toDouble()
        accuracy += accuracy % 2 // If it is set to uneven it changes it to even. Fixes a bug
        return if (range / accuracy < 0.01) 0.01 else (range / accuracy)
    }

    // Return hotbar amount
    private val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.player!!.inventoryContainer.getSlot(i).stack
                val itemStackItem = itemStack?.item!!
                if (itemStackItem is ItemBlock) {
                    val block = itemStackItem.block
                    val heldItem = mc.player!!.heldItemMainhand
                    if (heldItem != null && heldItem == itemStack || !InventoryUtils.BLOCK_BLACKLIST.contains(block) && block !is BlockBush) {
                        amount += itemStack.stackSize
                    }
                }
            }
            return amount
        }
    override val tag: String
        get() = modeValue.get()


    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @param raycast visible
     * @return
     */

    private fun search(blockPosition: BlockPos, raycast: Boolean): Boolean {
        val player = mc.player ?: return false
        val world = mc.world ?: return false

        if (!isReplaceable(blockPosition)) {
            return false
        }

        val xzRV = xzRangeValue.get().toDouble()
        val xzSSV = calcStepSize(xzRV.toFloat())
        val yRV = yRangeValue.get().toDouble()
        val ySSV = calcStepSize(yRV.toFloat())
        val eyesPos = Vec3d(player.posX, player.entityBoundingBox.minY + player.eyeHeight, player.posZ)
        var placeRotation: PlaceRotation? = null
        for (facingType in EnumFacing.values()) {
            val neighbor = blockPosition.offset(facingType)
            if (!canBeClicked(neighbor)) {
                continue
            }

            val dirVec = Vec3d(facingType.directionVec)
            val auto = searchMode.get().equals("Auto", true)
            val autoB = searchMode.get().equals("AutoB", true)
            val autoA = searchMode.get().equals("AutoA", true)
            val center = searchMode.get().equals("AutoCenter", true)

            var xSearch = if (auto) 0.1 else if (autoA) 0.5 else 0.5 - xzRV / 2
            while (xSearch <= if (auto) 0.9 else if (autoA) 0.5 else 0.5 + xzRV / 2) {
                var ySearch = if (auto) 0.1 else if (autoA) 0.5 else 0.5 - yRV / 2
                while (ySearch <= if (auto) 0.9 else if (autoA) 0.5 else 0.5 + yRV / 2) {
                    var zSearch = if (auto) 0.1 else if (autoA) 0.5 else 0.5 - xzRV / 2
                    while (zSearch <= if (auto) 0.9 else if (autoA) 0.5 else 0.5 + xzRV / 2) {
                        val posVec = Vec3d(blockPosition).addVector(
                            if (center) 0.5 else if (autoA) 0.5 else xSearch,
                            if (center) 0.5 else if (autoA) 0.5 else ySearch,
                            if (center) 0.5 else if (autoA) 0.5 else zSearch
                        )
                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec =
                            posVec.add(Vec3d(dirVec.x * 0.5, dirVec.y * 0.5, dirVec.z * 0.5))
                        if (raycast && (eyesPos.distanceTo(hitVec) > 4.5 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                posVec.add(dirVec)
                            ) || world.rayTraceBlocks(
                                eyesPos,
                                hitVec,
                                false,
                                true,
                                false
                            ) != null)
                        ) {
                            zSearch += if (auto) 0.1 else if (autoA) 0.1 else xzSSV
                            continue
                        }

                        // Face block
                        val diffX = hitVec.x - eyesPos.x
                        val diffY = hitVec.y - eyesPos.y
                        val diffZ = hitVec.z - eyesPos.z
                        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
                        if (facingType != EnumFacing.UP && facingType != EnumFacing.DOWN) {
                            val diff =
                                abs(if (facingType == EnumFacing.NORTH || facingType == EnumFacing.SOUTH) diffZ else diffX)
                            if (diff < minDistValue.get()) {
                                zSearch += if (auto) 0.1 else if (autoA) 0.1 else xzSSV
                                continue
                            }
                        }
                        val rotation = if(autoB) getRotations(0f,facingType.opposite,neighbor) else Rotation(
                            wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                        )


                        val rotationVector = RotationUtils.getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.x * 4.5,
                            rotationVector.y * 4.5,
                            rotationVector.z * 4.5
                        )

                        val obj = world.rayTraceBlocks(
                            eyesPos,
                            vector,
                            false,
                            false,
                            true
                        ) ?: continue

                        if (obj.typeOfHit != RayTraceResult.Type.BLOCK || obj.blockPos != neighbor || (obj.sideHit != facingType.opposite && strict.get())) {
                            zSearch += if (auto) 0.1 else xzSSV
                            continue
                        }
                        if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(
                                placeRotation.rotation
                            )
                        ) {
                            placeRotation = PlaceRotation(
                                PlaceInfo(neighbor, facingType.opposite, obj.hitVec),
                                rotation
                            )
                        }

                        zSearch += if (auto) 0.1 else if (autoA) 0.1 else xzSSV
                    }
                    ySearch += if (auto) 0.1 else if (autoA) 0.1 else ySSV
                }
                xSearch += if (auto) 0.1 else if (autoA) 0.1 else xzSSV
            }
        }
        if(placeRotation == null){
            return false;
        }

            if (rotationsValue.get()) {
                if (minTurnSpeedValue.get() < 180) {
                    val limitedRotation = RotationUtils.limitAngleChange(
                        RotationUtils.serverRotation,
                        placeRotation.rotation,
                        (Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat()
                    )

                    lockRotation = if ((10 * wrapAngleTo180_float(limitedRotation.yaw)).roundToInt() == (10 * wrapAngleTo180_float(
                            placeRotation.rotation.yaw
                        )).roundToInt() && (10 * wrapAngleTo180_float(limitedRotation.pitch)).roundToInt() == (10 * wrapAngleTo180_float(
                            placeRotation.rotation.pitch
                        )).roundToInt()
                    ) {
                        setRotation(placeRotation.rotation)
                        placeRotation.rotation
                    } else {
                        setRotation(limitedRotation)
                        limitedRotation
                    }
                } else {
                    setRotation(placeRotation.rotation)
                    lockRotation = placeRotation.rotation
                }
                lockRotationTimer.reset()
            }
            targetPlace = placeRotation!!.placeInfo
        return true
    }


    private fun getRotations(yawOffset: Float,enumFacing: EnumFacing,blockFace:BlockPos) : Rotation {
        var found = false
        var possibleYaw = mc.player!!.rotationYaw - 180 + yawOffset
        var rotation : Rotation? = null
        while (possibleYaw <= mc.player!!.rotationYaw + 360 - 180 && !found) {
            var possiblePitch = 90f
            while (possiblePitch > 30 && !found) {
                if (Skid.overBlock(
                        Rotation(possibleYaw, possiblePitch),
                        enumFacing,
                        blockFace,
                        strict.get()
                    )
                ) {
                    rotation = Rotation(possibleYaw,possiblePitch)
                    found = true
                }
                possiblePitch -= (if (possiblePitch > (80)) 1 else 10).toFloat()
            }
            possibleYaw += 45f
        }
        if (!found) {

            rotation = calculate(
                Vec3d(blockFace.x.toDouble(), blockFace.y.toDouble(), blockFace.z.toDouble()), enumFacing
            )
        }

        return rotation!!
    }
    private fun calculate(position: Vec3d, enumFacing: EnumFacing): Rotation {
        var x: Double = position.x + 0.5
        var y: Double = position.y + 0.5
        var z: Double = position.z + 0.5
        x += enumFacing.directionVec.x.toDouble() * 0.5
        y += enumFacing.directionVec.y.toDouble() * 0.5
        z += enumFacing.directionVec.z.toDouble() * 0.5
        return calculate(Vec3d(x, y, z))
    }
    private fun calculate(to: Vec3d): Rotation {
        return calculate(mc.player!!.positionVector.add(Vec3d(0.0, mc.player.getEyeHeight().toDouble(), 0.0)), to)
    }
    private fun calculate(from: Vec3d, to: Vec3d): Rotation {
        val diff: Vec3d = to.subtract(from)
        val distance = Math.hypot(diff.x, diff.z)
        val yaw = (MathHelper.atan2(diff.z, diff.x) * (180.0f / PI)).toFloat() - 90.0f
        val pitch = (-(MathHelper.atan2(diff.y, distance) * (180.0f / PI))).toFloat()
        return Rotation(yaw, pitch)
    }

   /* fun getHitVec(blockFace: BlockPos,enumFacing: EnumFacing): WVec3 {
        /* Correct HitVec */
        var hitVec = WVec3(blockFace.x + Math.random(), blockFace.y + Math.random(), blockFace.z + Math.random())

        val movingObjectPosition: IMovingObjectPosition = Skid.rayCast(RotationUtils.serverRotation, 4.5, 0F,mc.thePlayer)

        when (enumFacing) {
            EnumFacing.DOWN -> hitVec.yCoord = (blockFace.y + 0).toDouble()
            EnumFacing.UP -> hitVec.yCoord = (blockFace.y + 1).toDouble()
            EnumFacing.NORTH -> hitVec.zCoord = (blockFace.z + 0).toDouble()
            EnumFacing.EAST -> hitVec.xCoord = (blockFace.x + 1).toDouble()
            EnumFacing.SOUTH -> hitVec.zCoord = (blockFace.z + 1).toDouble()
            EnumFacing.WEST -> hitVec.xCoord = (blockFace.x + 0).toDouble()
        }

        if (movingObjectPosition.blockPos
            !! == blockFace.wrap() && movingObjectPosition.sideHit === enumFacing.wrap()
        ) {
            hitVec = movingObjectPosition.hitVec
        }
        return hitVec
    } */

}