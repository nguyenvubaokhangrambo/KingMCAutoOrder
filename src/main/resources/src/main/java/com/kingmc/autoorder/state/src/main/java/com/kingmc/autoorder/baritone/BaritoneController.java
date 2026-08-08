package com.kingmc.autoorder.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import net.minecraft.block.Blocks;

public class BaritoneController {
    public static void startMiningSand() {
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (!baritone.getMineProcess().isActive()) {
            baritone.getMineProcess().mine(Blocks.SAND);
        }
    }

    public static void stop() {
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        baritone.getMineProcess().cancel();
        baritone.getPathingBehavior().cancelEverything();
    }

    public static boolean isMining() {
        return BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive();
    }
}