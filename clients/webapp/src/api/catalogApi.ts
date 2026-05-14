/**
 * Catalog API client — matches Java catalog-service routes.
 */
import client from './client'
import { CatalogItem, CatalogBrand, CatalogType, PaginatedResponse } from '../types'

const API_URL = '/api/catalog'

export const catalogApi = {
  async getItems(pageIndex = 0, pageSize = 12): Promise<PaginatedResponse<CatalogItem>> {
    const { data } = await client.get(`${API_URL}/items`, {
      params: { pageIndex, pageSize },
    })
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
   */
  pictureUrl(item: CatalogItem): string {
    return item.pictureUri || `${API_URL}/items/${item.id}/pic`
  },
}
