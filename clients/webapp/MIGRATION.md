# WebApp Migration

**Status:** TODO (priority #8 — after all backend services)
**.NET Source:** `src/WebApp/` (Blazor SSR)
**Java Module:** `clients/webapp` (React + TypeScript)
**Port:** 8080

## Technology Mapping

| .NET | Java/JS |
|------|---------|
| Blazor SSR | React + TypeScript |
| Razor Components | React Components (TSX) |
| HttpClient (C#) | fetch / axios |
| Blazor auth state | OIDC client (oidc-client-ts) |
| Server-side rendering | Vite SPA |

## File Mapping

| Blazor File | React File |
|-------------|------------|
| `Components/Layout/HeaderBar.razor` | `src/components/layout/Header.tsx` |
| `Components/Pages/Catalog.razor` | `src/components/catalog/CatalogPage.tsx` |
| `Components/Pages/Cart.razor` | `src/components/cart/CartPage.tsx` |
| `Components/Pages/OrderDetail.razor` | `src/components/orders/OrderDetailPage.tsx` |
| `Services/BasketService.cs` | `src/api/basketApi.ts` |
| `Services/OrderingService.cs` | `src/api/orderingApi.ts` |
| `Services/CatalogService.cs` | `src/api/catalogApi.ts` |

## Dependencies

- All backend Java services must be running
- Identity service for OIDC auth flow

## Migration Notes

- React SPA replaces Blazor SSR — different rendering model
- API calls go directly to backend services (no BFF needed for SPA)
- Auth via OIDC authorization code flow with PKCE
- Environment variables for service URLs (VITE_CATALOG_URL, etc.)
