package com.kingmc.autoorder.order;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class KingMCOrderHandler {
    public static void executeCommand(String command) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.networkHandler.sendChatCommand(command.replace("/", ""));
        }
    }

    public static void closeScreen() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.closeHandledScreen();
        }
    }
}