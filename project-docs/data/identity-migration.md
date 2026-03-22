# Identity Migration: .NET Duende IdentityServer → Keycloak

## Overview

The eShop platform originally used **Duende IdentityServer 7** (in the `Identity.API` .NET project) as its OAuth2/OpenID Connect provider. As part of the .NET → Java migration, Identity.API was replaced with **Keycloak 26.1**, a standalone open-source identity provider. This document covers the full migration: technology mapping, configuration translation, authentication flows, issues encountered, and resolutions.

---

## 1. Technology Mapping

| Concept | .NET (Duende IdentityServer) | Java (Keycloak 26.1) |
|---------|------------------------------|----------------------|
| **Identity Provider** | Duende IdentityServer 7 (embedded in ASP.NET app) | Keycloak 26.1 (standalone Docker container) |
| **User Store** | ASP.NET Core Identity + PostgreSQL (`identitydb`) | Keycloak internal store (realm JSON import) |
| **Configuration** | C# code (`Config.cs`) + `appsettings.json` | Declarative realm JSON (`eshop-realm.json`) |
| **Token Format** | JWT with `typ: at+jwt` (RFC 9068) | JWT with `typ: JWT` |
| **Token Signing** | Developer signing credential (`tempkey.jwk`) | Keycloak-managed RSA keys (auto-rotated) |
| **Password Hashing** | ASP.NET Identity (PBKDF2) | Keycloak (bcrypt) |
| **Admin UI** | None (code-only configuration) | Keycloak Admin Console (`http://localhost:8180/admin`) |
| **OIDC Discovery** | `http://localhost:5223/.well-known/openid-configuration` | `http://localhost:8180/realms/eshop/.well-known/openid-configuration` |
| **Port** | 5223 | 8180 |

---

## 2. Client Migration Mapping

### Migrated Clients

| .NET Client | Keycloak Client | Grant Type | Changes |
|-------------|-----------------|------------|---------|
| `webapp` (Blazor Server) | `webapp` (confidential) | Authorization Code | Same client ID/secret. Added exact redirect URIs for Keycloak strict validation |
| `webhooksclient` | `webhooksclient` (confidential) | Authorization Code | Same configuration |
| `maui` (mobile) | Replaced by `mobilebff` | Authorization Code | Mobile now goes through BFF proxy instead of direct OIDC |
| `basketswaggerui` | Not migrated | Implicit | Swagger UI auth not needed in Java services |
| `orderingswaggerui` | Not migrated | Implicit | Swagger UI auth not needed in Java services |
| `webhooksswaggerui` | Not migrated | Implicit | Swagger UI auth not needed in Java services |

### New Clients (Keycloak-only)

| Client | Type | Purpose |
|--------|------|---------|
| `webapp-spa` | Public (PKCE) | React SPA frontend (future use) |
| `basket` | Service Account (Client Credentials) | Basket service-to-service auth |
| `orders` | Service Account (Client Credentials) | Ordering service-to-service auth |

---

## 3. Scope Migration

### Identity Resources → Client Scopes

| .NET Identity Resource | Keycloak Client Scope | Notes |
|------------------------|-----------------------|-------|
| `openid` (built-in) | `openid` | **Had to be explicitly created** — Keycloak realm import overwrote built-in scopes |
| `profile` (built-in) | `profile` | Same issue — explicitly defined with protocol mappers |

### API Scopes → Client Scopes

| .NET API Scope | Keycloak Scope | Assigned To |
|----------------|----------------|-------------|
| `orders` | `orders` | webapp, webapp-spa, mobilebff, orders (service account) |
| `basket` | `basket` | webapp, webapp-spa, mobilebff, basket (service account) |
| `webhooks` | `webhooks` | webhooksclient |
| `webshoppingagg` | Not migrated | Aggregator not yet migrated |
| `mobileshoppingagg` | Not migrated | Mobile aggregator not yet migrated |

---

## 4. Custom Claims Migration

.NET Identity.API issued custom claims via `ProfileService.GetClaimsFromUser()`. In Keycloak, these are implemented as **protocol mappers** on the `eshop-custom-claims` client scope.

