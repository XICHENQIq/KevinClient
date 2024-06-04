package kevin.hud.xichenqi

import io.netty.buffer.Unpooled
import kevin.utils.RenderUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import java.awt.Color


class GermUI : GuiScreen() {

    var mouseX = 0
    var mouseY = 0

    var currentClassification = 114514

    val sR = ScaledResolution(mc)
    val x0 = sR.scaledWidth / 2
    val y0 = sR.scaledHeight / 2
    val font = mc.fontRenderer

    private val classification = listOf("空岛战争", "起床战争", "休闲", "竞技", "战争")

    private val swGameMods = listOf("空岛战争-单人", "空岛战争-双人")
    private val bwGameMods = listOf("起床战争-单人", "起床战争-双人", "起床战争-四人", "起床战争-16人", "起床战争-32人", "练习场")
    private val leisureGameMods = listOf("小游戏派对", "烫手山芋")
    private val sportsGameMods = listOf("职业战争", "竞技场", "天坑")
    private val warGameMods = listOf("枪械起床", "职业起床", "战墙", "吃鸡单人")


    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.mouseX = mouseX
        this.mouseY = mouseY

        // background
        RenderUtils.drawRect(x0.toFloat(), y0.toFloat(), x0 + 102f, y0 + 63f, Color(0, 0, 0, 100).rgb)

