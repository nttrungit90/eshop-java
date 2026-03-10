# Service Defaults Migration

**Status:** DONE
**.NET Source:** `src/eShop.ServiceDefaults/`
**Java Module:** `common/service-defaults`

## Technology Mapping

| .NET | Java |
|------|------|
| IHostApplicationBuilder extensions | Spring Boot AutoConfiguration |
| AddOpenTelemetry() | Micrometer + micrometer-tracing-bridge-otel |
| AddHealthChecks() | Spring Boot Actuator |
| AddServiceDiscovery() | N/A (fixed ports via config) |
| AddStandardResilienceHandler() | Resilience4j |
| IdentityServer auth | Spring Security OAuth2 Resource Server |

## File Mapping

| .NET File | Java File |
|-----------|-----------|
| `Extensions.cs` (ConfigureOpenTelemetry) | `config/ServiceDefaultsAutoConfiguration.java` |
| `Extensions.cs` (AddDefaultHealthChecks) | `config/ServiceDefaultsAutoConfiguration.java` |
| `AuthenticationExtensions.cs` | `security/JwtSecurityConfig.java` |
| `HttpClientExtensions.cs` | `http/ResilientHttpClientConfig.java` |
| `eShop.ServiceDefaults.csproj` | `pom.xml` |

## Dependencies Provided

| Feature | Dependency |
|---------|-----------|
| Web framework | spring-boot-starter-web |
| Health/metrics | spring-boot-starter-actuator |
| Auth | spring-boot-starter-security + oauth2-resource-server |
| Validation | spring-boot-starter-validation |
| Dashboard | spring-boot-admin-starter-client |
| Tracing | micrometer-tracing-bridge-otel + opentelemetry-exporter-otlp |
| Metrics export | micrometer-registry-otlp |
| API docs | springdoc-openapi-starter-webmvc-ui |
| Resilience | resilience4j-spring-boot3 |
