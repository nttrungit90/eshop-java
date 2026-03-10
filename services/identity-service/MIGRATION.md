# Identity Service Migration

**Status:** TODO (priority #7 — last)
**.NET Source:** `src/Identity.API/`
**Java Module:** `services/identity-service`
**Port:** 9100

## Technology Mapping

| .NET | Java |
|------|------|
| Duende IdentityServer | Spring Authorization Server |
| ASP.NET Core Identity | Spring Security UserDetailsService |
| EF Core (identitydb) | Spring Data JPA |
| OIDC / OAuth2 flows | Spring Authorization Server OIDC |
| Razor Pages (login UI) | Thymeleaf / static HTML |

## File Mapping

| .NET File | Java File |
|-----------|-----------|
| `Identity.API/Program.cs` | `IdentityServiceApplication.java` |
| `Identity.API/Configuration/Config.cs` | `config/AuthorizationServerConfig.java` |
| `Identity.API/Models/ApplicationUser.cs` | `model/ApplicationUser.java` |
| `Identity.API/Data/ApplicationDbContext.cs` | JPA repositories |
| `Identity.API/Pages/Account/Login.cshtml` | Login page (Thymeleaf) |
| `Identity.API/Pages/Account/Register.cshtml` | Register page (Thymeleaf) |

## Dependencies

- PostgreSQL (identitydb)

## Client Registrations (from .NET Config.cs)

| Client | Grant Type | Redirect URI |
|--------|-----------|--------------|
| webapp | authorization_code | http://localhost:8080/callback |
| basket-api | client_credentials | - |
| ordering-api | client_credentials | - |
| webhooks-api | client_credentials | - |
| webhooksclient | authorization_code | (webhook client callback) |

## How to Run

While migrating (run separately for debugging):
```bash
# Terminal 1: .NET infra + remaining .NET services (still runs .NET Identity)
dotnet run --project src/eShop.AppHost

# Terminal 2: Already-migrated Java services
cd eshop-java && docker compose up -d

# Terminal 3: This service (debug — test alongside .NET Identity before switching)
cd eshop-java && ./mvnw -pl services/identity-service spring-boot:run -Dspring-boot.run.arguments=--server.port=9100
```

Once stable, add to `docker-compose.yml`, disable .NET Identity.API in AppHost, and point all services to Java identity.

## Migration Notes

- Migrate LAST — all other services depend on identity for JWT validation
- During migration, .NET Identity.API continues serving tokens to both .NET and Java services
- Must preserve: issuer URL, signing keys, token format, OIDC discovery endpoint
- Java services already validate JWTs via `spring-boot-starter-oauth2-resource-server`
- Client registrations and user data must be compatible
- .NET AppHost: remove Identity.API project reference, point all services to Java identity
