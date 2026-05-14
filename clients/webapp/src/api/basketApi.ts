/**
 * Basket REST API client — matches Java basket-service REST endpoints (port 9103).
 * (The .NET WebApp used gRPC; we use REST from the SPA for simplicity.)
 */
import client from './client'
import { CustomerBasket } from '../types'

const API_URL = '/api/basket'

export const basketApi = {
  async getBasket(buyerId: string): Promise<CustomerBasket> {
    const { data } = await client.get(`${API_URL}/${buyerId}`)
    return data
  },

  async updateBasket(basket: CustomerBasket): Promise<CustomerBasket> {
    const { data } = await client.post(API_URL, basket)
    return data
  },

  async deleteBasket(buyerId: string): Promise<void> {
    await client.delete(`${API_URL}/${buyerId}`)
  },
}
