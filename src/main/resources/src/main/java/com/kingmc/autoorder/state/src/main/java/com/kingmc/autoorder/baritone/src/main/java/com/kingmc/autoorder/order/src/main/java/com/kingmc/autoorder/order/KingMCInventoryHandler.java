package com.kingmc.autoorder.order;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class KingMCInventoryHandler {

    public static double getSandFillPercentage() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 0;
        
        int sandSlots = 0;
        int totalUsable = 36; // main inventory
        
        for (int i = 0; i < totalUsable; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.SAND)) {
                sandSlots++;
            }
        }
        return ((double) sandSlots / totalUsable) * 100.0;
    }

    public static int getFirstSandSlotInPlayerInventory(ScreenHandler handler) {
        MinecraftClient mc = MinecraftClient.getInstance();
        for (Slot slot : handler.slots) {
            if (slot.inventory == mc.player.getInventory() && slot.getStack().isOf(Items.SAND)) {
                return slot.id;
            }
        }
        return -1;
    }

    public static void shiftClickSlot(int syncId, int slotId) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.interactionManager != null && mc.player != null) {
            mc.interactionManager.clickSlot(
                syncId, 
                slotId, 
                0, 
                SlotActionType.QUICK_MOVE, 
                mc.player
            );
        }
    }

    public static void clickSlot(int syncId, int slotId) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.interactionManager != null && mc.player != null) {
            mc.interactionManager.clickSlot(
                syncId, 
                slotId, 
                0, 
                SlotActionType.PICKUP, 
                mc.player
            );
        }
    }
}