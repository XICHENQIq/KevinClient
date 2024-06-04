package kevin.module.modules.misc

import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.event.UpdateEvent
import kevin.event.WorldEvent
import kevin.module.BooleanValue
import kevin.module.ListValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.BlockUtils
import kevin.utils.ChatUtils
import net.minecraft.block.BlockAir
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketAnimation
import net.minecraft.network.play.server.SPacketEntity
import net.minecraft.util.math.BlockPos


class AntiCheater : Module(name = "AntiCheater", description = "by XiChenQi",category =  ModuleCategory.MISC) {
    private val noSlowCheck = BooleanValue("NoSlow",true)
    private val autoBlockCheck = BooleanValue("AutoBlock",true)
    private val velocityCheck = BooleanValue("AntiKB",true)
    private val antiKBMode = ListValue("AntiKB-Mode", arrayOf("C02Instant","PositionChange"),"C02Instant")
    private val morePacketCheck = BooleanValue("MorePacket",true)

    private var noSlowVl = mutableMapOf<Int, Int>()
    private var autoBlockVl = mutableMapOf<Int, Int>()
    private var velocityVl = mutableMapOf<Int, Int>()
    private var morePacketVl = mutableMapOf<Int, Int>()

    // Velocity
    private var normalKB = false
    private var lastPosX = 0.0
    private var lastPosZ = 0.0
    var a = 0
    private var c02s = 0
    private var lastEntityID = 0

    // NoSlowA
    private var b = 0

    // AutoBlock
    private var c = 0

    // MorePacket
    private var d = 0
    private var s14s = 0
    private var lastEntityID2 = 0


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        for (entity in mc.world.loadedEntityList) {
            if (entity is EntityPlayer) {
                // AntiKB
                if (velocityCheck.get()) {
                    if (entity.isInWeb || entity.isInWater || entity.isInLava) {
                        return
                    }
                    if (antiKBMode.get() == "PositionChange") {
                        if (entity.hurtTime == 9) {
                            lastPosX = entity.posX
                            lastPosZ = entity.posZ
                            return
                        }
                        val accuracy = 0.3
                        if (entity.hurtTime == 5 && entity.posX - lastPosX <= accuracy && entity.posX - lastPosX >= -accuracy && entity.posZ - lastPosZ <= accuracy && entity.posZ - lastPosZ >= -accuracy) {
                            val blocks = BlockUtils.searchBlocks2(BlockPos(entity.posX.toInt(),entity.posY.toInt(),entity.posZ.toInt()),2)
                            for (block in blocks) {
                                if (block.value !is BlockAir) {
                                    if (block.key.y == entity.posY.toInt() || block.key.y == entity.posY.toInt() + 1) {
                                        if (block.key.x == entity.posX.toInt() + 1 || block.key.x == entity.posX.toInt() - 1) {
                                            if (block.key.z == entity.posZ.toInt() + 1 || block.key.z == entity.posZ.toInt() - 1) {
                                                normalKB = true
                                            }
                                        }
                                    }
                                }
                            }
                            if (!normalKB) {
                                deBugVl("AntiKB",entity.name.toString(),entity.entityId,velocityVl,"")
                            } else {
                                normalKB = false
                            }
                        }
                    }
                }
                // NoSlow
                if (noSlowCheck.get()) {
                    if (entity.hurtTime == 0 && (entity.activeItemStackUseCount > 5 || entity.isHandActive) && entity.isSprinting) {
                        b ++
                        if (b >= 10) {
                            deBugVl("NoSlowA",entity.name.toString(),entity.entityId,noSlowVl,"")
                            b = 0
                        }
                    }
                }
            }
        }
        // AntiKB - C02Instant
        a ++
        if (a >= 2) {
            c02s = 0
            a = 0
        }
        if (c02s >= 5) {
            if (mc.world.getEntityByID(lastEntityID) is EntityPlayer) {
                deBugVl("AntiKB",mc.world.getEntityByID(lastEntityID)!!.name.toString(),lastEntityID,velocityVl, "/attacks: $c02s")
            }
        }
        // MorePacket
        d ++
        if (d >= 2) {
            s14s = 0
            d = 0
        }
        if (s14s >= 5) {
            if (mc.world.getEntityByID(lastEntityID2) is EntityPlayer) {
                deBugVl("MorePacket",mc.world.getEntityByID(lastEntityID2)!!.name.toString(),lastEntityID2,morePacketVl, "/packets: $s14s")
            }
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (antiKBMode.get() == "C02Instant") {
            if (packet is SPacketAnimation) {
                if (lastEntityID != packet.entityID) {
                    lastEntityID = packet.entityID
                } else {
                    c02s ++
                }
            }
        }
        // AutoBlock
        if (autoBlockCheck.get()) {
            if (packet is SPacketAnimation) {
                val entity = mc.world.getEntityByID(packet.entityID)
                if (entity is EntityPlayer) {
                    if (entity.isHandActive) {
                        c ++
                        if (c >= 10) {
                            deBugVl("AutoBlock",entity.name.toString(),entity.entityId,autoBlockVl,"")
                            c = 0
                        }
                    }
                }
            }
        }
        // MorePacket
        if (morePacketCheck.get()) {
            if (packet is SPacketEntity.S15PacketEntityRelMove) {
                if (lastEntityID != packet.getEntity(mc.world).entityId) {
                    lastEntityID = packet.getEntity(mc.world).entityId
                } else {
                    s14s ++
                }
            } else if (packet is SPacketEntity.S17PacketEntityLookMove) {
                if (lastEntityID != packet.getEntity(mc.world).entityId) {
                    lastEntityID = packet.getEntity(mc.world).entityId
                } else {
                    s14s ++
                }
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        noSlowVl.clear()
        autoBlockVl.clear()
        velocityVl.clear()
    }

    private fun deBugVl(vlName: String, playerName: String, entityID: Int ,derta: MutableMap<Int, Int>, other: String) {
        if (playerName == mc.player.name)
            return
        if (derta[entityID] == null) {
            derta[entityID] = 1
        } else {
            val lastVl = derta[entityID]!!
            derta.remove(entityID)
            derta[entityID] = lastVl + 1
        }
        val vl = derta[entityID]!!
        ChatUtils.message("§7[§bGr1mAC§7] $playerName §ffailed §b$vlName §f(vl:$vl.0) §7$other")
    }
}