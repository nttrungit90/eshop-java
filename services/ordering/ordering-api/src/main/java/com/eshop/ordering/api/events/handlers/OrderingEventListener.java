package com.eshop.ordering.api.events.handlers;

import com.eshop.ordering.api.application.commandbus.CommandBus;
import com.eshop.ordering.api.application.commandbus.IdempotentCommandExecutor;
import com.eshop.ordering.api.application.commandbus.IdentifiedCommand;
import com.eshop.ordering.api.application.commands.CancelOrderCommand;
import com.eshop.ordering.api.application.commands.SetAwaitingValidationOrderStatusCommand;
import com.eshop.ordering.api.application.commands.SetPaidOrderStatusCommand;
import com.eshop.ordering.api.application.commands.SetStockConfirmedOrderStatusCommand;
import com.eshop.ordering.api.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Inbound integration event listener. Translates every RabbitMQ message into a
 * CQRS command and dispatches through the {@link IdempotentCommandExecutor} —
 * the integration event's own UUID is the idempotency key, so a redelivered
 * RabbitMQ message can't double-transition an order.
 *
 * <p>State machine drivers:
 * <pre>
 *   GracePeriodConfirmedIntegrationEvent   → SetAwaitingValidationOrderStatusCommand
 *   OrderStockConfirmedIntegrationEvent    → SetStockConfirmedOrderStatusCommand
 *   OrderStockRejectedIntegrationEvent     → CancelOrderCommand
 *   OrderPaymentSucceededIntegrationEvent  → SetPaidOrderStatusCommand
 *   OrderPaymentFailedIntegrationEvent     → CancelOrderCommand
 * </pre>
 *
 * <p>Each {@code Set*OrderStatus} command's handler raises the matching domain event;
 * domain event handlers enqueue the next-stage integration event via the outbox.
 * Mirrors .NET {@code OrderStockConfirmedIntegrationEventHandler}, etc.
 */
@Component
public class OrderingEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderingEventListener.class);

    private final ObjectMapper objectMapper;
    private final CommandBus commandBus;
    private final IdempotentCommandExecutor idempotentExecutor;

    public OrderingEventListener(
            @Qualifier("eventBusObjectMapper") ObjectMapper objectMapper,
            CommandBus commandBus,
            IdempotentCommandExecutor idempotentExecutor) {
        this.objectMapper = objectMapper;
        this.commandBus = commandBus;
        this.idempotentExecutor = idempotentExecutor;
    }

    @RabbitListener(queues = "ordering-api_queue")
    public void onMessage(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String body = new String(message.getBody());
        log.info("Received event {} on ordering-api_queue", routingKey);

        try {
            switch (routingKey) {
                case "GracePeriodConfirmedIntegrationEvent" -> {
                    var ev = objectMapper.readValue(body, GracePeriodConfirmedIntegrationEvent.class);
                    idempotentExecutor.execute(new IdentifiedCommand<>(ev.getId(),
                            new SetAwaitingValidationOrderStatusCommand(ev.getOrderId())), Boolean.TRUE);
                }
                case "OrderStockConfirmedIntegrationEvent" -> {
                    var ev = objectMapper.readValue(body, OrderStockConfirmedIntegrationEvent.class);
                    idempotentExecutor.execute(new IdentifiedCommand<>(ev.getId(),
                            new SetStockConfirmedOrderStatusCommand(ev.getOrderId())), Boolean.TRUE);
                }
                case "OrderStockRejectedIntegrationEvent" -> {
                    var ev = objectMapper.readValue(body, OrderStockRejectedIntegrationEvent.class);
                    idempotentExecutor.execute(new IdentifiedCommand<>(ev.getId(),
                            new CancelOrderCommand(ev.getOrderId())), Boolean.TRUE);
                }
                case "OrderPaymentSucceededIntegrationEvent" -> {
                    var ev = objectMapper.readValue(body, OrderPaymentSucceededIntegrationEvent.class);
                    idempotentExecutor.execute(new IdentifiedCommand<>(ev.getId(),
                            new SetPaidOrderStatusCommand(ev.getOrderId())), Boolean.TRUE);
                }
                case "OrderPaymentFailedIntegrationEvent" -> {
                    var ev = objectMapper.readValue(body, OrderPaymentFailedIntegrationEvent.class);
                    idempotentExecutor.execute(new IdentifiedCommand<>(ev.getId(),
                            new CancelOrderCommand(ev.getOrderId())), Boolean.TRUE);
                }
                default -> log.warn("Unknown routing key on ordering-api_queue: {}", routingKey);
            }
        } catch (Exception e) {
            log.error("Error processing event {}: {}", routingKey, e.getMessage(), e);
        }
    }

    /** Silence unused-warning until needed; CommandBus is kept on the field for future direct dispatch. */
    @SuppressWarnings("unused")
    private CommandBus unused() { return commandBus; }
}
