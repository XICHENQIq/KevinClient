package kevin.hud.element.elements


import blur.BlurBuffer
import kevin.hud.element.Border
import kevin.hud.element.Element
import kevin.hud.element.ElementInfo
import kevin.hud.font.FontLoader
import kevin.utils.ColorUtils
import kevin.utils.RenderUtils
import kevin.utils.render.AnimationUtils2
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*

@ElementInfo(name = "Effects")
class Effects : Element() {

    var animX = 0f
    var animY = 0f

    var currX = 0f
    var currY = 0f
    /**
     * by XiChenQi
     */
    override fun drawElement(): Border? {
        val potionMaxDurations: MutableMap<Int, Int> = HashMap()
        val timerMap: MutableMap<Potion, Double> = HashMap()
        var x = 30

        var length: Int = 0
        val tempY: Int = mc.player.activePotionEffects.size.also({ length = it }) * 30

        currX = 110f
        currY = tempY.toFloat()

        animX = AnimationUtils2.animate(currX,animX,(0.0098f * RenderUtils.deltaTime))
        animY = AnimationUtils2.animate(currY,animY,(0.0098f * RenderUtils.deltaTime))
        if (length != 0) {
            RenderUtils.drawRoundedRect(0f, 0f, animX, animY,5f,Color(0,0,0,100).rgb,true)

            GL11.glTranslated(-renderX, -renderY, 0.0)

            BlurBuffer.drawRoundRectBlur(renderX.toFloat(), renderY.toFloat(),
                animX + renderX.toFloat(), animY + renderY.toFloat(), 5f,1f)

            GL11.glTranslated(renderX, renderY , 0.0)
        }
        val needRemove = ArrayList<Int>()
        for ((key) in potionMaxDurations.entries) {
            if (mc.player.getActivePotionEffect(Potion.getPotionById(key)!!) == null) {
                needRemove.add(key)
            }
        }
        for (id in needRemove) {
            potionMaxDurations.remove(id)
        }
        for (effect in mc.player.activePotionEffects) {
            if (!potionMaxDurations.containsKey(Potion.getIdFromPotion(effect.potion)) || potionMaxDurations[Potion.getIdFromPotion(effect.potion)]!! < effect.duration) {
                potionMaxDurations.put(Potion.getIdFromPotion(effect.potion), effect.duration)
            }
            val potion = Potion.getPotionById(Potion.getIdFromPotion(effect.potion))
            val PType = I18n.format(potion!!.name, Arrays.toString(arrayOfNulls(0)))
            var minutes: Int
            var seconds: Int
            try {
                minutes = Potion.getPotionDurationString(effect,1.0f).split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt()
                seconds = Potion.getPotionDurationString(effect,1.0f).split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toInt()
            } catch (ex: Exception) {
                minutes = 0
                seconds = 0
            }
            val total = (minutes * 60 + seconds).toDouble()
            if (!timerMap.containsKey(potion)) {
                timerMap[potion] = total
            }
            if (timerMap[potion] == 0.0 || total > timerMap[potion]!!) {
                timerMap.replace(potion, total)
            }
            val color = ColorUtils.blendColors(floatArrayOf(0.0f, 0.5f, 1.0f), arrayOf(Color(250, 50, 56), Color(236, 129, 44), Color(5, 134, 105)), effect.duration / (1.0f * potionMaxDurations[Potion.getIdFromPotion(effect.potion)]!!)).rgb
            val x1 = ((120f - 6) * 1.33f).toInt()
            val y1: Int = ((30f - 52 - mc.fontRenderer.FONT_HEIGHT + x + 5) * 1.33f).toInt()
            if (potion.hasStatusIcon()) {
                GlStateManager.pushMatrix()
                GL11.glDisable(2929)
                GL11.glEnable(3042)
                GL11.glDepthMask(false)
                OpenGlHelper.glBlendFunc(770, 771, 1, 0)
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                val index = potion.statusIconIndex
                val location = ResourceLocation("textures/gui/container/inventory.png")
                mc.textureManager.bindTexture(location)
                GlStateManager.scale(0.75, 0.75, 0.75)
                mc.ingameGUI.drawTexturedModalRect(x1 - 138, y1 + 8, index % 8 * 18, 198 + index / 8 * 18, 18, 18)
                GL11.glDepthMask(true)
                GL11.glDisable(3042)
                GL11.glEnable(2929)
                GlStateManager.popMatrix()
            }
            val y2: Int = 30 - mc.fontRenderer.FONT_HEIGHT + x - 38
            /*RenderUtils.drawArc(width - 104.75f, y2 + 2.5f, 10.0, Color(22, 28, 15).rgb, 0, 360.0, 3)
            RenderUtils.drawArc(
                    width - 104.75f,
                    y2 + 2.5f,
                    10.0,
                    color,
                    0,
                    (360.0f * (effect.duration / (1.0f * potionMaxDurations.get(effect.potionID)!!))).toDouble(),
                    3
            )*/
            FontLoader.getCFont(false,20).drawString(PType.replace("§.".toRegex(), ""), 120f - 85.0f, (y2 - mc.fontRenderer.FONT_HEIGHT + 2).toFloat(), -1)
            RenderUtils.drawRect(120f - 91.0f, y2 - 3.0f, 120f - 89.5f, y2 + 10.0f, Color(255, 255, 255, 100).rgb)
            FontLoader.getCFont(false,20).drawString(Potion.getPotionDurationString(effect,1.0f).replace("§.".toRegex(), ""), 120f - 85.0f, y2 + 6.5f, color)
            x += 30
        }
        return Border(0f, 0f + tempY, 110f, 0f)
    }

}