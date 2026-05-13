package com.eshop.ordering.api.application.commands;

import com.eshop.ordering.api.application.commandbus.CommandHandler;
import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.aggregates.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SetAwaitingValidationOrderStatusCommandHandler
        implements CommandHandler<SetAwaitingValidationOrderStatusCommand, Boolean> {

    private static final Logger log = LoggerFactory.getLogger(SetAwaitingValidationOrderStatusCommandHandler.class);

    private final OrderRepository orderRepository;

    public SetAwaitingValidationOrderStatusCommandHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override public Class<SetAwaitingValidationOrderStatusCommand> commandType() {
        return SetAwaitingValidationOrderStatusCommand.class;
    }

    @Override
    @Transactional
    public Boolean handle(SetAwaitingValidationOrderStatusCommand cmd) {
        Order order = orderRepository.findById(cmd.getOrderNumber()).orElse(null);
        if (order == null) {
            log.warn("Order {} not found for AwaitingValidation transition", cmd.getOrderNumber());
            return Boolean.FALSE;
        }
        order.setAwaitingValidationStatus();
        orderRepository.save(order);   // triggers OrderStatusChangedToAwaitingValidationDomainEvent → outbox
        return Boolean.TRUE;
    }
}
