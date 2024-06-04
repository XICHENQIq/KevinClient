package kevin.module.modules.render

import kevin.KevinClient
import kevin.event.EventTarget
import kevin.event.Render3DEvent
import kevin.font.GameFontRenderer.Companion.getColorIndex
import kevin.module.*
import kevin.module.modules.misc.KillerDetector
import kevin.utils.*
import kevin.utils.ColorUtils.rainbow
import kevin.utils.RenderUtils.glColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.resources.I18n
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemAir
import net.minecraft.util.math.AxisAlignedBB
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.vector.Vector3f
import java.awt.Color
import java.util.*
import kotlin.math.max
import kotlin.math.min


class ESP : Module("ESP", "Allows you to see targets through walls.", category = ModuleCategory.RENDER) {
    @JvmField
    val modeValue = ListValue("Mode", arrayOf("Box","2D","Outline","WireFrame"),"Box")
    private val colorRedValue = IntegerValue("R", 255, 0, 255)
    private val colorGreenValue = IntegerValue("G", 255, 0, 255)
    private val colorBlueValue = IntegerValue("B", 255, 0, 255)
    private val colorRainbow = BooleanValue("Rainbow", false)
    private val colorTeam = BooleanValue("Team", false)
    override val tag: String
        get() = modeValue.get()
    @JvmField
    val outlineWidth = FloatValue("OutlineWidth", 3f, 0.5f, 5f)
    @JvmField
    val wireframeWidth = FloatValue("WireFrameWidth", 2f, 0.5f, 5f)
    val autoWidth = BooleanValue("AutoWidth",true)

