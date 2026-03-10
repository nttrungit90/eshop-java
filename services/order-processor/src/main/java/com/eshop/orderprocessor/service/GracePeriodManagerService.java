package com.eshop.orderprocessor.service;

import com.eshop.eventbus.EventBus;
import com.eshop.orderprocessor.config.BackgroundTaskOptions;
import com.eshop.orderprocessor.events.GracePeriodConfirmedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GracePeriodManagerService {

    private static final Logger log = LoggerFactory.getLogger(GracePeriodManagerService.class);

    private final BackgroundTaskOptions options;
    private final EventBus eventBus;
    private final JdbcTemplate jdbcTemplate;

    public GracePeriodManagerService(BackgroundTaskOptions options, EventBus eventBus, JdbcTemplate jdbcTemplate) {
        this.options = options;
        this.eventBus = eventBus;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedDelayString = "${background-task.check-update-time:30}000")
    public void checkConfirmedGracePeriodOrders() {
        log.debug("Checking confirmed grace period orders");

        List<Integer> orderIds = getConfirmedGracePeriodOrders();

        for (int orderId : orderIds) {
            var confirmGracePeriodEvent = new GracePeriodConfirmedIntegrationEvent(orderId);

            log.info("Publishing integration event: {} - ({})", confirmGracePeriodEvent.getId(),
                    confirmGracePeriodEvent.getClass().getSimpleName());

            eventBus.publishAsync(confirmGracePeriodEvent);
        }
    }

    private List<Integer> getConfirmedGracePeriodOrders() {
        try {
            return jdbcTemplate.queryForList(
                    """
                    SELECT "Id"
                    FROM ordering.orders
                    WHERE CURRENT_TIMESTAMP - "OrderDate" >= make_interval(mins => ?) AND "OrderStatus" = 'Submitted'
                    """,
                    Integer.class,
                    options.getGracePeriodTime()
            );
        } catch (Exception e) {
            log.error("Error querying grace period orders", e);
            return List.of();
        }
    }
}
