package kevin.module.modules.player

import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.module.FloatValue
import kevin.module.Module
import kevin.module.ModuleCategory

class ArmorSpoof : Module(name = "ArmorSpoof", description = "欺骗敌人的眼睛", category = ModuleCategory.PLAYER) {

    private val range = FloatValue("WearRange",8f,0f,16f)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

    }
}