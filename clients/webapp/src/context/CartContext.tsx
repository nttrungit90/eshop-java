/**
 * Converted from: src/WebApp/Services/BasketState.cs
 *
 * React context for shopping cart state management.
 */
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { useAuth } from 'react-oidc-context'
import { BasketItem, CustomerBasket } from '../types'
import { basketApi } from '../api/basketApi'

interface CartContextType {
  items: BasketItem[]
  addItem: (item: BasketItem) => void
  removeItem: (itemId: string) => void
  updateQuantity: (itemId: string, quantity: number) => void
  clearCart: () => void
  total: number
  itemCount: number
}

const CartContext = createContext<CartContextType | undefined>(undefined)

export function CartProvider({ children }: { children: ReactNode }) {
  const auth = useAuth()
  const [items, setItems] = useState<BasketItem[]>([])

  const buyerId = auth.user?.profile.sub || 'anonymous'

  useEffect(() => {
    loadBasket()
  }, [buyerId])

  async function loadBasket() {
    try {
      const basket = await basketApi.getBasket(buyerId)
      setItems(basket.items || [])
    } catch (error) {
      console.error('Failed to load basket', error)
    }
  }

  async function saveBasket(newItems: BasketItem[]) {
    try {
      const basket: CustomerBasket = { buyerId, items: newItems }
      await basketApi.updateBasket(basket)
    } catch (error) {
      console.error('Failed to save basket', error)
    }
  }

  function addItem(item: BasketItem) {
    const existingItem = items.find(i => i.productId === item.productId)
    let newItems: BasketItem[]

    if (existingItem) {
      newItems = items.map(i =>
        i.productId === item.productId
          ? { ...i, quantity: i.quantity + item.quantity }
          : i
      )
    } else {
      newItems = [...items, { ...item, id: crypto.randomUUID() }]
    }

    setItems(newItems)
    saveBasket(newItems)
  }

  function removeItem(itemId: string) {
    const newItems = items.filter(i => i.id !== itemId)
    setItems(newItems)
    saveBasket(newItems)
  }

  function updateQuantity(itemId: string, quantity: number) {
    if (quantity <= 0) {
      removeItem(itemId)
      return
    }

    const newItems = items.map(i =>
      i.id === itemId ? { ...i, quantity } : i
    )
    setItems(newItems)
    saveBasket(newItems)
  }

  function clearCart() {
    setItems([])
    basketApi.deleteBasket(buyerId)
  }

  const total = items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
  const itemCount = items.reduce((sum, item) => sum + item.quantity, 0)

  return (
    <CartContext.Provider value={{ items, addItem, removeItem, updateQuantity, clearCart, total, itemCount }}>
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const context = useContext(CartContext)
  if (context === undefined) {
    throw new Error('useCart must be used within a CartProvider')
  }
  return context
}
