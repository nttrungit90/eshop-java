package com.eshop.ordering.api.application.commands;

import com.eshop.ordering.api.application.commandbus.Command;

public final class SetPaidOrderStatusCommand implements Command<Boolean> {
    private final long orderNumber;
    public SetPaidOrderStatusCommand(long orderNumber) { this.orderNumber = orderNumber; }
    public long getOrderNumber() { return orderNumber; }
}