| Claim | .NET Source | Keycloak Mapper Type | Token Inclusion |
|-------|-------------|----------------------|-----------------|
| `name` | `user.Name` | `oidc-usermodel-attribute-mapper` (firstName) | ID token, access token, userinfo |
| `last_name` | `user.LastName` | `oidc-usermodel-attribute-mapper` (lastName) | ID token, access token, userinfo |
| `preferred_username` | `user.UserName` | `oidc-usermodel-attribute-mapper` (username) | ID token, access token, userinfo |
| `card_number` | `user.CardNumber` | `oidc-usermodel-attribute-mapper` | Access token only |
| `card_holder` | `user.CardHolderName` | `oidc-usermodel-attribute-mapper` | Access token only |
| `card_security_number` | `user.SecurityNumber` | `oidc-usermodel-attribute-mapper` | Access token only |
| `card_expiration` | `user.Expiration` | `oidc-usermodel-attribute-mapper` | Access token only |
| `address_street` | `user.Street` | `oidc-usermodel-attribute-mapper` | Access token only |
| `address_city` | `user.City` | `oidc-usermodel-attribute-mapper` | Access token only |
| `address_state` | `user.State` | `oidc-usermodel-attribute-mapper` | Access token only |
| `address_country` | `user.Country` | `oidc-usermodel-attribute-mapper` | Access token only |
| `address_zip_code` | `user.ZipCode` | `oidc-usermodel-attribute-mapper` | Access token only |

---

## 5. User Migration

| User | Password | Keycloak Attributes |
|------|----------|---------------------|
| `alice` (AliceSmith@email.com) | `Pass123$` | card_number: 4012888888881881, card_holder: Alice Smith, card_security_number: 123, card_expiration: 12/25, address: 15703 NE 61st Ct, Redmond, WA 98052 |
| `bob` (BobSmith@email.com) | `Pass123$` | card_number: 4012888888881881, card_holder: Bob Smith, card_security_number: 456, card_expiration: 12/25, address: One Microsoft Way, Redmond, WA 98052 |

**Note**: .NET stored user attributes as database columns on `ApplicationUser`. Keycloak stores them as user attributes (key-value pairs), extracted to JWT via protocol mappers.

---

## 6. Authentication Flow

### Before (with .NET Identity.API)

```
Browser → WebApp (.NET Blazor)
    → OIDC redirect to Identity.API (localhost:5223)
    → User logs in on Identity.API login page
    → Authorization Code returned to WebApp
    → WebApp exchanges code for tokens
    → Access token forwarded to APIs via HttpClientAuthorizationDelegatingHandler
    → APIs validate JWT against Identity.API JWKS endpoint
```

### After (with Keycloak)

```
Browser → WebApp (.NET Blazor)
    → OIDC redirect to Keycloak (localhost:8180/realms/eshop)
    → User logs in on Keycloak login page
    → Authorization Code returned to WebApp
    → WebApp exchanges code for tokens
    → Access token forwarded to APIs via HttpClientAuthorizationDelegatingHandler
    → APIs validate JWT against Keycloak JWKS endpoint
```

### Token Forwarding (unchanged)

The `HttpClientAuthorizationDelegatingHandler` in `eShop.ServiceDefaults` extracts the saved access token from the HTTP context and adds it as a `Bearer` header to all outgoing API calls. This mechanism is identity-provider agnostic.

### Java Service JWT Validation

Java services validate tokens using Spring Security with a custom `NimbusJwtDecoder` that accepts both `typ: JWT` (Keycloak) and `typ: at+jwt` (.NET IdentityServer):

```java
NimbusJwtDecoder.withJwkSetUri(jwksUri)
    .jwtProcessorCustomizer(processor -> {
        processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
            JOSEObjectType.JWT, new JOSEObjectType("at+jwt")));
    })
    .build();
```

---

## 7. .NET AppHost Configuration Changes

### Identity URL

```csharp
// Before: .NET Identity.API was a project reference
var identityApi = builder.AddProject<Projects.Identity_API>("identity-api")
    .WithReference(identityDb);

// After: Keycloak is external, referenced by URL
var keycloakUrl = "http://localhost:8180/realms/eshop";
webApp.WithEnvironment("IdentityUrl", keycloakUrl);
```

### WebApp OIDC Configuration

The `WebApp/Extensions/Extensions.cs` OIDC setup required one change:

```csharp
.AddOpenIdConnect(options =>
{
    options.Authority = identityUrl;  // Now points to Keycloak
    options.ClientId = "webapp";
    options.ClientSecret = "secret";
    // ... existing config unchanged ...

    // NEW: Disable PAR for Keycloak compatibility
    options.PushedAuthorizationBehavior = PushedAuthorizationBehavior.Disable;
});
```

---

## 8. Keycloak Deployment

### Docker Compose

```yaml
keycloak:
  image: quay.io/keycloak/keycloak:26.1
  container_name: eshop-keycloak
  command: start-dev --import-realm
  ports:
    - "8180:8080"
  environment:
    KC_BOOTSTRAP_ADMIN_USERNAME: admin
    KC_BOOTSTRAP_ADMIN_PASSWORD: admin
    KC_HOSTNAME: http://localhost:8180
    KC_HEALTH_ENABLED: "true"
  volumes:
    - ./infrastructure/keycloak/eshop-realm.json:/opt/keycloak/data/import/eshop-realm.json:ro
```

