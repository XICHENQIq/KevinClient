package kevin.hud.element.elements

import blur.BlurBuffer
import kevin.hud.element.Border
import kevin.hud.element.Element
import kevin.hud.element.ElementInfo
import kevin.hud.element.Side
import kevin.hud.font.FontLoader
import kevin.utils.RenderUtils
import kevin.utils.render.AnimationUtils2
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * CustomHUD Armor element
 * Shows a horizontal display of current armor
 * by XiChenQi
 */
@ElementInfo(name = "Armor")
class Armor(x: Double = -8.0, y: Double = 57.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    var animX = 0f
    var animY = 0f

    var currX = 0f
    var currY = 0f

    /**
     * Draw element
     */
    override fun drawElement(): Border {
        GL11.glPushMatrix()

        val renderItem = mc.renderItem

        var x = 1
        var y = 0


        var height = 0

        for(index in 3 downTo 0){
            val stack = mc.player.inventory.armorInventory[index]
            if (stack != null && stack.maxDamage > 0){
                height += 18
            }
        }
        currX = x + 52f
        currY = y + height + 5f

        animX = AnimationUtils2.animate(currX,animX,(0.0098f * RenderUtils.deltaTime))
        animY = AnimationUtils2.animate(currY,animY,(0.0098f * RenderUtils.deltaTime))

        if (height != 0) {

            RenderUtils.drawRoundedRect(x - 6f,y - 5f,animX,animY,5f,Color(0,0,0,80).rgb,true)

            GL11.glTranslated(-renderX, -renderY, 0.0)

            BlurBuffer.drawRoundRectBlur(x - 6f + renderX.toFloat(), y - 5f + renderY.toFloat(),
                animX + renderX.toFloat(), animY + renderY.toFloat(), 5f,1f)

            GL11.glTranslated(renderX, renderY , 0.0)
        }

        for (index in 3 downTo 0) {
            val stack = mc.player.inventory.armorInventory[index]

            if (stack != null && stack.maxDamage > 0) {
                renderItem.renderItemIntoGUI(stack, x, y)
                renderItem.renderItemOverlays(mc.fontRenderer, stack, x, y)

                GL11.glColor4f(255f, 255f, 255f, 255f)
                FontLoader.getCFont(false,20).drawString(
                        (100 - ((stack.itemDamage * 100) / stack.maxDamage)).toString() + "%",
                        x + 20, y + 6, Color(255, 255, 255, 255).rgb
                )
                y += 18
            }
        }

        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()

        return Border(-5f,-5f,53f,77f)
    }
}