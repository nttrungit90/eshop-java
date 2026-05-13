package com.eshop.ordering.api.application.integrationevents;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderingIntegrationEventLogRepository extends JpaRepository<OrderingIntegrationEventLogEntry, UUID> {

    @Query("SELECT e FROM OrderingIntegrationEventLogEntry e WHERE e.state = com.eshop.ordering.api.application.integrationevents.OrderingIntegrationEventLogEntry$EventState.NotPublished ORDER BY e.creationTime")
    List<OrderingIntegrationEventLogEntry> findAllPendingEvents();

    @Query("SELECT e FROM OrderingIntegrationEventLogEntry e WHERE e.state = com.eshop.ordering.api.application.integrationevents.OrderingIntegrationEventLogEntry$EventState.PublishedFailed AND e.timesSent < 3 ORDER BY e.creationTime")
    List<OrderingIntegrationEventLogEntry> findFailedEventsForRetry();
}
