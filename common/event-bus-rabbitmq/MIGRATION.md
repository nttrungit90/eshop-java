# Event Bus RabbitMQ Migration

**Status:** DONE
**.NET Source:** `src/EventBusRabbitMQ/`
**Java Module:** `common/event-bus-rabbitmq`

## Technology Mapping

| .NET | Java |
|------|------|
| RabbitMQ.Client | spring-boot-starter-amqp (Spring AMQP) |
| Manual connection management | Spring RabbitTemplate + ConnectionFactory |
| ActivitySource tracing | Micrometer Observation + micrometer-tracing-bridge-otel |

## File Mapping

| .NET File | Java File |
|-----------|-----------|
| `RabbitMQEventBus.cs` | `RabbitMQEventBus.java` |
| `RabbitMQTelemetry.cs` | `RabbitMQTelemetry.java` |
| (DI registration in Program.cs) | `RabbitMQConfig.java` |
