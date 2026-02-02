/**
 * Converted from: src/WebApp/Services/BasketService.cs
 *
 * API client for basket service.
 */
import axios from 'axios'
import { CustomerBasket } from '../types'

const API_URL = import.meta.env.VITE_BASKET_URL || '/api/basket'

export const basketApi = {
  async getBasket(buyerId: string): Promise<CustomerBasket> {
    const response = await axios.get(`${API_URL}/${buyerId}`)
    return response.data
  },

  async updateBasket(basket: CustomerBasket): Promise<CustomerBasket> {
    const response = await axios.post(API_URL, basket)
    return response.data
  },

  async deleteBasket(buyerId: string): Promise<void> {
    await axios.delete(`${API_URL}/${buyerId}`)
  }
}
