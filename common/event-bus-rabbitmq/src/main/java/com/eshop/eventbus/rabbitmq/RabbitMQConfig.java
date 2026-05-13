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
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
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

    /**
     * ObjectMapper for RabbitMQ message serialization only.
     * Uses PascalCase to match .NET event bus JSON format.
     * Exposed as a named bean so event listeners can inject it via @Qualifier.
     * NOT marked @Primary so Spring MVC keeps using its default camelCase ObjectMapper.
     */
    @Bean("eventBusObjectMapper")
    public ObjectMapper eventBusObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    /**
     * Ensure Spring Boot's default ObjectMapper is available for Spring MVC.
     * Without this, JacksonAutoConfiguration skips creating one because eventBusObjectMapper exists.
     */
    @Bean
    @org.springframework.context.annotation.Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(eventBusObjectMapper());
        // Cross-service messages carry a __TypeId__ header with the publisher's FQN
        // (e.g. com.eshop.orderprocessor.events.GracePeriodConfirmedIntegrationEvent),
        // which doesn't exist in subscriber classpaths. DefaultClassMapper's setDefaultType only
        // applies when the header is missing — not when it points to an unresolvable class.
        // Override toClass() to fall back to a generic Map so conversion succeeds; @RabbitListener
        // handlers receive the raw Message and re-parse the body with their service-local class.
        DefaultClassMapper classMapper = new DefaultClassMapper() {
            @Override
            public Class<?> toClass(org.springframework.amqp.core.MessageProperties properties) {
                try {
                    return super.toClass(properties);
                } catch (Exception e) {
                    return java.util.LinkedHashMap.class;
                }
            }
        };
        classMapper.setDefaultType(java.util.LinkedHashMap.class);
        classMapper.setTrustedPackages("*");
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(EXCHANGE_NAME);
        return template;
    }

    /**
     * Override Spring Boot's default container factory so @RabbitListener consumers
     * use OUR tolerant Jackson converter (the one above with the LinkedHashMap default
     * class mapper). Without this, the default container factory installs its own
     * Jackson2JsonMessageConverter that fails on unknown __TypeId__ classes.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
