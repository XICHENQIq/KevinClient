package kevin.utils

import java.awt.Color
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.*

object ColorUtils {

    @JvmStatic
    fun colorCode(code: String, alpha: Int=255): Color {
        when(code.toLowerCase()){
            "0" -> {
                return Color(0,0,0,alpha)
            }
            "1" -> {
                return Color(0,0,170,alpha)
            }
            "2" -> {
                return Color(0,170,0,alpha)
            }
            "3" -> {
                return Color(0,170,170,alpha)
            }
            "4" -> {
                return Color(170,0,0,alpha)
            }
            "5" -> {
                return Color(170,0,170,alpha)
            }
            "6" -> {
                return Color(255,170,0,alpha)
            }
            "7" -> {
                return Color(170,170,170,alpha)
            }
            "8" -> {
                return Color(85,85,85,alpha)
            }
            "9" -> {
                return Color(85,85,255,alpha)
            }
            "a" -> {
                return Color(85,255,85,alpha)
            }
            "b" -> {
                return Color(85,255,255,alpha)
            }
            "c" -> {
                return Color(255,85,85,alpha)
            }
            "d" -> {
                return Color(255,85,255,alpha)
            }
            "e" -> {
                return Color(255,255,85,alpha)
            }
            else -> {
                return Color(255,255,255,alpha)
            }
        }
    }
    private val startTime=System.currentTimeMillis()
    fun hslRainbow(index: Int,lowest: Float=0.41f,bigest: Float=0.58f,indexOffset: Int=300,timeSplit: Int=1500):Color{
        return Color.getHSBColor((abs(((((System.currentTimeMillis()-startTime).toInt()+index*indexOffset)/timeSplit.toFloat())%2)-1) *(bigest-lowest))+lowest,0.7f,1f)
    }
    @JvmStatic
    fun astolfoRainbow(delay: Int, offset: Int, index: Int): Color {
        var rainbowDelay = ceil((System.currentTimeMillis() + (delay * index).toLong()).toDouble()) / offset
        return Color.getHSBColor(if (((360.0.also { rainbowDelay %= it }) / 360.0).toFloat().toDouble() < 0.5) -((rainbowDelay / 360.0).toFloat()) else (rainbowDelay / 360.0).toFloat(), 0.5f, 1.0f)
    }
    fun fade(color: Color, index: Int, count: Int): Color {
        val hsb = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, hsb)
        var brightness =
                abs(((System.currentTimeMillis() % 2000L).toFloat() / 1000.0f + index.toFloat() / count.toFloat() * 2.0f) % 2.0f - 1.0f)
        brightness = 0.5f + 0.5f * brightness
        hsb[2] = brightness % 2.0f
        return Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]))
    }
    fun getHealthColor(health: Float, maxHealth: Float): Int {
        var health = health
        if (health > 20) {
            health = 20f
        }
        val fractions = floatArrayOf(0f, 0.5f, 1f)
        val colors = arrayOf(Color.RED, Color.YELLOW, Color.GREEN)
        val progress = health * 5 * 0.01f
        val customColor = blendColors(fractions, colors, progress)!!.brighter()
        return customColor.rgb
    }
    fun getHealthColor2(health: Float, maxHealth: Float): Color {
        var health = health
        if (health > 20) {
            health = 20f
        }
        val fractions = floatArrayOf(0f, 0.5f, 1f)
        val colors = arrayOf(Color.RED, Color.YELLOW, Color.GREEN)
        val progress = health * 5 * 0.01f
        val customColor = blendColors(fractions, colors, progress)!!.brighter()
        return customColor
    }
    fun blendColors(fractions: FloatArray, colors: Array<Color?>, progress: Float): Color {
        if (fractions.size == colors.size) {
            val indices: IntArray = getFractionIndices(fractions, progress)
            val range = floatArrayOf(fractions[indices[0]], fractions[indices[1]])
            val colorRange = arrayOf(colors[indices[0]], colors[indices[1]])
            val max = range[1] - range[0]
            val value = progress - range[0]
            val weight = value / max
            val color: Color = blend(colorRange[0]!!, colorRange[1]!!, (1.0f - weight).toDouble())
            return color
        } else {
            throw IllegalArgumentException("Fractions and colours must have equal number of elements")
        }
    }
    fun getFractionIndices(fractions: FloatArray, progress: Float): IntArray {
        val range = IntArray(2)

        var startPoint: Int
        startPoint = 0
        while (startPoint < fractions.size && fractions[startPoint] <= progress) {
            ++startPoint
        }

        if (startPoint >= fractions.size) {
            startPoint = fractions.size - 1
        }

        range[0] = startPoint - 1
        range[1] = startPoint
        return range
    }
    fun blend(color1: Color, color2: Color, ratio: Double): Color {
        val r = ratio.toFloat()
        val ir = 1.0f - r
        val rgb1 = color1.getColorComponents(FloatArray(3))
        val rgb2 = color2.getColorComponents(FloatArray(3))
        var red = rgb1[0] * r + rgb2[0] * ir
        var green = rgb1[1] * r + rgb2[1] * ir
        var blue = rgb1[2] * r + rgb2[2] * ir
        if (red < 0.0f) {
            red = 0.0f
        } else if (red > 255.0f) {
            red = 255.0f
        }

        if (green < 0.0f) {
            green = 0.0f
        } else if (green > 255.0f) {
            green = 255.0f
        }

        if (blue < 0.0f) {
            blue = 0.0f
        } else if (blue > 255.0f) {
            blue = 255.0f
        }

        var color3: Color? = null

        try {
            color3 = Color(red, green, blue)
        } catch (var13: java.lang.IllegalArgumentException) {
        }

        return color3!!
    }

    @JvmStatic
    fun healthColor(hp: Float,maxHP: Float, alpha: Int=255):Color{
        val pct=((hp/maxHP)*255F).toInt()
        return Color(max(min(255-pct, 255),0), max(min(pct, 255),0), 0, alpha)
    }

    /** Array of the special characters that are allowed in any text drawing of Minecraft.  */
    val allowedCharactersArray = charArrayOf('/', '\n', '\r', '\t', '\u0000', '', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')

    fun isAllowedCharacter(character: Char): Boolean {
        return character.toInt() != 167 && character.toInt() >= 32 && character.toInt() != 127
    }

    @JvmStatic
    fun reAlpha(color: Color,alpha: Int): Color{
        return Color(color.red,color.green,color.blue,alpha)
    }

    @JvmStatic
    fun slowlyRainbow(time: Long, count: Int, qd: Float, sq: Float): Color {
        val color = Color(Color.HSBtoRGB((time.toFloat() + count * -3000000f) / 2 / 1.0E9f, qd, sq))
        return Color(color.red / 255.0f * 1, color.green / 255.0f * 1, color.blue / 255.0f * 1, color.alpha / 255.0f)
    }

    @JvmStatic
    fun rainbowWithAlpha(alpha: Int) = reAlpha(rainbow(),alpha)

    private val COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]")

    @JvmField
    val hexColors = IntArray(16)

    init {
        repeat(16) { i ->
            val baseColor = (i shr 3 and 1) * 85

            val red = (i shr 2 and 1) * 170 + baseColor + if (i == 6) 85 else 0
            val green = (i shr 1 and 1) * 170 + baseColor
            val blue = (i and 1) * 170 + baseColor

            hexColors[i] = red and 255 shl 16 or (green and 255 shl 8) or (blue and 255)
        }
    }

    @JvmStatic
    fun stripColor(input: String?): String? {
        return COLOR_PATTERN.matcher(input ?: return null).replaceAll("")
    }

    @JvmStatic
    fun translateAlternateColorCodes(textToTranslate: String): String {
        val chars = textToTranslate.toCharArray()

        for (i in 0 until chars.size - 1) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(chars[i + 1], true)) {
                chars[i] = '§'
                chars[i + 1] = Character.toLowerCase(chars[i + 1])
            }
        }

        return String(chars)
    }

    fun randomMagicText(text: String): String {
        val stringBuilder = StringBuilder()
        val allowedCharacters = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000"

        for (c in text.toCharArray()) {
            if (isAllowedCharacter(c)) {
                val index = Random().nextInt(allowedCharacters.length)
                stringBuilder.append(allowedCharacters.toCharArray()[index])
            }
        }

        return stringBuilder.toString()
    }

    @JvmStatic
    fun rainbow(): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + 400000L) / 10000000000F % 1, 1F, 1F))
        return Color(currentColor.red / 255F * 1F, currentColor.green / 255f * 1F, currentColor.blue / 255F * 1F, currentColor.alpha / 255F)
    }

    // TODO: Use kotlin optional argument feature

    @JvmStatic
    fun rainbow(offset: Long): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 10000000000F % 1, 1F, 1F))
        return Color(currentColor.red / 255F * 1F, currentColor.green / 255F * 1F, currentColor.blue / 255F * 1F,
            currentColor.alpha / 255F)
    }

    @JvmStatic
    fun rainbow(alpha: Float) = rainbow(400000L, alpha)

    @JvmStatic
    fun rainbow(alpha: Int) = rainbow(400000L, alpha)

    @JvmStatic
    fun rainbow(offset: Long, alpha: Int) = rainbow(offset, alpha.toFloat() / 255)

    @JvmStatic
    fun rainbow(offset: Long, alpha: Float): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 10000000000F % 1, 1F, 1F))
        return Color(currentColor.red / 255F * 1F, currentColor.green / 255f * 1F, currentColor.blue / 255F * 1F, alpha)
    }
}