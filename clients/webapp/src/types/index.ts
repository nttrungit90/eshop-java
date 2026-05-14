/**
 * TypeScript shapes that mirror the Java backend wire format.
 */

export interface CatalogItem {
  id: number
  name: string
  description: string
  price: number
  pictureFileName: string
  pictureUri?: string
  catalogTypeId: number
  catalogType?: CatalogType
  catalogBrandId: number
  catalogBrand?: CatalogBrand
  availableStock: number
}

export interface CatalogType {
  id: number
  type: string
}

export interface CatalogBrand {
  id: number
  brand: string
}

export interface BasketItem {
  id: string
  productId: number
  productName: string
  unitPrice: number
  oldUnitPrice?: number
  quantity: number
  pictureUrl: string
}

export interface CustomerBasket {
  buyerId: string
  items: BasketItem[]
}

/**
 * GET /api/orders returns this summary shape (matches Java OrderSummaryDto).
 * For richer fields (address, items), call GET /api/orders/{orderNumber}.
 */
export interface OrderSummary {
  orderNumber: number
  date: string
  status: string
  total: number
}

/**
 * GET /api/orders/{id} returns this richer shape (matches Java OrderDto).
 */
export interface Order {
  orderId: number
  date: string
  status: string
  description?: string
  street?: string
  city?: string
  state?: string
  country?: string
  zipCode?: string
  orderItems: OrderItem[]
  total: number
}

export interface OrderItem {
  productName: string
  units: number
  unitPrice: number
  pictureUrl: string
}

/** Java catalog page shape: {pageIndex, pageSize, count, data}. */
export interface PaginatedResponse<T> {
  pageIndex: number
  pageSize: number
  count: number
  data: T[]
}

export interface CardType {
  id: number
  name: string
}
