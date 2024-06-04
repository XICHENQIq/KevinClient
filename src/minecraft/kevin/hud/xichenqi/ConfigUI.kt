package kevin.hud.xichenqi

import blur.BlurBuffer
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kevin.KevinClient
import kevin.file.ConfigManager
import kevin.hud.font.FontLoader
import kevin.milk.utils.StencilUtil
import kevin.milk.utils.key.ClickUtils
import kevin.module.modules.render.ConfigsManager
import kevin.utils.ChatUtils
import kevin.utils.RenderUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.File
import java.io.IOException

class ConfigUI : GuiScreen() {


    var x = 0f

    var y = 0f

    val c24 = FontLoader.getCFont(true,24)
    val c12 = FontLoader.getCFont(true,12)
    val c30 = FontLoader.getCFont(true,30)

    var scroll = 0f

    var mouseX = 0
    var mouseY = 0

    var lastClickX = 0
    var lastClickY = 0
    var startX = 0
    var startY = 0

    var dragging = false

    var currConfig: File ? = null

    val configsManager = KevinClient.moduleManager.getModule(ConfigsManager::class.java) as ConfigsManager


    private val input: GuiTextField by lazy {
        GuiTextField(1,mc.fontRenderer,(x + 222).toInt(),(y + 22).toInt(),146,20)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        //GaussianBlur.renderBlur(2);
        if (dragging) {
            x = (lastClickX + mouseX - startX).toFloat()
            y = (lastClickY + mouseY - startY).toFloat()
        }
        this.mouseX = mouseY
        this.mouseY = mouseY

        var bgColor = 0
        var bgColorWhenBlur = 0
        var clientNameColor = 0
        var buttonColor = 0
        var buttonTextColor = 0
        var configsBGColor = 0

        var configSelectBgColor = 0
        var configNoSelectBgColor = 0

        var configTextColor = 0


        when (configsManager.colorMode.get()) {
            "Dark" -> {
                bgColorWhenBlur = Color(50,50,50,180).rgb
                bgColor = Color(50,50,50,255).rgb
                clientNameColor = Color(255,255,255,255).rgb
                buttonColor = Color(20,20,20,255).rgb
                buttonTextColor = Color(200,200,200,255).rgb
                configsBGColor = Color(0,0,0,100).rgb

                configSelectBgColor = Color(0,0,0,100).rgb
                configNoSelectBgColor = Color(100,100,100,100).rgb

                configTextColor = Color(200,200,200,255).rgb
            }
            "Light" -> {
                bgColorWhenBlur = Color(230,230,230,180).rgb
                bgColor = Color(230,230,230,255).rgb
                clientNameColor = Color(0,0,0,255).rgb
                buttonColor = Color(180,180,180,255).rgb
                buttonTextColor = Color(50,50,50,255).rgb
                configsBGColor = Color(0,0,0,100).rgb

                configSelectBgColor = Color(200,200,200,100).rgb
                configNoSelectBgColor = Color(230,230,230,200).rgb

                configTextColor = Color(50,50,50,255).rgb
            }
        }
        input.x = (x + 222).toInt()
        input.y = (y + 22).toInt()

        // bg
        if (configsManager.blur.get()) {
            RenderUtils.drawRoundedRect(x,y,x + 400f,y + 300f,6f,bgColorWhenBlur,true)
            BlurBuffer.drawRoundRectBlur(x,y,x + 400f,y + 300f,6f,1f)
        } else {
            RenderUtils.drawRoundedRect(x,y,x + 400f,y + 300f,6f,bgColor,true)
        }


        // input
        input.drawTextBox()

        // client name
        c30.drawString(KevinClient.clientName,x + 24f,y + 28f,clientNameColor)
        c12.drawString(KevinClient.version,x + 65f,y + 14f,clientNameColor)

        // button
        RenderUtils.drawRoundedRect(x + 20,y + 67,x + 95,y + 100,3f,buttonColor,true)
        RenderUtils.drawRoundedRect(x + 20,y + 114,x + 95,y + 147,3f,buttonColor,true)
        RenderUtils.drawRoundedRect(x + 20,y + 165,x + 95,y + 198,3f,buttonColor,true)
        RenderUtils.drawRoundedRect(x + 20,y + 214,x + 95,y + 247,3f,buttonColor,true)
        c24.drawString("加载",x + 44f,y + 80f,buttonTextColor)
        c24.drawString("保存",x + 44f,y + 126f,buttonTextColor)
        c24.drawString("新建",x + 44f,y + 177f,buttonTextColor)
        c24.drawString("删除",x + 44f,y + 226f,buttonTextColor)

        RenderUtils.drawRoundedRect(x + 117f,y + 54f,x + 368,y + 264f,4f,configsBGColor,true)

        StencilUtil.initStencilToWrite()
        drawRect((x + 117f).toInt(),(y + 54f).toInt(),(x + 368).toInt(),(y + 264f).toInt(), -1)
        StencilUtil.readStencilBuffer(1)

        var currY = 0f + scroll
        for (c in ConfigManager.configList) {
            var color = if (c == currConfig) configSelectBgColor else configNoSelectBgColor
            RenderUtils.drawRect(x + 130f,y + 65f + currY,x + 355f,y + 105f + currY,color)
            c24.drawCenteredString(c.name.removeSuffix(".json"),x + 235f,y + 83f + currY,configTextColor)

            currY += 50
        }


        StencilUtil.uninitStencilBuffer()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {

        if (mouseButton == 0 && ClickUtils.isClickable(x, y, (x + 200), (y + 45), mouseX, mouseY)) {
            startX = mouseX
            startY = mouseY
            lastClickX = x.toInt()
            lastClickY = y.toInt()
            dragging = true
        }

        if (mouseButton == 0) {
            if (ClickUtils.isClickable(x + 20, y + 67, x + 95, y + 100, mouseX, mouseY)) {
                //加载
                if (currConfig == null) {
                    ChatUtils.messageWithPrefix("§e请先选择参数")
                    return
                }

                try {
                    when (ConfigManager.loadConfig(currConfig!!.name.removeSuffix(".json"))) {
                        0 -> {
                            ChatUtils.messageWithPrefix("§a成功的加载参数 §b${currConfig!!.name}.")
                        }

                        1 -> {
                            ChatUtils.messageWithPrefix("§eWarning: §eThe §eModules §econfig §efile §eis §emissing.§eSuccessfully §eloaded §eHUD §econfig §b${currConfig!!.name}.")
                        }

                        2 -> {
                            ChatUtils.messageWithPrefix("§eWarning: §eThe §eHUD §econfig §efile §eis §emissing.§eSuccessfully §eloaded §eModules §econfig §b${currConfig!!.name}.")
                        }

                        3 -> {
                            ChatUtils.messageWithPrefix("§cFailed to load config §b${currConfig!!.name}.§cFile not found.")
                        }
                    }
                } catch (e: Exception) {
                    ChatUtils.messageWithPrefix("§cError: $e")
                }
            } else if (ClickUtils.isClickable(x + 20,y + 114,x + 95,y + 147,mouseX,mouseY)) {
                //保存
                if (currConfig == null) {
                    ChatUtils.messageWithPrefix("§e请先选择参数")
                    return
                }

                try {
                    ConfigManager.saveConfig(currConfig!!.name.removeSuffix(".json"))
                    ChatUtils.messageWithPrefix("§aSuccessfully saved config §b${currConfig!!.name}.")
                } catch (e: Exception) {
                    ChatUtils.messageWithPrefix("§cError: $e")
                }

            } else if (ClickUtils.isClickable(x + 20,y + 165,x + 95,y + 198,mouseX,mouseY)) {
                //新建
                if (input.text == "") {
                    ChatUtils.messageWithPrefix("§e请输入参数名")
                    return
                }
                for (c in ConfigManager.configList) {
                    if (c.name == input.text + ".json") {
                        ChatUtils.messageWithPrefix("§e请勿重复. 如要替换原有参数, 请使用'保存'")
                        return
                    }
                }
                try {
                    ConfigManager.saveConfig(input.text.removeSuffix(".json"))
                    ChatUtils.messageWithPrefix("§aSuccessfully saved config §b${currConfig!!.name}.")
                } catch (e: Exception) {
                    ChatUtils.messageWithPrefix("§cError: $e")
                }


            } else if (ClickUtils.isClickable(x + 20,y + 214,x + 95,y + 247,mouseX,mouseY)) {
                //删除
                if (currConfig == null) {
                    ChatUtils.messageWithPrefix("§e请先选择参数")
                    return
                }

                try {
                    ConfigManager.removeConfig(currConfig!!.name.removeSuffix(".json"))
                    ChatUtils.messageWithPrefix("§aSuccessfully remove config §b${currConfig!!.name}.")
                } catch (e: Exception) {
                    ChatUtils.messageWithPrefix("§cError: $e")
                }
            }
        }



        var currY = 0f + scroll
        for (c in ConfigManager.configList) {
            if (mouseButton == 0 && ClickUtils.isClickable(x + 130f,y + 65f + currY,x + 355f,y + 105f + currY,mouseX,mouseY)) {
                if (currConfig == c) {
                    currConfig = null
                } else {
                    currConfig = c
                }
            }
            currY += 50
        }
        input.mouseClicked(mouseX,mouseY,mouseButton)
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        if (Mouse.getEventDWheel() != 0) {
            if (scroll <= 0) {
                scroll += (if (Mouse.getEventDWheel() > 0) 5 else -5).toFloat()
            } else {
                scroll = 0f
            }

        }
        super.handleMouseInput()
    }

    override fun initGui() {
        x = (ScaledResolution(mc).scaledWidth / 2 - 200).toFloat()
        y = (ScaledResolution(mc).scaledHeight / 2 - 150).toFloat()
        input.text = ""
        input.isFocused = false
        input.maxStringLength = Int.MAX_VALUE
        super.initGui()
    }
    override fun updateScreen() {
        input.updateCursorCounter()
    }
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (input.isFocused) {
            input.textboxKeyTyped(typedChar, keyCode)
        }
        super.keyTyped(typedChar, keyCode)
    }


    override fun doesGuiPauseGame(): Boolean {
        return false
    }
    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = false
        super.mouseReleased(mouseX, mouseY, state)
    }

}
