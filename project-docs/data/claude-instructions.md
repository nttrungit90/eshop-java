# Claude Migration Instructions

Rules for Claude to follow when executing the migration plan.

## Execution Loop

1. Read `project-docs/data/migration-plan.md` and `project-docs/data/migration-progress.md`
2. Read `project-docs/data/tasks.yaml` for the structured task details
3. Identify the next unfinished task (first unchecked `- [ ]` in migration-plan.md, confirmed by migration-progress.md)
4. Implement ONLY that single task in the codebase
5. Verify the code compiles (`./mvnw compile -pl <module> -am`)
6. Update progress:
   - Mark the task as `- [x]` in `project-docs/data/migration-plan.md`
   - Update `project-docs/data/migration-progress.md` with current phase, completed task, next task
   - Update task status in `project-docs/data/tasks.yaml`
7. Commit: `migration: complete task <task-id>`

## Rules

- **One task at a time.** Never execute multiple tasks in a single pass.
- **Always read progress first.** Before starting work, read migration-progress.md so the process can resume safely across sessions.
- **Verify compilation** after code changes. If it fails, fix before marking complete.
- **If a task is unclear, stop and ask.** Do not guess.
- **Do not modify unrelated code.** Only touch files relevant to the current task.
- **Never commit in the eShop .NET repo** (`/Users/trung/Documents/nttrungit90-github/eShop/`). You may edit files there (e.g., AppHost Program.cs), but do NOT `git add` or `git commit` in that repo. The user will review and commit .NET changes manually.

## Reference Files

When implementing a task, consult:

- **MIGRATION.md** in the service directory — has file mappings, technology mappings, integration events
- **.NET source** at `/Users/trung/Documents/nttrungit90-github/eShop/src/` — the original implementation to convert from
- **Catalog service** (`services/catalog-service/`) — reference for how a completed migration looks (event handling, config, etc.)
- **Event bus** (`common/event-bus/`, `common/event-bus-rabbitmq/`) — how events are published/consumed

## Conventions

### Java Code
- Package: `com.eshop.<servicename>`
- Events extend `com.eshop.eventbus.IntegrationEvent`
- Event handlers use `@RabbitListener` with queue name `{spring.application.name}_queue`
- Publishing via `EventBus.publishAsync(event)`
- Configuration via `@ConfigurationProperties`
- Use constructor injection, not field injection

### application.yml
- Aspire-managed RabbitMQ credentials: `guest` / `WBpzyj95KTuVkpGxR5Fx1j`
- Aspire-managed Postgres credentials: `postgres` / `71UhdH_{f7C+yPyrh92RRW`
- OTLP tracing: `http://localhost:4318/v1/traces`
- OTLP metrics: `http://localhost:4318/v1/metrics`
- Spring Boot Admin: `http://localhost:9090`
- Server address: `0.0.0.0`

### docker-compose.yml
- Services connect to Aspire infra via `host.docker.internal`
- Each service gets OTLP env vars pointing to jaeger container
- Spring Boot Admin client URL points to admin container

### .NET AppHost
- Add `bool useJava<ServiceName> = true/false` flag
- When true: skip .NET project, set env var for direct URL
- When false: original .NET behavior

## Startup During Migration

```
Terminal 1: dotnet run --project src/eShop.AppHost          # .NET infra + unmigrated services
Terminal 2: cd eshop-java && docker compose up -d            # migrated Java services (stable)
Terminal 3: cd eshop-java && ./mvnw -pl services/<x> spring-boot:run  # service being debugged
```