### Realm Import

Keycloak uses `--import-realm` flag to auto-import the realm JSON on first startup. The realm file at `infrastructure/keycloak/eshop-realm.json` is the single source of truth for all identity configuration.

---

## 9. Issues Encountered & Resolutions

### Issue 1: Pushed Authorization Request (PAR) Errors

**Symptom**: `PUSHED_AUTHORIZATION_REQUEST_ERROR` — `invalid_redirect_uri`

**Root Cause**: .NET 9+ (and .NET 10) `AddOpenIdConnect` **automatically uses PAR** when the OIDC discovery document advertises a PAR endpoint. Keycloak 26 advertises PAR by default. However, Keycloak validates redirect URIs more strictly on the PAR endpoint than on the standard authorization endpoint — wildcard patterns like `https://localhost:*/signin-oidc` were rejected.

**Resolution**: Disabled PAR in the .NET WebApp OIDC configuration:
```csharp
options.PushedAuthorizationBehavior = PushedAuthorizationBehavior.Disable;
```

**File**: `eShop/src/WebApp/Extensions/Extensions.cs`

---

### Issue 2: Redirect URI Rejected (even after PAR fix)

**Symptom**: `LOGIN_ERROR` — `invalid_redirect_uri` for `https://localhost:7298/signin-oidc`

**Root Cause**: Keycloak 26 does not match wildcard redirect URI patterns (`https://localhost:*/signin-oidc`) reliably. The standard authorization endpoint also rejected the wildcard.

**Resolution**: Added exact redirect URIs alongside the wildcards in the Keycloak webapp client:
```json
"redirectUris": [
    "https://localhost:7298/signin-oidc",
    "http://localhost:5045/signin-oidc",
    "https://localhost:*/signin-oidc",
    "http://localhost:*/signin-oidc"
]
```

Updated both the live Keycloak instance (via Admin API) and the realm JSON file.

**File**: `eshop-java/infrastructure/keycloak/eshop-realm.json`

---

### Issue 3: Invalid Scopes (openid, profile)

**Symptom**: `invalid_scope` — `Invalid scopes: openid profile orders basket`

**Root Cause**: The realm JSON only defined custom scopes (`orders`, `basket`, `webhooks`, `eshop-custom-claims`) in the `clientScopes` array and only `eshop-custom-claims` in `defaultDefaultClientScopes`. When Keycloak imports a realm with explicit `clientScopes`, it **replaces** the built-in scopes rather than merging. This removed the standard `openid` and `profile` scopes that Keycloak normally creates automatically.

**Resolution**: Explicitly defined `openid` and `profile` scopes in the realm JSON with appropriate protocol mappers, and added them to `defaultDefaultClientScopes`:

```json
"clientScopes": [
    {
      "name": "openid",
      "protocol": "openid-connect",
      "protocolMappers": [{ "name": "sub", "protocolMapper": "oidc-sub-mapper" }]
    },
    {
      "name": "profile",
      "protocol": "openid-connect",
      "protocolMappers": [
        { "name": "username", "protocolMapper": "oidc-usermodel-attribute-mapper", ... },
        { "name": "full name", "protocolMapper": "oidc-full-name-mapper", ... }
      ]
    },
    ...
],
"defaultDefaultClientScopes": ["openid", "profile", "eshop-custom-claims"]
```

**File**: `eshop-java/infrastructure/keycloak/eshop-realm.json`

---

### Issue 4: JWT `at+jwt` Type Header (pre-existing)

**Symptom**: Spring Security rejected tokens with `JOSE header typ (type) at+jwt not allowed`

**Root Cause**: .NET Duende IdentityServer issues access tokens with `typ: at+jwt` per RFC 9068. Spring Security's default `NimbusJwtDecoder` only accepts `typ: JWT`. This was an issue during the migration period when both .NET and Java services coexisted.

**Resolution**: Custom JWT decoder in Java services that accepts both types. See `common/service-defaults/.../security/JwtSecurityConfig.java`.

**Note**: Keycloak access tokens actually use `typ: Bearer` (not `JWT`), so the decoder must accept `JWT`, `at+jwt`, and `Bearer`.

---

### Issue 5: JWKS URI Unreachable from Docker (hostname mismatch)

**Symptom**: `Couldn't retrieve remote JWK set: 404 Not Found` on JWKS URL

