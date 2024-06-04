package net.minecraft.util;

import kevin.event.EventManager;
import kevin.event.MoveInputEvent;
import net.minecraft.client.settings.GameSettings;

public class MovementInputFromOptions extends MovementInput
{
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState()
    {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;
        if (this.gameSettings.keyBindForward.isKeyDown()) {
            ++this.moveForward;
            this.forwardKeyDown = true;
        } else {
            this.forwardKeyDown = false;
        }

        if (this.gameSettings.keyBindBack.isKeyDown()) {
            --this.moveForward;
            this.backKeyDown = true;
        } else {
            this.backKeyDown = false;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown()) {
            ++this.moveStrafe;
            this.leftKeyDown = true;
        } else {
            this.leftKeyDown = false;
        }

        if (this.gameSettings.keyBindRight.isKeyDown()) {
            --this.moveStrafe;
            this.rightKeyDown = true;
        } else {
            this.rightKeyDown = false;
        }
        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        final MoveInputEvent event = new MoveInputEvent(moveForward, moveStrafe, jump, sneak, 0.3D);
        EventManager.INSTANCE.callEvent(event);

        final double sneakMultiplier = event.getSneakSlowDownMultiplier();
        this.moveForward = event.getForward();
        this.moveStrafe = event.getStrafe();
        this.jump = event.getJump();
        this.sneak = event.getSneak();

        if (this.sneak) {
            this.moveStrafe = (float)((double)this.moveStrafe * sneakMultiplier);
            this.moveForward = (float)((double)this.moveForward *sneakMultiplier);
        }
    }
}
