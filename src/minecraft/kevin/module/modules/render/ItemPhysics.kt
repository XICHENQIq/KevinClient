/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package kevin.module.modules.render

import kevin.module.FloatValue
import kevin.module.Module
import kevin.module.ModuleCategory


class ItemPhysics : Module(name = "ItemPhysics", category = ModuleCategory.RENDER, description = "物理掉落") {

    val itemWeight = FloatValue("Weight", 0.5F, 0F, 1F)
    override val tag: String
        get() = "${itemWeight.get()}"
}