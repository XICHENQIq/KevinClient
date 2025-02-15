package kevin.module.modules.combat

import kevin.module.BooleanValue
import kevin.module.FloatValue
import kevin.module.Module
import kevin.module.ModuleCategory

class HitBox : Module("HitBox", "Makes hitboxes of targets bigger.", category = ModuleCategory.COMBAT) {
    val sizeValue = FloatValue("Size", 0.4F, 0F, 1F)
    val grim = BooleanValue("Grim",false)
    val grimY = FloatValue("GrimY",1f,0f,2f)
    override val tag: String
        get() = "Size:${sizeValue.get()}"
}