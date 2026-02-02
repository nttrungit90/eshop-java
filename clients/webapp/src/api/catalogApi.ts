/**
 * Converted from: src/WebApp/Services/CatalogService.cs
 *
 * API client for catalog service.
 */
import axios from 'axios'
import { CatalogItem, CatalogBrand, CatalogType, PaginatedResponse } from '../types'

const API_URL = import.meta.env.VITE_CATALOG_URL || '/api/catalog'

export const catalogApi = {
  async getItems(pageIndex = 0, pageSize = 10): Promise<PaginatedResponse<CatalogItem>> {
    const response = await axios.get(`${API_URL}/items`, {
      params: { pageIndex, pageSize }
    })
    return response.data
  },

  async getItemById(id: number): Promise<CatalogItem> {
    const response = await axios.get(`${API_URL}/items/${id}`)
    return response.data
  },

  async getItemsByName(name: string, pageIndex = 0, pageSize = 10): Promise<PaginatedResponse<CatalogItem>> {
    const response = await axios.get(`${API_URL}/items/by-name/${name}`, {
      params: { pageIndex, pageSize }
    })
    return response.data
  },

  async getCatalogTypes(): Promise<CatalogType[]> {
    const response = await axios.get(`${API_URL}/catalog-types`)
    return response.data
  },

  async getCatalogBrands(): Promise<CatalogBrand[]> {
    const response = await axios.get(`${API_URL}/catalog-brands`)
    return response.data
  }
}