**Root Cause**: `JwtDecoders.fromIssuerLocation()` fetches the OIDC discovery document, which returns `jwks_uri: http://localhost:8180/...` (based on `KC_HOSTNAME`). Inside Docker containers, `localhost` refers to the container itself, not the host — so the JWKS endpoint is unreachable.

**Resolution**: Build the `JwtDecoder` with an explicit JWKS URI derived from `identity.url` instead of relying on discovery:
```java
String jwksUri = identityUrl + "/protocol/openid-connect/certs";
NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
```
This ensures Docker containers use `host.docker.internal:8180` throughout.

**File**: `common/service-defaults/.../security/JwtSecurityConfig.java`

---

### Issue 6: Missing `sub` Claim in Access Tokens

**Symptom**: `UNAUTHENTICATED` on gRPC calls — `getUserIdentity()` returned null

**Root Cause**: When creating the `openid` client scope via Keycloak Admin API, protocol mappers were not included. The built-in Keycloak `openid` scope normally includes a `sub` mapper automatically, but our custom-created scope was empty. Without the mapper, the `sub` claim was absent from access tokens.

**Resolution**: Added `oidc-sub-mapper` protocol mapper to the `openid` client scope, and `oidc-usermodel-attribute-mapper` (preferred_username) + `oidc-full-name-mapper` to the `profile` scope. Updated realm JSON to include these mappers.

**Lesson**: When defining custom `openid`/`profile` scopes in Keycloak realm JSON (to avoid built-in scope overwrite), you must explicitly include all protocol mappers — Keycloak does not auto-add them for custom scopes.

---

### Issue 7: Keycloak Token `typ: Bearer` Rejected

**Symptom**: JWT decode fails silently in Java gRPC service

**Root Cause**: Keycloak access tokens use `typ: Bearer` in the JOSE header (not `typ: JWT`). The custom `NimbusJwtDecoder` was configured to accept `JWT` and `at+jwt` but not `Bearer`.

**Resolution**: Added `Bearer` to the allowed JOSE types:
```java
processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
    JOSEObjectType.JWT, new JOSEObjectType("at+jwt"), new JOSEObjectType("Bearer")));
```

---

## 10. Key Differences & Gotchas

| Area | .NET IdentityServer | Keycloak | Gotcha |
|------|---------------------|----------|--------|
| **Token `typ` header** | `at+jwt` (RFC 9068) | `Bearer` | Java services need custom decoder to accept `JWT`, `at+jwt`, and `Bearer` |
| **Realm import** | N/A (code-based) | Overwrites built-in scopes | Must explicitly define `openid`/`profile` in realm JSON |
| **PAR support** | Not used by .NET < 9 | Advertised by default in KC 26 | .NET 9+/10 auto-enables PAR; must disable or configure exact redirect URIs |
| **Redirect URI matching** | Exact match in code | Wildcards unreliable in KC 26 | Use exact URIs, keep wildcards as fallback only |
| **User attributes** | DB columns on `ApplicationUser` | Key-value attributes + protocol mappers | Each attribute needs a protocol mapper to appear in JWT |
| **Login UI** | Custom Razor views in Identity.API | Keycloak themed login page | Different look/feel; customizable via Keycloak themes |
| **Session management** | ASP.NET cookie + IdentityServer session | Keycloak SSO session | Different idle/max timeouts; configure in realm settings |
| **Token lifetime** | 2h (configured in `Config.cs`) | 2h (configured in realm JSON `accessTokenLifespan: 7200`) | Must match to avoid token expiry issues |

---

## 11. Verification Checklist

- [x] WebApp can authenticate via Keycloak OIDC flow
- [x] Redirect URI correctly accepted
- [x] All required scopes (openid, profile, orders, basket) work
- [x] Custom claims (name, address, card info) present in tokens
- [x] Java services validate Keycloak-issued JWTs
- [x] Java services validate .NET-issued JWTs (backward compatibility)
- [x] Token forwarding from WebApp to APIs works
- [x] WebhookClient can authenticate with `webhooks` scope
- [ ] Logout flow (sign-out redirect) works end-to-end
- [ ] Token refresh works for long sessions
- [ ] Mobile BFF authentication flow validated

---

## 12. Access Reference

| Resource | URL | Credentials |
|----------|-----|-------------|
| Keycloak Admin Console | http://localhost:8180 | admin / admin |
| Keycloak Realm | http://localhost:8180/realms/eshop | — |
| OIDC Discovery | http://localhost:8180/realms/eshop/.well-known/openid-configuration | — |
| JWKS | http://localhost:8180/realms/eshop/protocol/openid-connect/certs | — |
| Test User: alice | — | alice / Pass123$ |
| Test User: bob | — | bob / Pass123$ |
