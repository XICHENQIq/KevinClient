package kevin.module.modules.movement

import kevin.KevinClient
import kevin.event.*
import kevin.module.*
import kevin.module.modules.combat.DuelGod
import kevin.module.modules.player.Blink
import kevin.utils.ChatUtils
import kevin.utils.MovementUtils
import kevin.utils.PacketUtils
import kevin.utils.RenderUtils
import kevin.utils.render.RenderUtil
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.inventory.Slot
import net.minecraft.item.*
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketSetSlot
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import java.util.concurrent.LinkedBlockingQueue

class NoSlow : Module("NoSlow", "Cancels slowness effects caused by soulsand and using items.", category = ModuleCategory.MOVEMENT) {

    private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    private val packetMode = ListValue("PacketMode", arrayOf("None","GrimBug","AntiCheat","AAC5","HytGrim","Test","Fake"),"None")
    private val eatMode = ListValue("EatMode", arrayOf("Click","Anim","None"),"Click")

    val soulsandValue = BooleanValue("Soulsand", true)
    val liquidPushValue = BooleanValue("LiquidPush", true)

    var post = false
    var postSlow = false
    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayServer>>()
    private val slotPacket = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()

    var tick = 0
    var process = false
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (process) {
            tick ++
        }
        if (tick >= 33 && mc.gameSettings.keyBindUseItem.pressed) {
            mc.gameSettings.keyBindUseItem.pressed = false
        }
        if (tick == 3) {
            PacketUtils.sendPacketNoEvent(CPacketPlayerDigging(CPacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN,EnumFacing.DOWN))
        }
        if (tick >= 35) {
            process = false
            tick = 0
            try {
                while (!slotPacket.isEmpty()) {
                    slotPacket.take().processPacket(mc.connection!!)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    @EventTarget
    fun onRender2d(event: Render2DEvent) {
        if (process && eatMode.get() == "Click") {
            val sr = ScaledResolution(mc)
            RenderUtil.drawProcess(sr.scaledWidth / 2 - 79f,sr.scaledHeight / 2 + 100f,"Eating ...",tick.toFloat(),35f,RenderUtils.skyRainbow(1,1f,1f).rgb)
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (!MovementUtils.isMoving)
            return

        when(packetMode.get()){
            "AntiCheat" -> {
                when (event.eventState) {
                    EventState.PRE -> {
                        val digging = CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(0, 0, 0), EnumFacing.DOWN)
                        mc.connection!!.sendPacket(digging)
                    }
                    EventState.POST -> {
                        val blockPlace = CPacketPlayerTryUseItemOnBlock(BlockPos(-1, -1, -1), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F)
                        mc.connection!!.sendPacket(blockPlace)
                    }
                }
            }
            "AAC5" -> {
                if (event.eventState == EventState.POST && (mc.player.isHandActive)) {
                    mc.connection!!.sendPacket(CPacketPlayerTryUseItemOnBlock(BlockPos(-1, -1, -1), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0f, 0f, 0f))
                }
            }
            "HytGrim" -> {
                val duelGod = KevinClient.moduleManager.getModule(DuelGod::class.java) as DuelGod
                if (duelGod.state) {
                    postSlow = false
                    return
                }
                post = event.eventState == EventState.POST
                postSlow = true
                if (mc.player.heldItemMainhand.item is ItemSword && mc.gameSettings.keyBindUseItem.isKeyDown) {
                    if (event.eventState == EventState.PRE) {
                        mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange((mc.player.inventory.currentItem + 1) % 9))
                        mc.connection!!.networkManager.sendPacket(CPacketConfirmTransaction(0,0,true))
                        mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
                    }
                    if (event.eventState == EventState.POST) {
                        mc.connection!!.networkManager.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                    }
                } else {
                    postSlow = false
                }
                if (mc.player.heldItemMainhand.item is ItemBow && mc.gameSettings.keyBindUseItem.isKeyDown && MovementUtils.isMoving) {
                    if (event.eventState == EventState.PRE) {
                        mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange((mc.player.inventory.currentItem + 1) % 9))
                        mc.connection!!.networkManager.sendPacket(CPacketConfirmTransaction(0,0,true))
                        mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
                    }
                }
            }
            "Test" -> {
                if (mc.player.heldItemMainhand.item is ItemSword && mc.gameSettings.keyBindUseItem.isKeyDown) {

                    mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange((mc.player.inventory.currentItem + 1) % 9))
                    mc.connection!!.networkManager.sendPacket(CPacketConfirmTransaction(0,0,true))
                    mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
                    // mc.connection!!.networkManager.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                }
                if (mc.player.heldItemMainhand.item is ItemBow && mc.gameSettings.keyBindUseItem.isKeyDown && MovementUtils.isMoving) {

                    mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange((mc.player.inventory.currentItem + 1) % 9))
                    mc.connection!!.networkManager.sendPacket(CPacketConfirmTransaction(0,0,true))
                    mc.connection!!.networkManager.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))

                }
            }
        }
    }
    @EventTarget
    fun onUpdate1(event: UpdateEvent) {
    }
    @EventTarget
    fun onTick(event: TickEvent) {
        if (post && packetMode.get() == "HytGrim") {
            blink2()
        }

    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packetMode.get() == "GrimBug") {
            if (mc.player.heldItemMainhand.item is ItemAppleGold || mc.player.heldItemMainhand.item is ItemSword || mc.player.heldItemMainhand.item is ItemBow) {
                if (packet is CPacketPlayerTryUseItem) {
                    PacketUtils.sendPacketNoEvent(CPacketPlayerTryUseItemOnBlock(BlockPos(mc.player.posX,mc.player.posY + 5,mc.player.posZ),EnumFacing.UP,EnumHand.MAIN_HAND,0f,0f,0f))
                }
            }
        }

        if (packetMode.get() == "Fake") {
            if (mc.player.heldItemMainhand.item is ItemSword) {
                if (packet is CPacketPlayerTryUseItem) {
                    event.cancelEvent()
                }
                if (packet is CPacketPlayerDigging) {
                    if (packet.action == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                        event.cancelEvent()
                    }
                }
            }
        }

        if (packet is CPacketHeldItemChange) {
            process = false
            tick = 0
        }
        if (packetMode.get() == "HytGrim" && mc.player.heldItemMainhand.item is ItemAppleGold && mc.playerController.isNotCreative && eatMode.get() != "None") {
            if (mc.player.heldItemMainhand.count > 2) {
                if (packet is CPacketPlayerTryUseItem) {
                    tick = 0
                    process = true
                }
                if (packet is CPacketPlayerDigging && packet.action == CPacketPlayerDigging.Action.RELEASE_USE_ITEM && mc.gameSettings.keyBindUseItem.isKeyDown) {
                    event.cancelEvent()
                }
                if (mc.gameSettings.keyBindUseItem.isKeyDown) {
                    if (packet is SPacketSetSlot && eatMode.get() == "Anim" && (packet.stack.item is ItemAppleGold || packet.stack.item is ItemSword)) {
                        slotPacket.add(packet)
                        event.cancelEvent()
                    }
                }
            } else {
                process = false
                tick = 0
            }
        }

        val blink = KevinClient.moduleManager.getModule(Blink::class.java) as Blink
        if (postSlow && packetMode.get() == "HytGrim" && !blink.state) {
            if (packet is CPacketPlayer) {
                event.cancelEvent()
                packets.add(packet)
            }
            if (packet is CPacketHeldItemChange) {
                event.cancelEvent()
                packets.add(packet)
            }
            if (packet is CPacketConfirmTransaction) {
                event.cancelEvent()
                packets.add(packet)
            }
            if ((packet is CPacketPlayerTryUseItemOnBlock || packet is CPacketUseEntity && (packet.action == CPacketUseEntity.Action.INTERACT || packet.action == CPacketUseEntity.Action.INTERACT_AT)) && mc.player.heldItemMainhand.item is ItemSword) {
                event.cancelEvent()
            }
        }
    }

    override val tag: String
        get() = packetMode.get()

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = if(mc.player!!.heldItemMainhand.isEmpty) mc.player!!.heldItemOffhand.item else mc.player!!.heldItemMainhand.item

        val duelGod = KevinClient.moduleManager.getModule(DuelGod::class.java) as DuelGod
        if (duelGod.state) {
            return
        }

        if (mc.player.heldItemMainhand.item is ItemAppleGold && tick <= 3) {
            return
        }
        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }
    private fun blink2() {
        try {
            while (!packets.isEmpty()) {
                val packet = packets.take()
                PacketUtils.sendPacketNoEvent(packet as Packet<INetHandlerPlayServer>)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMultiplier(item: Item?, isForward: Boolean): Float {
        return when {
            (item)is ItemFood || (item)is ItemPotion || (item)is ItemBucketMilk -> {
                if (isForward) this.consumeForwardMultiplier.get() else this.consumeStrafeMultiplier.get()
            }
            (item)is ItemSword || (item)is ItemShield -> {
                if (isForward) this.blockForwardMultiplier.get() else this.blockStrafeMultiplier.get()
            }
            (item)is ItemBow -> {
                if (isForward) this.bowForwardMultiplier.get() else this.bowStrafeMultiplier.get()
            }
            else -> 0.2F
        }
    }
}