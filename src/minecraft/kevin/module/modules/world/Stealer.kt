package kevin.module.modules.world

import kevin.KevinClient
import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.event.Render2DEvent
import kevin.event.UpdateEvent
import kevin.module.BooleanValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.module.modules.player.InventoryManager
import kevin.utils.RenderUtils
import kevin.utils.timers.MSTimer
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketOpenWindow
import net.minecraft.network.play.server.SPacketWindowItems
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glTranslated
import com.yumegod.obfuscation.Native
import java.awt.Color

class Stealer : Module("Stealer","自动拿箱子",category = ModuleCategory.WORLD) {
    private val hud = BooleanValue("ChestHud",false)
    private val follow = BooleanValue("FollowChest",false)
    private val usefulCheck = BooleanValue("UsefulCheck",true)

    public var hasWindow = false
    private var windowid = 0
    private var guiMod = 0

    private var items: MutableList<ItemStack>? = null

    private var currentBlockPos: BlockPos? = null

    private val msTimer = MSTimer()

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is SPacketOpenWindow) {
            msTimer.reset()
            hasWindow = true
            windowid = packet.windowId
            event.cancelEvent()
        }
        if (packet is CPacketPlayerTryUseItemOnBlock) {
            currentBlockPos = packet.pos
        }
        if (packet is SPacketWindowItems) {
            if (packet.windowId != windowid)
                return

            items = packet.itemStacks
            when (packet.itemStacks.size) {
                63 -> {
                    guiMod = 1
                    for (index in 0..26) {
                        if (isUseful(packet.itemStacks[index])) {
                            mc.connection!!.networkManager.sendPacket(CPacketClickWindow(packet.windowId,index,1,ClickType.QUICK_MOVE,packet.itemStacks[index],0.toShort()))
                        }
                    }
                }
                39 -> {
                    guiMod = 2
                    for (index in 0..2) {
                        mc.connection!!.networkManager.sendPacket(CPacketClickWindow(packet.windowId,index,1,ClickType.QUICK_MOVE,packet.itemStacks[index],0.toShort()))
                    }
                }
                41 -> {
                    guiMod = 3
                    for (index in 0..2) {
                        mc.connection!!.networkManager.sendPacket(CPacketClickWindow(packet.windowId,index,1,ClickType.QUICK_MOVE,packet.itemStacks[index],0.toShort()))
                    }
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (msTimer.hasTimePassed(200L) && hasWindow) {
            mc.connection!!.networkManager.sendPacket(CPacketCloseWindow(windowid))
            currentBlockPos = null
            hasWindow = false
            guiMod = 0
            items!!.clear()
        }
    }
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (hud.get() && hasWindow && items!!.isNotEmpty() && guiMod != 0) {
            val sr = ScaledResolution(mc)
            val x = sr.scaledWidth / 2 - 87
            val y = sr.scaledHeight / 2 + 50

            val font = mc.fontRenderer
            GL11.glPushMatrix()

            if (follow.get()) {
                glTranslated(
                        currentBlockPos!!.x + 0.5 - mc.renderManager.renderPosX,
                        currentBlockPos!!.y + 1 - mc.renderManager.renderPosY,
                        currentBlockPos!!.z + 0.5 - mc.renderManager.renderPosZ
                )
            }

            when (guiMod) {
                1 -> {
                    RenderUtils.drawRoundedRect(x.toFloat(),y.toFloat(),x + 174f,y + 66f,3f,Color(0,0,0,100).rgb,true)

//                    GL11.glPushMatrix()
                    RenderHelper.enableGUIStandardItemLighting()
                    renderInv(0, 8, x + 6, y + 6, font)
                    renderInv(9, 17, x + 6, y + 24, font)
                    renderInv(18, 26, x + 6, y + 42, font)
                    RenderHelper.disableStandardItemLighting()
                    GlStateManager.enableAlpha()
                    GlStateManager.disableBlend()
                    GlStateManager.disableLighting()
                    GlStateManager.disableCull()
//                    GL11.glPopMatrix()
                }
                2 ,3  -> {
                    RenderUtils.drawRoundedRect(x.toFloat(),y.toFloat(),x + 66f,y + 26f,3f,Color(0,0,0,100).rgb,true)

//                    GL11.glPushMatrix()
                    RenderHelper.enableGUIStandardItemLighting()
                    renderInv(0, 2, x + 6, y + 6, font)
                    RenderHelper.disableStandardItemLighting()
                    GlStateManager.enableAlpha()
                    GlStateManager.disableBlend()
                    GlStateManager.disableLighting()
                    GlStateManager.disableCull()
//                    GL11.glPopMatrix()
                }
            }
            GL11.glPopMatrix()
        }
    }

    private fun renderInv(slot: Int, endSlot: Int, x: Int, y: Int, font: FontRenderer) {
        var xOffset = x
        for (i in slot..endSlot) {
            xOffset += 18
            val stack = items!!.get(i)

            if (isUseful(stack)) {
                RenderUtils.drawRect(xOffset - 19,y - 1,xOffset - 1,y + 17,Color(0,255,0,100).rgb)
            }

            mc.renderItem.renderItemAndEffectIntoGUI(stack, xOffset - 18, y)
            mc.renderItem.renderItemOverlays(font, stack, xOffset - 18, y)
        }
    }
    private fun isUseful(itemStack: ItemStack) : Boolean {
        if (!usefulCheck.get())
            return true

        return (KevinClient.moduleManager.getModule(InventoryManager::class.java) as InventoryManager).isUseful(itemStack,-1)
    }
}