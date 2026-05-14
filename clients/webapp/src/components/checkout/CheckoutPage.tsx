/**
 * Converted from: src/WebApp/Components/Pages/Checkout.razor
 *
 * Checkout page for completing orders.
 */
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useCart } from '../../context/CartContext'
import { orderingApi, CheckoutForm } from '../../api/orderingApi'

export default function CheckoutPage() {
  const navigate = useNavigate()
  const auth = useAuth()
  const { items, total, clearCart } = useCart()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // Default future-dated expiration so the @FutureOrPresent validator on the
  // Java side accepts the submission with the demo card number.
  const [form, setForm] = useState({
    street: '15703 NE 61st Ct',
    city: 'Redmond',
    state: 'WA',
    country: 'USA',
    zipCode: '98052',
    cardNumber: '4012888888881881',
    cardHolderName: '',
    cardExpiration: '12/27',
    cardSecurityNumber: '123',
  })

  if (!auth.isAuthenticated) {
    return (
      <div className="text-center py-12">
        <h1 className="text-3xl font-bold mb-4">Please Sign In</h1>
        <p className="text-gray-600 mb-8">You need to be signed in to check out.</p>
        <button onClick={() => auth.signinRedirect()} className="bg-primary text-white px-6 py-3 rounded">Sign In</button>
      </div>
    )
  }

  if (items.length === 0) {
    navigate('/cart')
    return null
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const userId = auth.user?.profile.sub
      const userName = auth.user?.profile.name || auth.user?.profile.preferred_username || 'unknown'
      if (!userId) throw new Error('No user id in JWT')

      const payload: CheckoutForm = {
        ...form,
        cardTypeId: 1,
        items: items.map((item) => ({
          productId: item.productId,
          productName: item.productName,
          unitPrice: item.unitPrice,
          discount: 0,
          quantity: item.quantity,
          pictureUrl: item.pictureUrl,
        })),
      }

      await orderingApi.createOrder(payload, userId, userName as string)
      clearCart()
      navigate('/orders')
    } catch (err: any) {
      const detail = err?.response?.data ? JSON.stringify(err.response.data) : err.message
      setError(`Failed to create order: ${detail}`)
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold mb-8">Checkout</h1>

      {error && (
        <div className="bg-red-100 text-red-700 p-4 rounded mb-6">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-bold mb-4">Shipping Address</h2>
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-sm font-medium mb-1">Street</label>
              <input
                type="text"
                name="street"
                value={form.street}
                onChange={handleChange}
                required
                className="w-full border rounded px-3 py-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">City</label>
              <input
                type="text"
                name="city"
                value={form.city}
                onChange={handleChange}
                required
                className="w-full border rounded px-3 py-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">State</label>
              <input
                type="text"
                name="state"
                value={form.state}
                onChange={handleChange}
                required
                className="w-full border rounded px-3 py-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Country</label>
              <input
                type="text"
                name="country"
                value={form.country}
                onChange={handleChange}
                required
                className="w-full border rounded px-3 py-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Zip Code</label>
              <input
                type="text"
                name="zipCode"
                value={form.zipCode}
                onChange={handleChange}
                required
                className="w-full border rounded px-3 py-2"
              />
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-bold mb-4">Payment Information</h2>
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-sm font-medium mb-1">Card Number</label>
              <input
                type="text"
                name="cardNumber"
                value={form.cardNumber}
                onChange={handleChange}
                required
                className="w-full border rounded px-3 py-2"
              />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium mb-1">Card Holder Name</label>
              <input
                type="text"
                name="cardHolderName"
                value={form.cardHolderName}
                onChange={handleChange}
                required
                className="w-full border rounded px-3 py-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Expiration</label>
              <input
                type="text"
                name="cardExpiration"
                value={form.cardExpiration}
                onChange={handleChange}
                placeholder="MM/YY"
                required
                className="w-full border rounded px-3 py-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">CVV</label>
              <input
                type="text"
                name="cardSecurityNumber"
                value={form.cardSecurityNumber}
                onChange={handleChange}
                required
                className="w-full border rounded px-3 py-2"
              />
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-bold mb-4">Order Summary</h2>
          <div className="space-y-2">
            {items.map(item => (
              <div key={item.id} className="flex justify-between">
                <span>{item.productName} x {item.quantity}</span>
                <span>${(item.unitPrice * item.quantity).toFixed(2)}</span>
              </div>
            ))}
            <hr className="my-4" />
            <div className="flex justify-between font-bold text-lg">
              <span>Total</span>
              <span>${total.toFixed(2)}</span>
            </div>
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-primary text-white py-3 rounded hover:bg-opacity-90 disabled:opacity-50"
        >
          {loading ? 'Processing...' : 'Place Order'}
        </button>
      </form>
    </div>
  )
}
