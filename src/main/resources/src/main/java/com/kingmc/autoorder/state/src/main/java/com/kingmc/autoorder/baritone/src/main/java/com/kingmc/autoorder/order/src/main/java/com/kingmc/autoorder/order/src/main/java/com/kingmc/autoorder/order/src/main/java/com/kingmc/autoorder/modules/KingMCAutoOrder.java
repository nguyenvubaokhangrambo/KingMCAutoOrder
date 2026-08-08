package com.kingmc.autoorder.modules;

import com.kingmc.autoorder.baritone.BaritoneController;
import com.kingmc.autoorder.order.KingMCGuiDetector;
import com.kingmc.autoorder.order.KingMCInventoryHandler;
import com.kingmc.autoorder.order.KingMCOrderHandler;
import com.kingmc.autoorder.state.AutoOrderState;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class KingMCAutoOrder extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoMine = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-mine")
        .description("Automatically mine Sand with Baritone.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder()
        .name("inventory-threshold")
        .description("Percentage of inventory filled with Sand to trigger order.")
        .defaultValue(90)
        .min(1).max(100)
        .sliderMax(100)
        .build()
    );

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
        .name("command")
        .description("Command to open the order GUI.")
        .defaultValue("/order sand")
        .build()
    );

    private final Setting<Integer> clickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("click-delay")
        .description("Delay in ticks before clicking GUI elements (20 ticks = 1s).")
        .defaultValue(10)
        .min(1).max(40)
        .build()
    );

    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("debug")
        .description("Show debug messages in chat.")
        .defaultValue(true)
        .build()
    );

    private AutoOrderState currentState = AutoOrderState.IDLE;
    private int delayTicks = 0;
    private int stateTimeoutTicks = 0;
    private int failureCount = 0;

    public KingMCAutoOrder() {
        super(Categories.Misc, "KingMC-Auto-Order", "Automatically mines Sand and fulfills KingMC orders.");
    }

    @Override
    public void onActivate() {
        currentState = AutoOrderState.IDLE;
        delayTicks = 0;
        failureCount = 0;
        if (autoMine.get()) {
            setState(AutoOrderState.MINING);
        }
    }

    @Override
    public void onDeactivate() {
        BaritoneController.stop();
        currentState = AutoOrderState.IDLE;
    }

    private void setState(AutoOrderState newState) {
        currentState = newState;
        stateTimeoutTicks = 0;
        if (debug.get()) {
            info("[KingMCAutoOrder] State changed to: " + newState.name());
        }
    }

    private void debugLog(String msg) {
        if (debug.get()) {
            info("[KingMCAutoOrder] " + msg);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        stateTimeoutTicks++;
        
        // Timeout catch-all for GUI states
        if (stateTimeoutTicks > 100 && currentState != AutoOrderState.MINING && currentState != AutoOrderState.IDLE) {
            debugLog("Timeout reached in state " + currentState.name() + ". Aborting.");
            KingMCOrderHandler.closeScreen();
            handleFailure();
            return;
        }

        switch (currentState) {
            case IDLE:
                if (autoMine.get()) setState(AutoOrderState.MINING);
                break;

            case MINING:
                if (!BaritoneController.isMining() && mc.currentScreen == null) {
                    BaritoneController.startMiningSand();
                }
                
                // Check inventory every 10 ticks
                if (stateTimeoutTicks % 10 == 0) {
                    if (KingMCInventoryHandler.getSandFillPercentage() >= threshold.get()) {
                        debugLog("Inventory threshold reached.");
                        setState(AutoOrderState.INVENTORY_FULL);
                    }
                }
                break;

            case INVENTORY_FULL:
                BaritoneController.stop();
                delayTicks = clickDelay.get(); 
                setState(AutoOrderState.OPENING_ORDER);
                break;

            case OPENING_ORDER:
                debugLog("Opening order GUI...");
                KingMCOrderHandler.executeCommand(command.get());
                delayTicks = clickDelay.get() * 2;
                setState(AutoOrderState.ORDER_LIST);
                break;

            case ORDER_LIST:
                if (mc.currentScreen instanceof HandledScreen<?> screen) {
                    if (KingMCGuiDetector.isOrderListGui(screen)) {
                        int sandSlot = KingMCGuiDetector.findSandOrderSlot(screen);
                        if (sandSlot != -1) {
                            debugLog("Found Sand order at slot " + sandSlot);
                            KingMCInventoryHandler.clickSlot(screen.getScreenHandler().syncId, sandSlot);
                            delayTicks = clickDelay.get();
                            setState(AutoOrderState.OPENING_INPUT);
                        } else {
                            debugLog("No Sand order found in list.");
                            KingMCOrderHandler.closeScreen();
                            handleFailure();
                        }
                    }
                }
                break;

            case OPENING_INPUT:
                if (mc.currentScreen instanceof HandledScreen<?> screen && !KingMCGuiDetector.isOrderListGui(screen)) {
                    debugLog("Opening input GUI...");
                    delayTicks = clickDelay.get();
                    setState(AutoOrderState.INSERTING_SAND);
                }
                break;

            case INSERTING_SAND:
                if (mc.currentScreen instanceof HandledScreen<?> screen) {
                    int pInvSand = KingMCInventoryHandler.getFirstSandSlotInPlayerInventory(screen.getScreenHandler());
                    if (pInvSand != -1) {
                        debugLog("Found Sand in inventory slot " + pInvSand + ". Moving Sand...");
                        KingMCInventoryHandler.shiftClickSlot(screen.getScreenHandler().syncId, pInvSand);
                        delayTicks = clickDelay.get();
                        setState(AutoOrderState.CONFIRMING);
                    } else {
                        debugLog("Not enough sand to insert!");
                        KingMCOrderHandler.closeScreen();
                        handleFailure();
                    }
                }
                break;

            case CONFIRMING:
                if (mc.currentScreen instanceof HandledScreen<?> screen) {
                    int confirmSlot = KingMCGuiDetector.findConfirmButton(screen);
                    if (confirmSlot != -1) {
                        debugLog("Confirming order...");
                        KingMCInventoryHandler.clickSlot(screen.getScreenHandler().syncId, confirmSlot);
                        delayTicks = clickDelay.get() * 2;
                        setState(AutoOrderState.WAITING_RESULT);
                    } else {
                        debugLog("Cannot identify KingMC confirm button.");
                        KingMCOrderHandler.closeScreen();
                        handleFailure();
                    }
                }
                break;

            case WAITING_RESULT:
                // Wait for GUI to close naturally or manually check
                if (mc.currentScreen == null) {
                    debugLog("Order successful.");
                    failureCount = 0;
                    delayTicks = 20; // 1s delay before resuming
                    setState(AutoOrderState.RESUME_MINING);
                } else if (stateTimeoutTicks > 60) {
                    // Force close if server didn't close it
                    KingMCOrderHandler.closeScreen();
                    setState(AutoOrderState.RESUME_MINING);
                }
                break;

            case RESUME_MINING:
                debugLog("Resuming Baritone...");
                setState(AutoOrderState.MINING);
                break;

            case ERROR:
                if (autoMine.get()) {
                    toggle(); // Auto-turn off module on persistent error
                }
                break;
        }
    }

    private void handleFailure() {
        failureCount++;
        if (failureCount >= 2) {
            debugLog("Max failures reached. Stopping Auto Order.");
            setState(AutoOrderState.ERROR);
        } else {
            debugLog("Retrying order sequence... (" + failureCount + "/2)");
            delayTicks = 40;
            setState(AutoOrderState.INVENTORY_FULL);
        }
    }
}