/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package kevin.hud.element.elements

import kevin.KevinClient
import kevin.font.AWTFontRenderer
import kevin.hud.designer.GuiHudDesigner
import kevin.hud.element.Border
import kevin.hud.element.Element
import kevin.hud.element.ElementInfo
import kevin.hud.element.Side
import kevin.hud.element.Side.Horizontal.*
import kevin.hud.font.FontLoader
import kevin.module.*
import kevin.utils.render.AnimationUtils
import kevin.utils.ColorUtils
import kevin.utils.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 *
 * by XiChenQi
 */
@ElementInfo(name = "Arraylist", single = true)
class Arraylist(x: Double = 1.0, y: Double = 2.0, scale: Float = 1F, side: Side = Side(RIGHT, Side.Vertical.UP)) :
    Element(x, y, scale, side) {
    private val noRenderModule = BooleanValue("NoRenderModule", false)
    private val colorRedValue = IntegerValue("R", 0, 0, 255)
    private val colorGreenValue = IntegerValue("G", 111, 0, 255)
    private val colorBlueValue = IntegerValue("B", 255, 0, 255)
    val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "AnotherRainbow", "SkyRainbow"), "Custom")
    private val tag = BooleanValue("Tag", true)
    private val LogoValue = BooleanValue("Logo", true)
    private val backgroundValue = BooleanValue("Background", true)
    private val shadow = BooleanValue("ShadowText", true)
    private val spaceValue = FloatValue("Space", 0F, 0F, 5F)
    private val textHeightValue = FloatValue("TextHeight", 11F, 1F, 20F)
    private val textYValue = FloatValue("TextY", 1F, 0F, 20F)

    private var x2 = 0
    private var y2 = 0F

    private var modules = emptyList<Module>()


    override fun drawElement(): Border? {
        val fontRenderer = FontLoader.getCFont(true, 20)

        AWTFontRenderer.assumeNonVolatile = true

        val delta = RenderUtils.deltaTime

        for (module in KevinClient.moduleManager.modules) {
            if (!module.array || (!module.state && module.slide == 0F)) continue

            val displayString = (module.name + if (tag.get()) module.tag else "")

            val width = fontRenderer.getStringWidth(displayString)

            if (module.state) {
                if (module.slide < width) {
                    module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                    module.slideStep += delta / 4F
                }
            } else if (module.slide > 0) {
                module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                module.slideStep -= delta / 4F
            }

            module.slide = module.slide.coerceIn(0F, width.toFloat())
            module.slideStep = module.slideStep.coerceIn(0F, width.toFloat())
        }

        // Draw arraylist
        val space = spaceValue.get()
        val textHeight = textHeightValue.get()
        val textY = textYValue.get()
        val backgroundCustomColor = Color(0, 0, 0, 100).rgb
        val textShadow = shadow.get()
        val textSpacer = textHeight + space
        val customColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), 255)

        modules.forEachIndexed { index, module ->
            val displayString = module.name

            val xPos = -module.slide - 2
            val yPos = (if (side.vertical == Side.Vertical.DOWN) -textSpacer else textSpacer) *
                    if (side.vertical == Side.Vertical.DOWN) index + 1 else index

            val size = modules.size * 2.0E-2f
            if (module.state) {
                if (module.higt < yPos) {
                    module.higt += (size -
                            Math.min(
                                module.higt * 0.002f, size - (module.higt * 0.0001f)
                            )) * delta
                    module.higt = Math.min(yPos, module.higt)
                } else {
                    module.higt -= (size -
                            Math.min(
                                module.higt * 0.002f, size - (module.higt * 0.0001f)
                            )) * delta
                    module.higt = Math.max(module.higt, yPos)
                }
            }
            if (backgroundValue.get()) {
                if (index == 0) {
                    // first
                    RenderUtils.drawRoundedRect2(
                        xPos - 10f,
                        module.higt,
                        -3f,
                        module.higt + textHeight,
                        3f,
                        false,
                        true,
                        true,
                        if (fontRenderer.getStringWidth(modules[0].name) - fontRenderer.getStringWidth(modules[1].name) >= 3f) true else false,
                        backgroundCustomColor
                    )
                } else if (index == modules.size - 1) {
                    // last
                    RenderUtils.drawRoundedRect2(
                        xPos - 10f,
                        module.higt,
                        -3f,
                        module.higt + textHeight,
                        3f,
                        true,
                        false,
                        false,
                        true,
                        backgroundCustomColor
                    )
                } else {
                    RenderUtils.drawRoundedRect2(
                        xPos - 10f,
                        module.higt,
                        -3f,
                        module.higt + textHeight,
                        3f,
                        false,
                        false,
                        false,
                        if (fontRenderer.getStringWidth(modules[index].name) - fontRenderer.getStringWidth(modules[index + 1].name) >= 3f) true else false,
                        backgroundCustomColor
                    )
                }
                RenderUtils.drawRect(-3F, module.higt, 1F, module.higt + textHeight, customColor)

            }
            val textColor = when (colorModeValue.get().toLowerCase()) {
                "rainbow" -> ColorUtils.hslRainbow(
                    1,
                    indexOffset = 100 * 10
                ).rgb

                "skyrainbow" -> RenderUtils.skyRainbow(1, 1F, 1F).rgb
                "anotherrainbow" -> ColorUtils.fade(customColor, 100, 1).rgb
                else -> customColor.rgb
            }
            fontRenderer.drawString(
                displayString,
                xPos - 5,
                module.higt + textY,
                textColor,
                textShadow
            )
        }
        if (LogoValue.get()) {
            val text = KevinClient.clientName + "- " + KevinClient.version

            FontLoader.getCFont(true, 22).drawString(
                text,
                -1f - FontLoader.getCFont(true, 22).getStringWidth(text),
                -10f, customColor.rgb,
                true
            )
        }

        if (mc.currentScreen is GuiHudDesigner) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return Border(0F, -1F, -20F, 20F)
            }

            for (module in modules) {
                when (side.horizontal) {
                    RIGHT, MIDDLE -> {
                        val xPos = -module.slide.toInt() - 2
                        if (x2 == Int.MIN_VALUE || xPos < x2) x2 = xPos
                    }

                    LEFT -> TODO()
                }
            }
            y2 = textSpacer * modules.size

            return Border(0F, 0F, x2 - 7F, y2 - if (side.vertical == Side.Vertical.DOWN) 1F else 0F)
        }

        AWTFontRenderer.assumeNonVolatile = false
        GlStateManager.resetColor()
        return null
    }

    override fun updateElement() {
        modules = KevinClient.moduleManager.modules
            .filter { if (it.category == ModuleCategory.RENDER && noRenderModule.get()) false else true }
            .filter { it.array && it.slide > 0 }
            .sortedBy { -FontLoader.getCFont(true, 20).getStringWidth(it.name + if (tag.get()) "§7" + it.tag else "") }
    }
}