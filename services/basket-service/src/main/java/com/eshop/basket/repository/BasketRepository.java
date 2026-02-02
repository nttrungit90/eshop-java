/**
 * Converted from: src/Basket.API/Repositories/RedisBasketRepository.cs
 * .NET Class: eShop.Basket.API.Repositories.RedisBasketRepository
 *
 * Redis repository for customer baskets.
 */
package com.eshop.basket.repository;

import com.eshop.basket.model.CustomerBasket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class BasketRepository {

    private static final Logger log = LoggerFactory.getLogger(BasketRepository.class);
    private static final String BASKET_KEY_PREFIX = "basket:";
    private static final long BASKET_TTL_DAYS = 30;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public BasketRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<CustomerBasket> getBasket(String buyerId) {
        String key = BASKET_KEY_PREFIX + buyerId;
        String data = redisTemplate.opsForValue().get(key);

        if (data == null) {
            return Optional.empty();
        }

        try {
            CustomerBasket basket = objectMapper.readValue(data, CustomerBasket.class);
            return Optional.of(basket);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize basket for buyer {}", buyerId, e);
            return Optional.empty();
        }
    }

    public CustomerBasket updateBasket(CustomerBasket basket) {
        String key = BASKET_KEY_PREFIX + basket.getBuyerId();

        try {
            String data = objectMapper.writeValueAsString(basket);
            redisTemplate.opsForValue().set(key, data, BASKET_TTL_DAYS, TimeUnit.DAYS);
            log.info("Updated basket for buyer {}", basket.getBuyerId());
            return basket;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize basket for buyer {}", basket.getBuyerId(), e);
            throw new RuntimeException("Failed to save basket", e);
        }
    }

    public boolean deleteBasket(String buyerId) {
        String key = BASKET_KEY_PREFIX + buyerId;
        Boolean deleted = redisTemplate.delete(key);
        log.info("Deleted basket for buyer {}: {}", buyerId, deleted);
        return Boolean.TRUE.equals(deleted);
    }
}
