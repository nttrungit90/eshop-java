/**
 * Converted from: src/IntegrationEventLogEF/Services/IIntegrationEventLogService.cs
 * .NET Interface: eShop.IntegrationEventLogEF.Services.IIntegrationEventLogService
 *
 * Repository for integration event log entries.
 */
package com.eshop.eventbus.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntegrationEventLogRepository extends JpaRepository<IntegrationEventLogEntry, UUID> {

    @Query("SELECT e FROM IntegrationEventLogEntry e WHERE e.transactionId = :transactionId AND e.state = 'NOT_PUBLISHED' ORDER BY e.creationTime")
    List<IntegrationEventLogEntry> findPendingEventsByTransactionId(String transactionId);

    @Query("SELECT e FROM IntegrationEventLogEntry e WHERE e.state = 'NOT_PUBLISHED' ORDER BY e.creationTime")
    List<IntegrationEventLogEntry> findAllPendingEvents();

    @Query("SELECT e FROM IntegrationEventLogEntry e WHERE e.state = 'PUBLISHED_FAILED' AND e.timesSent < 3 ORDER BY e.creationTime")
    List<IntegrationEventLogEntry> findFailedEventsForRetry();
}
