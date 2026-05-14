/**
 * Ordering API client — matches the Java ordering-api DDD/CQRS flow.
 *
 * - POST /api/orders requires an `x-requestid` header (UUID) — this is the
 *   idempotency key for the IdentifiedCommand wrapper. Without it the server
 *   rejects with 400. We generate one per checkout call.
 * - The Java CreateOrderRequest payload uses `quantity` (not `units`) and an
 *   ISO Instant for `cardExpiration` ("2027-12-31T00:00:00Z"); the UI form
 *   collects "MM/YY", so we convert here.
 * - The list endpoint returns OrderSummary[]; the detail endpoint returns Order.
 */
import client from './client'
import { Order, OrderSummary } from '../types'

const API_URL = '/api/orders'

export interface CheckoutForm {
  street: string
  city: string
  state: string
  country: string
  zipCode: string
  cardNumber: string
  cardHolderName: string
  /** Free-text "MM/YY" — converted to ISO before send. */
  cardExpiration: string
  cardSecurityNumber: string
  cardTypeId: number
  items: {
    productId: number
    productName: string
    unitPrice: number
    discount: number
    quantity: number
    pictureUrl: string
  }[]
}

function parseExpiration(mmYY: string): string {
  // "12/25" → 2025-12-31T23:59:59Z (last day of the month)
  const m = mmYY.match(/^(\d{1,2})\s*\/\s*(\d{2}|\d{4})$/)
  if (!m) {
    // Allow already-ISO inputs to pass through
    if (mmYY.includes('T')) return mmYY
    throw new Error(`Invalid cardExpiration: ${mmYY} (expected MM/YY)`)
  }
  const month = parseInt(m[1], 10)
  const year = m[2].length === 2 ? 2000 + parseInt(m[2], 10) : parseInt(m[2], 10)
  // last day of month, end of day
  const lastDay = new Date(Date.UTC(year, month, 0, 23, 59, 59))
  return lastDay.toISOString()
}

function uuid(): string {
  // browsers expose crypto.randomUUID() — fall back to a manual v4 for safety
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) return crypto.randomUUID()
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export const orderingApi = {
  async getOrders(): Promise<OrderSummary[]> {
    const { data } = await client.get(API_URL)
    return data
  },

  async getOrder(orderId: number): Promise<Order> {
    const { data } = await client.get(`${API_URL}/${orderId}`)
    return data
  },

  /**
   * Submits the create-order command. The Java side validates + outboxes
   * OrderStartedIntegrationEvent + OrderStatusChangedToSubmittedIntegrationEvent.
   * Returns void; subsequent state transitions are async via the saga.
   */
  async createOrder(form: CheckoutForm, userId: string, userName: string): Promise<void> {
    const requestId = uuid()
    const payload = {
      userId,
      userName,
      buyer: userId,
      city: form.city,
      street: form.street,
      state: form.state,
      country: form.country,
      zipCode: form.zipCode,
      cardNumber: form.cardNumber,
      cardHolderName: form.cardHolderName,
      cardExpiration: parseExpiration(form.cardExpiration),
      cardSecurityNumber: form.cardSecurityNumber,
      cardTypeId: form.cardTypeId,
      items: form.items.map((it) => ({
        id: uuid(),
        productId: it.productId,
        productName: it.productName,
        unitPrice: it.unitPrice,
        oldUnitPrice: 0,
        quantity: it.quantity,
        pictureUrl: it.pictureUrl,
      })),
    }
    await client.post(API_URL, payload, {
      headers: { 'x-requestid': requestId },
    })
  },

  async cancelOrder(orderNumber: number): Promise<void> {
    await client.put(
      `${API_URL}/cancel`,
      { orderNumber },
      { headers: { 'x-requestid': uuid() } },
    )
  },
}
