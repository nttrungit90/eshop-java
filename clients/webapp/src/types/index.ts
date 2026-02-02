/**
 * Converted from: src/WebApp types
 *
 * TypeScript interfaces for the application.
 */

export interface CatalogItem {
  id: number
  name: string
  description: string
  price: number
  pictureFileName: string
  pictureUri: string
  catalogType: CatalogType
  catalogBrand: CatalogBrand
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

export interface Order {
  orderId: number
  date: string
  status: string
  description: string
  street: string
  city: string
  state: string
  country: string
  zipCode: string
  orderItems: OrderItem[]
  total: number
}

export interface OrderItem {
  productName: string
  units: number
  unitPrice: number
  pictureUrl: string
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