        for (index in 0..4) {
            drawClassification(index)
        }
        if (currentClassification != 114514) {
            for (index in 0 until getClassification(currentClassification).size) {
                drawButton(currentClassification, index)
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (index in 0..4) {
            if (isMouseInsideClassification(index)) {
                currentClassification = index
            }
        }
        if (currentClassification != 114514) {
            for (index in 0 until getClassification(currentClassification).size) {
                if (isMouseInsideGameMod(index)) {
                    packet(currentClassification, index)
                }
            }
        }
    }

    fun isMouseInsideClassification(index: Int): Boolean {
        return mouseX in x0 + 2f.toInt()..x0 + 42f.toInt() && mouseY.toFloat() in y0 + 2f + (index * 8f).toInt()..y0 + 9f + (index * 8f).toInt()
    }

    fun isMouseInsideGameMod(index: Int): Boolean {
        return mouseX in x0 + 44f.toInt()..x0 + 100f.toInt() && mouseY.toFloat() in y0 + 2f + (index * 8f).toInt()..y0 + 9f + (index * 8f).toInt()
    }

    fun drawClassification(index: Int) {
        if (isMouseInsideClassification(index)) {
            RenderUtils.drawRect(x0 + 2f, y0 + 2f + (index * 8f), x0 + 42f, y0 + 9f + (index * 8f), Color(255, 255, 255, 100).rgb)
        }
        font.drawString(classification[index], x0 + 4, y0 + 2 + (index * 8), Color.GREEN.rgb)
    }

    fun drawButton(classificationIndex: Int, index: Int) {
        if (isMouseInsideGameMod(index)) {
            RenderUtils.drawRect(x0 + 44f, y0 + 2f + (index * 8f), x0 + 100f, y0 + 9f + (index * 8f), Color(255, 255, 255, 100).rgb)
        }

        font.drawString(getClassification(classificationIndex)[index], x0 + 46, y0 + 2 + (index * 8), Color.YELLOW.rgb)
    }

    fun getClassification(index: Int): List<String> {
        if (classification[index] == "空岛战争") {
            return swGameMods
        } else if (classification[index] == "起床战争") {
            return bwGameMods
        } else if (classification[index] == "休闲") {
            return leisureGameMods
        } else if (classification[index] == "竞技") {
            return sportsGameMods
        } else if (classification[index] == "战争") {
            return warGameMods
        } else {
            return emptyList()
        }
    }

    fun packet(classificationIndex: Int, index: Int) {
        if (classification[classificationIndex] == "空岛战争") {
            if (swGameMods[index] == "空岛战争-单人") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 48, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 48, 44, 34, 115, 105, 100, 34, 58, 34, 83, 75, 89, 87, 65, 82, 47, 110, 115, 107, 121, 119, 97, 114, 34, 125)))))
            } else if (swGameMods[index] == "空岛战争-双人") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 49, 41, 123, 34, 101, 110, 116, 114, 121, 34, 58, 49, 44, 34, 115, 105, 100, 34, 58, 34, 83, 75, 89, 87, 65, 82, 47, 110, 115, 107, 121, 119, 97, 114, 45, 100, 111, 117, 98, 108, 101, 34, 125)))))
            }
        } else if (classification[classificationIndex] == "起床战争") {
            if (bwGameMods[index] == "起床战争-单人") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 49, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 49, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 115, 111, 108, 111, 34, 125)))))
            } else if (bwGameMods[index] == "起床战争-双人") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 50, 36, 123, 34, 101, 110, 116, 114, 121, 34, 58, 50, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 100, 111, 117, 98, 108, 101, 34, 125)))))
            } else if (bwGameMods[index] == "起床战争-四人") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 51, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 51, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 116, 101, 97, 109, 34, 125)))))
            } else if (bwGameMods[index] == "起床战争-16人") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 52, 36, 123, 34, 101, 110, 116, 114, 121, 34, 58, 52, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 120, 112, 49, 54, 110, 101, 119, 34, 125)))))
            } else if (bwGameMods[index] == "起床战争-32人") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 53, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 53, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 120, 112, 45, 51, 50, 34, 125)))))
            } else if (bwGameMods[index] == "练习场") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 48, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 48, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 100, 97, 108, 117, 34, 125)))))
            }
        } else if (classification[classificationIndex] == "休闲") {
            if (leisureGameMods[index] == "小游戏派对") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 49, 35, 123, 34, 101, 110, 116, 114, 121, 34, 58, 49, 44, 34, 115, 105, 100, 34, 58, 34, 76, 69, 73, 83, 85, 82, 69, 47, 109, 103, 45, 103, 97, 109, 101, 34, 125)))))
            } else if (leisureGameMods[index] == "烫手山芋") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 52, 35, 123, 34, 101, 110, 116, 114, 121, 34, 58, 52, 44, 34, 115, 105, 100, 34, 58, 34, 76, 69, 73, 83, 85, 82, 69, 47, 104, 112, 45, 103, 97, 109, 101, 34, 125)))))
            }
        } else if (classification[classificationIndex] == "竞技") {
            if (sportsGameMods[index] == "职业战争") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 50, 33, 123, 34, 101, 110, 116, 114, 121, 34, 58, 50, 44, 34, 115, 105, 100, 34, 58, 34, 70, 73, 71, 72, 84, 47, 107, 98, 45, 103, 97, 109, 101, 34, 125)))))
            } else if (sportsGameMods[index] == "竞技场") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 51, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 51, 44, 34, 115, 105, 100, 34, 58, 34, 70, 73, 71, 72, 84, 47, 97, 114, 101, 110, 97, 80, 86, 80, 34, 125)))))
            } else if (sportsGameMods[index] == "天坑") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 52, 33, 123, 34, 101, 110, 116, 114, 121, 34, 58, 52, 44, 34, 115, 105, 100, 34, 58, 34, 70, 73, 71, 72, 84, 47, 116, 104, 101, 45, 112, 105, 116, 34, 125)))))
            }
        } else if (classification[classificationIndex] == "战争") {
            if (warGameMods[index] == "枪械起床") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 48, 40, 123, 34, 101, 110, 116, 114, 121, 34, 58, 48, 44, 34, 115, 105, 100, 34, 58, 34, 84, 69, 65, 77, 95, 70, 73, 71, 72, 84, 47, 99, 115, 98, 119, 120, 112, 45, 51, 50, 34, 125)))))
            } else if (warGameMods[index] == "职业起床") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 49, 41, 123, 34, 101, 110, 116, 114, 121, 34, 58, 49, 44, 34, 115, 105, 100, 34, 58, 34, 84, 69, 65, 77, 95, 70, 73, 71, 72, 84, 47, 98, 119, 107, 105, 116, 120, 112, 45, 51, 50, 34, 125)))))
            } else if (warGameMods[index] == "战墙") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 51, 42, 123, 34, 101, 110, 116, 114, 121, 34, 58, 51, 44, 34, 115, 105, 100, 34, 58, 34, 84, 69, 65, 77, 95, 70, 73, 71, 72, 84, 47, 98, 97, 116, 116, 108, 101, 119, 97, 108, 108, 115, 34, 125)))))
            } else if (warGameMods[index] == "吃鸡单人") {
                mc.connection!!.networkManager.sendPacket(CPacketCustomPayload("germmod-netease", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 51, 40, 123, 34, 101, 110, 116, 114, 121, 34, 58, 51, 44, 34, 115, 105, 100, 34, 58, 34, 84, 69, 65, 77, 95, 70, 73, 71, 72, 84, 47, 112, 117, 98, 103, 45, 115, 111, 108, 111, 34, 125)))))
            }
        }
        mc.displayGuiScreen(null)
    }
}