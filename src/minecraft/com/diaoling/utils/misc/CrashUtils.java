package com.diaoling.utils.misc;

import com.diaoling.network.packet.Packet;
import kevin.utils.Mc;

/**
 * @author DiaoLing
 * @since 4/10/2024
 */
public class CrashUtils extends Mc {
    /**
     * Forces the Java Virtual Machine to crash by exhausting available memory.
     * @param message The error message to display/log upon crashing.
     */
    public static void forceCrash(String message) {
        System.err.println("Forced crash initiated: " + message);
        try {
            long[] l = new long[Integer.MAX_VALUE];
        } finally {
            System.err.println("Recovering from forced memory allocation...");
        }
        forceUnrecoverableError();
    }

    /**
     * Intentionally causes StackOverflowError by recursive method calls.
     */
    public static void forceUnrecoverableError() {
        forceUnrecoverableError();
    }

    public static void createExcessiveThreads() {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "CrashThread-" + i).start();
        }
    }

    public static void minecraftCrash() {
        Mc.getMc().player = null;
        Mc.getMc().world = null;
        //Mc.getMc().gameRenderer.blitScreenProgram = null;
        //Mc.getMc().getConnection().sendPacket((Packet<?>) new Object());
    }

    public static void crash(String message) {
        forceCrash(message);
        forceUnrecoverableError();
        minecraftCrash();
        createExcessiveThreads();
    }
}
