/**
 * Catalog API client — matches Java catalog-service routes.
 */
import client from './client'
import { CatalogItem, CatalogBrand, CatalogType, PaginatedResponse } from '../types'

const API_URL = '/api/catalog'

export const catalogApi = {
  /**
   * List items, optionally filtered by brand and/or type. Routes to the
   * Java catalog endpoints that mirror the .NET ones:
   *   - both filters → /items/type/{typeId}/brand/{brandId}
   *   - type only    → /items/type/{typeId}/brand
   *   - brand only   → /items/type/all/brand/{brandId}
   *   - neither      → /items
   */
  async getItems(
    pageIndex = 0,
    pageSize = 12,
    brandId?: number | null,
    typeId?: number | null,
  ): Promise<PaginatedResponse<CatalogItem>> {
    let path = `${API_URL}/items`
    if (typeId != null && brandId != null) {
      path = `${API_URL}/items/type/${typeId}/brand/${brandId}`
    } else if (typeId != null) {
      path = `${API_URL}/items/type/${typeId}/brand`
    } else if (brandId != null) {
      path = `${API_URL}/items/type/all/brand/${brandId}`
    }
    const { data } = await client.get(path, { params: { pageIndex, pageSize } })
    return data
  },

  async getItemById(id: number): Promise<CatalogItem> {
    const { data } = await client.get(`${API_URL}/items/${id}`)
    return data
  },

  async getItemsByName(name: string, pageIndex = 0, pageSize = 12): Promise<PaginatedResponse<CatalogItem>> {
    const { data } = await client.get(`${API_URL}/items/by/${encodeURIComponent(name)}`, {
      params: { pageIndex, pageSize },
    })
    return data
  },

  // Java catalog uses camelCase paths: /catalogTypes, /catalogBrands
  async getCatalogTypes(): Promise<CatalogType[]> {
    const { data } = await client.get(`${API_URL}/catalogTypes`)
    return data
  },

  async getCatalogBrands(): Promise<CatalogBrand[]> {
    const { data } = await client.get(`${API_URL}/catalogBrands`)
    return data
  },

  /**
   * Build the picture URL for a catalog item. The /api/catalog/items/{id}/pic
   * endpoint returns the binary image; goes through the SPA's nginx proxy.
   * <img src> bypasses our axios interceptor, so api-version must be inline.
   */
  pictureUrl(item: CatalogItem): string {
    return item.pictureUri || `${API_URL}/items/${item.id}/pic?api-version=1.0`
  },
}
