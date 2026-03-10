/**
 * Converted from: src/EventBusRabbitMQ/RabbitMqDependencyInjectionExtensions.cs
 * .NET Class: eShop.EventBusRabbitMQ.RabbitMqDependencyInjectionExtensions
 *
 * RabbitMQ configuration for the event bus.
 *
 * Each service must provide an EventBusSubscriptions bean listing the event types it consumes.
 * Bindings are created per event type to match .NET's direct exchange routing.
 */
package com.eshop.eventbus.rabbitmq;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "eshop_event_bus";

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Autowired(required = false)
    private EventBusSubscriptions eventBusSubscriptions;

    @Bean
    public DirectExchange eventBusExchange() {
        return new DirectExchange(EXCHANGE_NAME, false, false);
    }

    @Bean
    public Queue eventQueue() {
        return QueueBuilder.durable(applicationName + "_queue")
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME + "_dlx")
                .build();
    }

    /**
     * Create bindings for each subscribed event type.
     * .NET uses the event class simple name as routing key on a direct exchange,
     * so each event type needs its own binding.
     */
    @Bean
    public Declarables eventBindings(Queue eventQueue, DirectExchange eventBusExchange) {
        List<Declarable> bindings = new ArrayList<>();
        if (eventBusSubscriptions != null) {
            for (String eventName : eventBusSubscriptions.getEventNames()) {
                bindings.add(BindingBuilder.bind(eventQueue).to(eventBusExchange).with(eventName));
            }
        }
        return new Declarables(bindings);
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
        // Match .NET JSON serialization: PascalCase property names and ISO date format
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // .NET events may have extra fields the Java class doesn't need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
}
