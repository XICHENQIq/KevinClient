package kevin.module.modules.movement

import kevin.event.EventTarget
import kevin.event.MotionEvent
import kevin.module.Module
import kevin.module.ModuleCategory

class TntFly : Module(name = "TntFly", description = "TntFly", category = ModuleCategory.MOVEMENT) {

    @EventTarget
    fun onMotion(event: MotionEvent){
        if (event.eventState.name == "PRE"){
            mc.player.setPositionAndRotation(
                    mc.player.posX + 1000.0,
                    mc.player.posY,
                    mc.player.posZ,
                    mc.player.rotationYaw,
                    mc.player.rotationPitch
            )
        } else {
            mc.player!!.setPositionAndRotation(
                    mc.player!!.posX - 1000.0,
                    mc.player!!.posY,
                    mc.player!!.posZ,
                    mc.player!!.rotationYaw,
                    mc.player!!.rotationPitch
            )
        }
    }
}