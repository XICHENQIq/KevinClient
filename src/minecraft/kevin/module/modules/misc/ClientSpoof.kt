/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package kevin.module.modules.misc

import kevin.module.ListValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.module.TextValue

class ClientSpoof : Module(name = "ClientSpoof", description = "spoof your client", category = ModuleCategory.MISC) {
    val modeValue: ListValue = ListValue("Payloads", arrayOf("Vanilla", "Fabric", "Lunar", "LabyMod", "Custom", "CheatBreaker", "PvPLounge"), "Lunar")
    val CustomClient: TextValue = TextValue("CustomClientSpoof", "CustomClient")

    override val tag: String
        get() = modeValue.get()
}