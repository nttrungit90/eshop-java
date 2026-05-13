package com.eshop.ordering.api.application.commands;

import com.eshop.ordering.api.application.commandbus.CommandHandler;
import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.aggregates.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SetStockConfirmedOrderStatusCommandHandler
        implements CommandHandler<SetStockConfirmedOrderStatusCommand, Boolean> {

    private static final Logger log = LoggerFactory.getLogger(SetStockConfirmedOrderStatusCommandHandler.class);

    private final OrderRepository orderRepository;

    public SetStockConfirmedOrderStatusCommandHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override public Class<SetStockConfirmedOrderStatusCommand> commandType() {
        return SetStockConfirmedOrderStatusCommand.class;
    }

    @Override
    @Transactional
    public Boolean handle(SetStockConfirmedOrderStatusCommand cmd) {
        Order order = orderRepository.findById(cmd.getOrderNumber()).orElse(null);
        if (order == null) { log.warn("Order {} not found", cmd.getOrderNumber()); return Boolean.FALSE; }
        order.setStockConfirmedStatus();
        orderRepository.save(order);
        return Boolean.TRUE;
    }
}
