/**
 * Converted from: src/WebApp/Services/OrderingService.cs
 *
 * API client for ordering service.
 */
import axios from 'axios'
import { Order } from '../types'

const API_URL = import.meta.env.VITE_ORDERING_URL || '/api/orders'

export interface CreateOrderRequest {
  city: string
  street: string
  state: string
  country: string
  zipCode: string
  cardNumber: string
  cardHolderName: string
  cardExpiration: string
  cardSecurityNumber: string
  cardTypeId: number
  items: {
    productId: number
    productName: string
    unitPrice: number
    discount: number
    units: number
    pictureUrl: string
  }[]
}

export const orderingApi = {
  async getOrders(token?: string): Promise<Order[]> {
    const response = await axios.get(API_URL, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    return response.data
  },

  async getOrder(orderId: number, token?: string): Promise<Order> {
    const response = await axios.get(`${API_URL}/${orderId}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    return response.data
  },

  async createOrder(order: CreateOrderRequest, token?: string): Promise<Order> {
    const response = await axios.post(API_URL, order, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    return response.data
  },

  async cancelOrder(orderId: number, token?: string): Promise<void> {
    await axios.put(`${API_URL}/${orderId}/cancel`, {}, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
  }
}
