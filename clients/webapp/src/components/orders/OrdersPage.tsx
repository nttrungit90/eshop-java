/**
 * Orders history. Uses the OrderSummary shape returned by GET /api/orders
 * (orderNumber, date, status, total) — matches the Java OrderSummaryDto and
 * mirrors what the .NET WebApp's Razor page showed.
 */
import { useState, useEffect } from 'react'
import { useAuth } from 'react-oidc-context'
import { orderingApi } from '../../api/orderingApi'
import { OrderSummary } from '../../types'
import { useDocumentTitle } from '../../hooks/useDocumentTitle'

const STATUS_STYLE: Record<string, string> = {
  Submitted: 'bg-yellow-100 text-yellow-800',
  AwaitingValidation: 'bg-yellow-100 text-yellow-800',
  StockConfirmed: 'bg-blue-100 text-blue-800',
  Paid: 'bg-green-100 text-green-800',
  Shipped: 'bg-blue-100 text-blue-800',
  Cancelled: 'bg-red-100 text-red-800',
}

export default function OrdersPage() {
  useDocumentTitle('My Orders | AdventureWorks')
  const auth = useAuth()
  const [orders, setOrders] = useState<OrderSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!auth.isAuthenticated) {
      setLoading(false)
      return
    }
    loadOrders()
  }, [auth.isAuthenticated, auth.user?.access_token])

  async function loadOrders() {
    setLoading(true)
    setError('')
    try {
      const data = await orderingApi.getOrders()
      setOrders(data)
    } catch (err) {
      console.error('Failed to load orders', err)
      setError('Failed to load orders.')
    } finally {
      setLoading(false)
    }
  }

  async function handleCancelOrder(orderNumber: number) {
    try {
      await orderingApi.cancelOrder(orderNumber)
      loadOrders()
    } catch (err) {
      console.error('Failed to cancel order', err)
    }
  }

  if (!auth.isAuthenticated) {
    return (
      <div className="text-center py-12">
        <h1 className="text-3xl font-bold mb-4">Please Sign In</h1>
        <p className="text-gray-600 mb-8">You need to be signed in to view your orders.</p>
        <button
          onClick={() => auth.signinRedirect()}
          className="bg-primary text-white px-6 py-3 rounded hover:bg-opacity-90"
        >
          Sign In
        </button>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-lg">Loading orders...</div>
      </div>
    )
  }

  if (orders.length === 0) {
    return (
      <div className="text-center py-12">
        <h1 className="text-3xl font-bold mb-4">No Orders Yet</h1>
        <p className="text-gray-600">You haven't placed any orders.</p>
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-3xl font-bold">My Orders</h1>
        <button
          onClick={loadOrders}
          className="text-sm bg-gray-200 hover:bg-gray-300 px-3 py-2 rounded"
        >
          Refresh
        </button>
      </div>

      {error && (
        <div className="bg-red-100 text-red-700 p-4 rounded mb-6">{error}</div>
      )}

      <div className="space-y-4">
        {orders.map((order) => (
          <div key={order.orderNumber} className="bg-white rounded-lg shadow-md p-6">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-lg font-bold">Order #{order.orderNumber}</h3>
                <p className="text-gray-600 text-sm">
                  {new Date(order.date).toLocaleString()}
                </p>
              </div>
              <div className="text-right space-y-2">
                <div>
                  <span className={`inline-block px-3 py-1 rounded text-sm font-medium ${STATUS_STYLE[order.status] || 'bg-gray-100 text-gray-800'}`}>
                    {order.status}
                  </span>
                </div>
                <div className="text-xl font-bold">${order.total.toFixed(2)}</div>
              </div>
            </div>
            {(order.status === 'Submitted' || order.status === 'AwaitingValidation' || order.status === 'StockConfirmed') && (
              <button
                onClick={() => handleCancelOrder(order.orderNumber)}
                className="mt-4 text-red-500 hover:text-red-700 text-sm"
              >
                Cancel Order
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
