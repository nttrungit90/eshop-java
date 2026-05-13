package com.eshop.ordering.api.application.commands;

import com.eshop.ordering.api.application.commandbus.CommandHandler;
import com.eshop.ordering.api.dto.OrderDraftDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Mirrors .NET CreateOrderDraftCommandHandler — builds a draft Order in memory
 * just to compute the total + line items shape for UI preview, then discards it.
 * No persistence, no events.
 */
@Component
public class CreateOrderDraftCommandHandler implements CommandHandler<CreateOrderDraftCommand, OrderDraftDto> {

    @Override public Class<CreateOrderDraftCommand> commandType() { return CreateOrderDraftCommand.class; }

    @Override
    public OrderDraftDto handle(CreateOrderDraftCommand cmd) {
        var items = cmd.getItems().stream()
                .map(i -> new OrderDraftDto.Item(
                        i.getProductId(), i.getProductName(), i.getUnitPrice(),
                        i.getDiscount() != null ? i.getDiscount() : BigDecimal.ZERO,
                        i.getQuantity(), i.getPictureUrl()))
                .collect(Collectors.toList());
        BigDecimal total = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getUnits())).subtract(i.getDiscount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderDraftDto(items, total);
    }
}
