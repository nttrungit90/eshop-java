/**
 * Converted from: src/EventBusRabbitMQ/RabbitMqDependencyInjectionExtensions.cs
 * .NET Class: eShop.EventBusRabbitMQ.RabbitMqDependencyInjectionExtensions
 *
 * RabbitMQ configuration for the event bus.
 */
package com.eshop.eventbus.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "eshop_event_bus";

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Bean
    public DirectExchange eventBusExchange() {
        // .NET uses direct exchange (not topic)
        return new DirectExchange(EXCHANGE_NAME, false, false);
    }

    @Bean
    public Queue eventQueue() {
        return QueueBuilder.durable(applicationName + "_queue")
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME + "_dlx")
                .build();
    }

    @Bean
    public Binding eventBinding(Queue eventQueue, DirectExchange eventBusExchange) {
        // For direct exchange, use empty routing key to receive all messages
        return BindingBuilder.bind(eventQueue).to(eventBusExchange).with("");
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(EXCHANGE_NAME);
        return template;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
