package com.eshop.ordering.api.application.commands;

import com.eshop.ordering.api.application.commandbus.Command;

public final class SetStockConfirmedOrderStatusCommand implements Command<Boolean> {
    private final long orderNumber;
    public SetStockConfirmedOrderStatusCommand(long orderNumber) { this.orderNumber = orderNumber; }
    public long getOrderNumber() { return orderNumber; }
}
