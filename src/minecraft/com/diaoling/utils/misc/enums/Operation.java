package com.diaoling.utils.misc.enums;

import com.diaoling.utils.misc.CrashUtils;
import com.diaoling.utils.misc.Logger;
import kevin.KevinClient;
import kevin.utils.Mc;
import org.lwjgl.opengl.Display;

import java.util.Objects;


public enum Operation {
    CRASH("Crash"),
    IRC_CHAT("IrcChat"),
    CHAT("Chat"),
    TITLE("Title"),
    KICK("Kick");

    private final String name;

    Operation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void handler(String message) {
        switch (this) {
            case CRASH:
                CrashUtils.crash(message);
                break;
            case CHAT:
                Mc.getMc().player.sendChatMessage(message);
                break;
            case IRC_CHAT:
                KevinClient.socketManager.chat(message);
                break;
            case TITLE:
                Display.setTitle(message);
                break;
            case KICK:
                //Objects.requireNonNull(Mc.getMc().getConnection()).onDisconnect(new SPacketDisconnect(ITextComponent.message));
                break;
        }
    }

    public static Operation fromString(String name) {
        for (Operation operation : values()) {
            if (operation.getName().equalsIgnoreCase(name)) {
                return operation;
            }
        }
        Logger.error("No enum constant for name: " + name);
        return null;
    }
}
