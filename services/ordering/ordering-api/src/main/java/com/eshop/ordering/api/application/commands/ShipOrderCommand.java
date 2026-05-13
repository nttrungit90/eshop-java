package com.eshop.ordering.api.application.commands;

import com.eshop.ordering.api.application.commandbus.Command;

public final class ShipOrderCommand implements Command<Boolean> {
    private final long orderNumber;
    public ShipOrderCommand(long orderNumber) { this.orderNumber = orderNumber; }
    public long getOrderNumber() { return orderNumber; }
}
