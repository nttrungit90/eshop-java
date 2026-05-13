package com.eshop.ordering.api.application.commands;

import com.eshop.ordering.api.application.commandbus.CommandHandler;
import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.aggregates.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CancelOrderCommandHandler implements CommandHandler<CancelOrderCommand, Boolean> {

    private static final Logger log = LoggerFactory.getLogger(CancelOrderCommandHandler.class);

    private final OrderRepository orderRepository;

    public CancelOrderCommandHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override public Class<CancelOrderCommand> commandType() { return CancelOrderCommand.class; }

    @Override
    @Transactional
    public Boolean handle(CancelOrderCommand cmd) {
        Order order = orderRepository.findById(cmd.getOrderNumber()).orElse(null);
        if (order == null) { log.warn("Order {} not found", cmd.getOrderNumber()); return Boolean.FALSE; }
        order.setCancelledStatus();
        orderRepository.save(order);
        return Boolean.TRUE;
    }
}