    @EventTarget
    fun onRender3D(event: Render3DEvent){
        val mode = modeValue.get()
        if (mode !in arrayOf("Box","2D")) return
        val mvMatrix = WorldToScreen.getMatrix(GL_MODELVIEW_MATRIX)
        val projectionMatrix = WorldToScreen.getMatrix(GL_PROJECTION_MATRIX)
        if (mode == "2D"){
            glPushAttrib(GL_ENABLE_BIT)
            glEnable(GL_BLEND)
            glDisable(GL_TEXTURE_2D)
            glDisable(GL_DEPTH_TEST)
            glMatrixMode(GL_PROJECTION)
            glPushMatrix()
            glLoadIdentity()
            glOrtho(0.0, mc.displayWidth.toDouble(), mc.displayHeight.toDouble(), 0.0, -1.0, 1.0)
            glMatrixMode(GL_MODELVIEW)
            glPushMatrix()
            glLoadIdentity()
            glDisable(GL_DEPTH_TEST)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.enableTexture2D()
            glDepthMask(true)
            glLineWidth(1.0f)
        }
        for (entity in mc.world.loadedEntityList){
            if (!EntityUtils.isSelected(entity, false) || entity !is EntityLivingBase) continue
            val color = getColor(entity)
            when(mode){
                "Box" -> drawEntityBox(entity,color)
                "2D" -> {
                    val renderManager = mc.renderManager
                    val timer = mc.timer
                    val bb = entity.entityBoundingBox
                            .offset(-entity.posX, -entity.posY, -entity.posZ)
                            .offset(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks,
                                    entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks,
                                    entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks)
                            .offset(-renderManager.renderPosX, -renderManager.renderPosY, -renderManager.renderPosZ)
                    val boxVertices = arrayOf(doubleArrayOf(bb.minX , bb.minY, bb.minZ ), doubleArrayOf(bb.minX, bb.maxY + 0.1, bb.minZ ), doubleArrayOf(bb.maxX , bb.maxY + 0.1, bb.minZ ), doubleArrayOf(bb.maxX , bb.minY, bb.minZ ), doubleArrayOf(bb.minX , bb.minY, bb.maxZ ), doubleArrayOf(bb.minX, bb.maxY + 0.1, bb.maxZ), doubleArrayOf(bb.maxX , bb.maxY + 0.1, bb.maxZ), doubleArrayOf(bb.maxX , bb.minY, bb.maxZ ))
                    var minX = Float.MAX_VALUE
                    var minY = Float.MAX_VALUE
                    var maxX = -1f
                    var maxY = -1f
                    for (boxVertex in boxVertices) {
                        val screenPos = WorldToScreen.worldToScreen(Vector3f(boxVertex[0].toFloat(), boxVertex[1].toFloat(), boxVertex[2].toFloat()), mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight)
                                ?: continue
                        minX = min(screenPos.x, minX)
                        minY = min(screenPos.y, minY)
                        maxX = max(screenPos.x, maxX)
                        maxY = max(screenPos.y, maxY)
                    }
                    if (minX > 0 || minY > 0 || maxX <= mc.displayWidth || maxY <= mc.displayWidth) {


                            glColor4f(0F, 0F, 0F, 1.0f)
                            glBegin(GL11.GL_QUADS)
                            glVertex2f(minX - 8F, minY - 1F)
                            glVertex2f(minX - 8F, maxY + 1F)
                            glVertex2f(minX - 3F, maxY + 1F)
                            glVertex2f(minX - 3F, minY - 1F)
                            glEnd()

                            glColor4f(0.2F, 0.2F, 0.2F, 1.0f)
                            glBegin(GL11.GL_QUADS)
                            glVertex2f(minX - 7F, minY)
                            glVertex2f(minX - 7F, maxY)
                            glVertex2f(minX - 4F, maxY)
                            glVertex2f(minX - 4F, minY)
                            glEnd()

                            RenderUtils.glColorHex(ColorUtils.getHealthColor(entity.health, entity.maxHealth))
                            //GL11.glColor4f(0F, 1F, 0F, 1.0f)
                            glBegin(GL11.GL_QUADS)
                            glVertex2f(minX - 7F, maxY - (entity.health / entity.maxHealth) * (maxY - minY))
                            glVertex2f(minX - 7F, maxY)
                            glVertex2f(minX - 4F, maxY)
                            glVertex2f(minX - 4F, maxY - (entity.health / entity.maxHealth) * (maxY - minY))
                            glEnd()



                            glColor4f(0F, 0F, 0F, 1.0f)
                            GL11.glLineWidth(1F);
                            glBegin(GL11.GL_LINE_LOOP)
                            glVertex2f(minX - 1F, minY - 1F)
                            glVertex2f(minX - 1F, maxY + 1F)
                            glVertex2f(maxX + 1F, maxY + 1F)
                            glVertex2f(maxX + 1F, minY - 1F)
                            glEnd()

                            glColor4f(0F, 0F, 0F, 1.0f)
                            GL11.glLineWidth(1F);
                            glBegin(GL11.GL_LINE_LOOP)
                            glVertex2f(minX + 1F, minY + 1F)
                            glVertex2f(minX + 1F, maxY - 1F)
                            glVertex2f(maxX - 1F, maxY - 1F)
                            glVertex2f(maxX - 1F, minY + 1F)
                            glEnd()


                        glColor4f(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, 1.0f)
                        GL11.glLineWidth(1F);
                        glBegin(GL11.GL_LINE_LOOP)
                        glVertex2f(minX, minY)
                        glVertex2f(minX, maxY)
                        glVertex2f(maxX, maxY)
                        glVertex2f(maxX, minY)
                        glEnd()


                            // Name
                            GL11.glEnable(GL11.GL_TEXTURE_2D)
                            GL11.glEnable(GL11.GL_DEPTH_TEST)
                            val effects = entity.activePotionEffects
                            if (!effects.isEmpty()) {
                                mc.fontRenderer.drawCenteredString(entity.name!!, minX + (maxX - minX) / 2.0f, minY - 2.0f - mc.fontRenderer.FONT_HEIGHT, Color(255,0,0,255).rgb, true)
                            } else {
                                mc.fontRenderer.drawCenteredString(entity.name!!, minX + (maxX - minX) / 2.0f, minY - 2.0f - mc.fontRenderer.FONT_HEIGHT, -1, true)
                            }
                            GL11.glDisable(GL11.GL_TEXTURE_2D)
                            GL11.glDisable(GL11.GL_DEPTH_TEST)

                            //HeldItem
                            GL11.glEnable(GL11.GL_TEXTURE_2D)
                            GL11.glEnable(GL11.GL_DEPTH_TEST)
                            if (entity.heldItemMainhand.item !is ItemAir) {
                                mc.fontRenderer.drawCenteredString(
                                        entity.heldItemMainhand.displayName,
                                        minX + (maxX - minX) / 2.0f,
                                        maxY + 2.0f,
                                        -1,
                                        true
                                )
                            }
                            GL11.glDisable(GL11.GL_TEXTURE_2D)
                            GL11.glDisable(GL11.GL_DEPTH_TEST)

                            //Effects
                            GL11.glEnable(GL11.GL_TEXTURE_2D)
                            GL11.glEnable(GL11.GL_DEPTH_TEST)
                            var effectsY = 0
                            if (!entity.activePotionEffects.isEmpty()) {
                                for (effects2 in entity.activePotionEffects) {
                                    mc.fontRenderer.drawString(
                                            I18n.format(effects2.potion.name, Arrays.toString(arrayOfNulls(0))) + " " + effects2.duration,
                                            maxX + 4,
                                            minY + effectsY,
                                            Color(255, 255, 255).rgb,
                                            true
                                    )
                                    effectsY += 10
                                }
                            }
                            GL11.glDisable(GL11.GL_TEXTURE_2D)
                            GL11.glDisable(GL11.GL_DEPTH_TEST)

                    }
                }
            }
        }
        if (mode == "2D"){
            glEnable(GL_DEPTH_TEST)
            glMatrixMode(GL_PROJECTION)
            glPopMatrix()
            glMatrixMode(GL_MODELVIEW)
            glPopMatrix()
            glPopAttrib()
        }
    }
    private fun drawEntityBox(entity: EntityLivingBase,color: Color){
        val renderManager = mc.renderManager
        val timer = mc.timer
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        val x: Double =
            (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                    - renderManager.renderPosX)
        val y: Double =
            (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                    - renderManager.renderPosY)
        val z: Double =
            (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                    - renderManager.renderPosZ)
        val entityBox: AxisAlignedBB = entity.entityBoundingBox
        val axisAlignedBB = AxisAlignedBB(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )
        glLineWidth(1f)
        glEnable(GL_LINE_SMOOTH)
        glColor(color.red, color.green, color.blue, 95)
        drawSelectionBoundingBox(axisAlignedBB)
        glColor(color.red, color.green, color.blue,26)
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()

        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        tessellator.draw()
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
    }
    fun getColor(entity: EntityLivingBase): Color{
        run {
            if (entity.hurtTime > 0)
                    return Color.RED
            val killerDetector = KevinClient.moduleManager.getModule("KillerDetector") as KillerDetector
            if (killerDetector.state && entity ==killerDetector.killer)
                return Color.RED
                if (entity is EntityPlayer && KevinClient.isFriend(entity.name))
                    return Color.BLUE

                if (colorTeam.get()) {
                    val chars: CharArray = (entity.displayName ?: return@run).formattedText.toCharArray()
                    var color = Int.MAX_VALUE
                    for (i in chars.indices) {
                        if (chars[i] != '§' || i + 1 >= chars.size) continue
                        val index = getColorIndex(chars[i + 1])
                        if (index < 0 || index > 15) continue
                        color = ColorUtils.hexColors[index]
                        break
                    }
                    return Color(color)
                }
        }
        return if (colorRainbow.get()) rainbow() else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
        //return if (entity.hurtTime > 0) Color.RED else if (colorRainbow.get()) rainbow() else Color(colorRedValue.get(),colorGreenValue.get(),colorBlueValue.get())
    }
    companion object {
        @JvmField
        var renderNameTags = true
    }
}