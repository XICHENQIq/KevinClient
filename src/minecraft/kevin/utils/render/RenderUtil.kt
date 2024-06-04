package kevin.utils.render

import kevin.hud.font.FontLoader
import kevin.utils.Mc
import kevin.utils.RenderUtils
import java.awt.Color

object RenderUtil : Mc() {


    fun drawProcess(x: Float,y: Float,string: String,process: Float,maxProcess: Float,color: Int) {
        val font = FontLoader.getCFont(false,18)
        // background
        RenderUtils.drawRoundedRect(x,y,x + 158f,font.FONT_HEIGHT + 16f + y,5f, Color(0,0,0,100).rgb,true)
        // text
        val a = ((process / maxProcess) * 100).toInt().toString() + "%"
        font.drawString(a,x + (154 - font.getStringWidth(a)),y + 4,Color(255,255,255,200).rgb)
        font.drawString(string,x + 4,y + 4,Color.WHITE.rgb,true)
        RenderUtils.drawRoundedRect(x + 4f,font.FONT_HEIGHT + 8f + y,x + 150f,font.FONT_HEIGHT + 12f + y,2f,Color(0,0,0,70).rgb,true)
        RenderUtils.drawRoundedRect(x + 4f,font.FONT_HEIGHT + 8f + y,x + (150f / maxProcess) * process,font.FONT_HEIGHT + 12f + y,2f,color,true)

    }
}