/**
 * Converted from: src/WebApp/Components/Pages/Orders.razor
 *
 * Orders history page.
 */
import { useState, useEffect } from 'react'
import { useAuth } from 'react-oidc-context'
import { orderingApi } from '../../api/orderingApi'
import { Order } from '../../types'

export default function OrdersPage() {
  const auth = useAuth()
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadOrders()
  }, [auth.user])

  async function loadOrders() {
    if (!auth.isAuthenticated) {
      setLoading(false)
      return
    }

    try {
      const data = await orderingApi.getOrders(auth.user?.access_token)
      setOrders(data)
    } catch (error) {
      console.error('Failed to load orders', error)
    } finally {
      setLoading(false)
    }
  }

  async function handleCancelOrder(orderId: number) {
    try {
      await orderingApi.cancelOrder(orderId, auth.user?.access_token)
      loadOrders()
    } catch (error) {
      console.error('Failed to cancel order', error)
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
      <h1 className="text-3xl font-bold mb-8">My Orders</h1>

      <div className="space-y-6">
        {orders.map(order => (
          <div key={order.orderId} className="bg-white rounded-lg shadow-md p-6">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className="text-lg font-bold">Order #{order.orderId}</h3>
                <p className="text-gray-600 text-sm">
                  {new Date(order.date).toLocaleDateString()}
                </p>
              </div>
              <div className="text-right">
                <span className={`inline-block px-3 py-1 rounded text-sm font-medium ${
                  order.status === 'Paid' ? 'bg-green-100 text-green-800' :
                  order.status === 'Shipped' ? 'bg-blue-100 text-blue-800' :
                  order.status === 'Cancelled' ? 'bg-red-100 text-red-800' :
                  'bg-yellow-100 text-yellow-800'
                }`}>
                  {order.status}
                </span>
              </div>
            </div>

            <div className="border-t pt-4">
              <div className="space-y-2">
                {order.orderItems.map((item, index) => (
                  <div key={index} className="flex justify-between">
                    <span>{item.productName} x {item.units}</span>
                    <span>${(item.unitPrice * item.units).toFixed(2)}</span>
                  </div>
                ))}
              </div>
              <hr className="my-4" />
              <div className="flex justify-between font-bold">
                <span>Total</span>
                <span>${order.total.toFixed(2)}</span>
              </div>
            </div>

            <div className="mt-4 text-sm text-gray-600">
              <p>Ship to: {order.street}, {order.city}, {order.state} {order.zipCode}</p>
            </div>

            {(order.status === 'Submitted' || order.status === 'Awaiting Validation') && (
              <button
                onClick={() => handleCancelOrder(order.orderId)}
                className="mt-4 text-red-500 hover:text-red-700"
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
