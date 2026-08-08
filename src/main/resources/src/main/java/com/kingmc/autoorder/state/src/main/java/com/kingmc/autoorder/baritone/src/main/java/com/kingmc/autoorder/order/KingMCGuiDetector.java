package com.kingmc.autoorder.order;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class KingMCGuiDetector {
    
    public static boolean isOrderListGui(Screen screen) {
        if (screen == null) return false;
        String title = screen.getTitle().getString().toLowerCase();
        return title.contains("đơn hàng");
    }

    public static int findSandOrderSlot(HandledScreen<?> screen) {
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : handler.slots) {
            // Check only top inventory (custom GUI)
            if (slot.inventory != MinecraftClient.getInstance().player.getInventory()) {
                if (slot.getStack().isOf(Items.SAND)) {
                    return slot.id;
                }
            }
        }
        return -1;
    }

    public static int findConfirmButton(HandledScreen<?> screen) {
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : handler.slots) {
            if (slot.inventory == MinecraftClient.getInstance().player.getInventory()) continue;
            
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            String itemName = stack.getName().getString().toLowerCase();
            // Fallback heuristics: green dye, lime glass, or item named "xác nhận"/"confirm"
            if (itemName.contains("xác nhận") || itemName.contains("hoàn tất") || itemName.contains("confirm") 
                || stack.isOf(Items.GREEN_DYE) || stack.isOf(Items.LIME_STAINED_GLASS_PANE)) {
                return slot.id;
            }
        }
        return -1; // Cannot safely identify
    }
}