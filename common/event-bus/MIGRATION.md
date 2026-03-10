# Event Bus Migration

**Status:** DONE
**.NET Source:** `src/EventBus/`
**Java Module:** `common/event-bus`

## Technology Mapping

| .NET | Java |
|------|------|
| IEventBus interface | EventBus interface |
| IIntegrationEventHandler\<T\> | IntegrationEventHandler\<T\> |
| IntegrationEvent base class | IntegrationEvent base class |

## File Mapping

| .NET File | Java File |
|-----------|-----------|
| `Abstractions/IEventBus.cs` | `EventBus.java` |
| `Abstractions/IIntegrationEventHandler.cs` | `IntegrationEventHandler.java` |
| `Events/IntegrationEvent.cs` | `IntegrationEvent.java` |
