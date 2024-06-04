package kevin.module.modules.movement

import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.module.ListValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.MovementUtils

class NoWeb : Module("NoWeb", "Prevents you from getting slowed down in webs.", category = ModuleCategory.MOVEMENT) {

    private val modeValue = ListValue("Mode", arrayOf("None","GrimStrafe", "AAC", "LAAC", "Rewi"), "None")

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.player ?: return

        if (!thePlayer.isInWeb)
            return

        when (modeValue.get().toLowerCase()) {
            "none" -> thePlayer.isInWeb = false
            "aac" -> {
                thePlayer.jumpMovementFactor = 0.59f

                if (!mc.gameSettings.keyBindSneak.isKeyDown)
                    thePlayer.motionY = 0.0
            }
            "laac" -> {
                thePlayer.jumpMovementFactor = if (thePlayer.movementInput.moveStrafe != 0f) 1.0f else 1.21f

                if (!mc.gameSettings.keyBindSneak.isKeyDown)
                    thePlayer.motionY = 0.0

                if (thePlayer.onGround)
                    thePlayer.jump()
            }
            "rewi" -> {
                thePlayer.jumpMovementFactor = 0.42f

                if (thePlayer.onGround)
                    thePlayer.jump()
            }
            "grimstrafe" -> {
                thePlayer.motionY = 0.0
                thePlayer.motionX = 0.0
                thePlayer.motionZ = 0.0
                if (mc.gameSettings.keyBindJump.isKeyDown) thePlayer.motionY += 0.5
                if (mc.gameSettings.keyBindSneak.isKeyDown){
                    thePlayer.motionY -= 0.3
                }
                MovementUtils.strafe(0.2f)
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}