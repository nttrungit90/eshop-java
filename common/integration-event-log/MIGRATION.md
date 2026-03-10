# Integration Event Log Migration

**Status:** DONE
**.NET Source:** `src/IntegrationEventLogEF/`
**Java Module:** `common/integration-event-log`

## Technology Mapping

| .NET | Java |
|------|------|
| Entity Framework Core (DbContext) | Spring Data JPA |
| EF Core transactions | Spring @Transactional |
| Outbox pattern via EF | Outbox pattern via JPA |

## File Mapping

| .NET File | Java File |
|-----------|-----------|
| `IntegrationEventLogEntry.cs` | `IntegrationEventLogEntry.java` |
| `Services/IntegrationEventLogService.cs` | `IntegrationEventLogService.java` |
| `Services/IIntegrationEventLogService.cs` | `IntegrationEventLogService.java` (interface + impl) |
