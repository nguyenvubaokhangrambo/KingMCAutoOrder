package com.kingmc.autoorder.state;

public enum AutoOrderState {
    IDLE,
    MINING,
    INVENTORY_FULL,
    OPENING_ORDER,
    ORDER_LIST,
    OPENING_INPUT,
    INSERTING_SAND,
    CONFIRMING,
    WAITING_RESULT,
    RESUME_MINING,
    ERROR
}
