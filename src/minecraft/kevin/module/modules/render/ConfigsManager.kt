package kevin.module.modules.render

import kevin.hud.xichenqi.ConfigUI
import kevin.module.BooleanValue
import kevin.module.ListValue
import kevin.module.Module
import kevin.module.ModuleCategory
import java.awt.Color

class ConfigsManager : Module(name = "ConfigsManager", description = "参数管理器", category = ModuleCategory.RENDER) {

    val colorMode = ListValue("Mode", arrayOf("Dark","Light"),"Dark")
    val blur = BooleanValue("Blur",false)

    override fun onEnable() {
        mc.displayGuiScreen(ConfigUI())
        this.state = false
    }
}