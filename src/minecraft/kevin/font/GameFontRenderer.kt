package kevin.font

import kevin.utils.ColorUtils
import kevin.utils.RenderUtils
import kevin.utils.rainbow.RainbowFontShader
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.awt.Color
import java.awt.Font

class GameFontRenderer(font: Font) {
    val fontHeight: Int
    var defaultFont = AWTFontRenderer(font)
    private var boldFont = AWTFontRenderer(font.deriveFont(Font.BOLD))
    private var italicFont = AWTFontRenderer(font.deriveFont(Font.ITALIC))
    private var boldItalicFont = AWTFontRenderer(font.deriveFont(Font.BOLD or Font.ITALIC))
    val height: Int
        get() = defaultFont.height / 2
    val size: Int
        get() = defaultFont.font.size
    init {
        fontHeight = height
    }
    fun drawString(s: String?, x: Float, y: Float, color: Int) = drawString(s, x, y, color, false)
    fun drawStringWithShadow(text: String?, x: Float, y: Float, color: Int) = drawString(text, x, y, color, true)
    fun drawCenteredString(s: String, x: Float, y: Float, color: Int, shadow: Boolean) = drawString(s, x - getStringWidth(s) / 2F, y, color, shadow)
    fun drawCenteredString(s: String, x: Float, y: Float, color: Int) =
        drawStringWithShadow(s, x - getStringWidth(s) / 2F, y, color)
    fun drawString(text: String?, x: Float, y: Float, color: Int, shadow: Boolean): Int {
        val currentText = text
        val currY = y - 3F
        val rainbow = RainbowFontShader.isInUse
        if (shadow) {
            GL20.glUseProgram(0)

            drawText(currentText, x + 1f, currY + 1f, Color(0, 0, 0, 150).rgb, true)
        }
        return drawText(currentText, x, currY, color, false, rainbow)
    }
    private fun drawText(text: String?, x: Float, y: Float, color: Int, ignoreColor: Boolean, rainbow: Boolean = false): Int {
        if (text == null)
            return 0
        if (text.isNullOrEmpty())
            return x.toInt()
        val rainbowShaderId = RainbowFontShader.programId
        if (rainbow)
            GL20.glUseProgram(rainbowShaderId)
        GL11.glTranslated(x - 1.5, y + 0.5, 0.0)
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.enableTexture2D()
        var currentColor = color
        if (currentColor and -0x4000000 == 0)
            currentColor = currentColor or -16777216
        val defaultColor = currentColor
        val alpha: Int = (currentColor shr 24 and 0xff)
        if (text.contains("§")) {
            val parts = text.split("§")
            var currentFont = defaultFont
            var width = 0.0
            // Color code states
            var randomCase = false
            var bold = false
            var italic = false
            var strikeThrough = false
            var underline = false
            parts.forEachIndexed { index, part ->
                if (part.isEmpty())
                    return@forEachIndexed
                if (index == 0) {
                    currentFont.drawString(part, width, 0.0, currentColor)
                    width += currentFont.getStringWidth(part)
                } else {
                    val words = part.substring(1)
                    val type = part[0]
                    when (val colorIndex = getColorIndex(type)) {
                        in 0..15 -> {
                            if (!ignoreColor) {
                                currentColor = ColorUtils.hexColors[colorIndex] or (alpha shl 24)

                                if (rainbow)
                                    GL20.glUseProgram(0)
                            }
                            bold = false
                            italic = false
                            randomCase = false
                            underline = false
                            strikeThrough = false
                        }
                        16 -> randomCase = true
                        17 -> bold = true
                        18 -> strikeThrough = true
                        19 -> underline = true
                        20 -> italic = true
                        21 -> {
                            currentColor = color

                            if (currentColor and -67108864 == 0)
                                currentColor = currentColor or -16777216

                            if (rainbow)
                                GL20.glUseProgram(rainbowShaderId)

                            bold = false
                            italic = false
                            randomCase = false
                            underline = false
                            strikeThrough = false
                        }
                    }

                    currentFont = if (bold && italic)
                        boldItalicFont
                    else if (bold)
                        boldFont
                    else if (italic)
                        italicFont
                    else
                        defaultFont

                    currentFont.drawString(if (randomCase) ColorUtils.randomMagicText(words) else words, width, 0.0, currentColor)

                    if (strikeThrough)
                        RenderUtils.drawLine(width / 2.0 + 1, currentFont.height / 3.0,
                            (width + currentFont.getStringWidth(words)) / 2.0 + 1, currentFont.height / 3.0,
                            fontHeight / 16F)

                    if (underline)
                        RenderUtils.drawLine(width / 2.0 + 1, currentFont.height / 2.0,
                            (width + currentFont.getStringWidth(words)) / 2.0 + 1, currentFont.height / 2.0,
                            fontHeight / 16F)

                    width += currentFont.getStringWidth(words)
                }
            }
        } else {
            // Color code states
            defaultFont.drawString(text, 0.0, 0.0, currentColor)
        }
        GlStateManager.disableBlend()
        GL11.glTranslated(-(x - 1.5), -(y + 0.5), 0.0)
        GL11.glColor4f(1f, 1f, 1f, 1f)

        return (x + getStringWidth(text)).toInt()
    }
    fun getColorCode(charCode: Char) =
        ColorUtils.hexColors[getColorIndex(charCode)]
    fun getStringWidth(text: String?): Int {
        return if (text!!.contains("§")) {
            val parts = text.split("§")
            var currentFont = defaultFont
            var width = 0
            var bold = false
            var italic = false
            parts.forEachIndexed { index, part ->
                if (part.isEmpty())
                    return@forEachIndexed
                if (index == 0) {
                    width += currentFont.getStringWidth(part)
                } else {
                    val words = part.substring(1)
                    val type = part[0]
                    val colorIndex = getColorIndex(type)
                    when {
                        colorIndex < 16 -> {
                            bold = false
                            italic = false
                        }

                        colorIndex == 17 -> bold = true
                        colorIndex == 20 -> italic = true
                        colorIndex == 21 -> {
                            bold = false
                            italic = false
                        }
                    }
                    currentFont = if (bold && italic)
                        boldItalicFont
                    else if (bold)
                        boldFont
                    else if (italic)
                        italicFont
                    else
                        defaultFont

                    width += currentFont.getStringWidth(words)
                }
            }
            width / 2
        } else
            defaultFont.getStringWidth(text) / 2
    }
    fun getCharWidth(character: Char) = getStringWidth(character.toString())
    companion object {
        @JvmStatic
        fun getColorIndex(type: Char): Int {
            return when (type) {
                in '0'..'9' -> type - '0'
                in 'a'..'f' -> type - 'a' + 10
                in 'k'..'o' -> type - 'k' + 16
                'r' -> 21
                else -> -1
            }
        }
    }
}