package kevin.hud.element.elements

import kevin.KevinClient
import kevin.hud.element.Border
import kevin.hud.element.Element
import kevin.hud.element.ElementInfo
import kevin.hud.font.FontLoader
import kevin.utils.RenderUtils
import java.awt.Color

@ElementInfo("ClientInfo",true)
class ClientInfo(x: Double = 2.0, y: Double = 2.0, scale: Float = 1F) : Element() {

    override fun drawElement(): Border {

        val font = FontLoader.getCFont(false,20)

        var width = 0f

        val clientName = KevinClient.clientName
        width += font.getStringWidth(clientName)

        val kills = " Kills:" + KevinClient.combatManager.killCounts
        width += font.getStringWidth(kills)

        RenderUtils.drawRoundedRect(0f,0f,width + 6f,font.FONT_HEIGHT + 6f,3f,Color(0,0,0,100).rgb,true)

        font.drawString(clientName,3,3,RenderUtils.skyRainbow(1,1f,1f).rgb)
        font.drawString(kills,font.getStringWidth(clientName) + 3,3,Color.WHITE.rgb)

        return Border(0f,0f,width + 6f,font.FONT_HEIGHT + 6f)
    }

}