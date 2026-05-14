/**
 * Checkout — mirrors .NET Blazor Components/Pages/Checkout/Checkout.razor.
 *
 * Only the shipping address is collected on the form (defaulted from the
 * Keycloak JWT claims address_street/city/state/country/zip_code, exactly
 * as Blazor does). Card details ride along from JWT claims and the
 * expiration is fixed to "1 year from now" — same shortcut the .NET
 * WebApp's BasketState.CheckoutAsync uses.
 *
 * The order summary is on the Cart page; checkout is just shipping +
 * "Place order".
 */
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useCart } from '../../context/CartContext'
import { orderingApi, CheckoutForm } from '../../api/orderingApi'

function readClaim(profile: any | undefined, key: string): string {
  return (profile?.[key] as string) || ''
}

export default function CheckoutPage() {
  const navigate = useNavigate()
  const auth = useAuth()
  const { items, clearCart } = useCart()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // Defaults pulled from Keycloak claims (address_street, address_city, …).
  const [form, setForm] = useState({
    street: readClaim(auth.user?.profile, 'address_street'),
    city: readClaim(auth.user?.profile, 'address_city'),
    state: readClaim(auth.user?.profile, 'address_state'),
    country: readClaim(auth.user?.profile, 'address_country') || 'USA',
    zipCode: readClaim(auth.user?.profile, 'address_zip_code'),
  })

  if (!auth.isAuthenticated) {
    return (
      <div className="text-center py-12">
        <h1 className="text-3xl font-bold mb-4">Please Sign In</h1>
        <p className="text-gray-600 mb-8">You need to be signed in to check out.</p>
        <button onClick={() => auth.signinRedirect()} className="btn-brand">Sign In</button>
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
      const userName = (auth.user?.profile.name as string) || (auth.user?.profile.preferred_username as string) || 'unknown'
      if (!userId) throw new Error('No user id in JWT')

      // Card details from claims, expiration forced to 1y from now (matches .NET WebApp default).
      const oneYearFromNow = new Date()
      oneYearFromNow.setUTCFullYear(oneYearFromNow.getUTCFullYear() + 1)

      const payload: CheckoutForm = {
        street: form.street,
        city: form.city,
        state: form.state,
        country: form.country,
        zipCode: form.zipCode,
        cardNumber: readClaim(auth.user?.profile, 'card_number') || '4012888888881881',
        cardHolderName: readClaim(auth.user?.profile, 'card_holder') || userName,
        cardExpiration: oneYearFromNow.toISOString(),
        cardSecurityNumber: readClaim(auth.user?.profile, 'card_security_number') || '123',
        cardTypeId: 1,
        items: items.map((it) => ({
          productId: it.productId,
          productName: it.productName,
          unitPrice: it.unitPrice,
          discount: 0,
          quantity: it.quantity,
          pictureUrl: it.pictureUrl,
        })),
      }

      await orderingApi.createOrder(payload, userId, userName)
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
    <div>
      <h1 className="text-4xl font-extrabold mb-8">Checkout</h1>

      {error && (
        <div className="bg-red-100 text-red-700 p-4 rounded mb-6">{error}</div>
      )}

      <form onSubmit={handleSubmit}>
        <section className="mb-8">
          <h2 className="text-xl font-semibold pb-2 mb-5 border-b border-gray-300">
            Shipping address
          </h2>

          <label className="block mb-4">
            <span className="block text-sm text-gray-600 mb-1">Address</span>
            <input
              type="text"
              name="street"
              value={form.street}
              onChange={handleChange}
              required
              className="w-full border border-black bg-white px-3 py-2 rounded"
            />
          </label>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-4">
            <label className="block">
              <span className="block text-sm text-gray-600 mb-1">City</span>
              <input
                type="text"
                name="city"
                value={form.city}
                onChange={handleChange}
                required
                className="w-full border border-black bg-white px-3 py-2 rounded"
              />
            </label>
            <label className="block">
              <span className="block text-sm text-gray-600 mb-1">State</span>
              <input
                type="text"
                name="state"
                value={form.state}
                onChange={handleChange}
                required
                className="w-full border border-black bg-white px-3 py-2 rounded"
              />
            </label>
            <label className="block">
              <span className="block text-sm text-gray-600 mb-1">Zip code</span>
              <input
                type="text"
                name="zipCode"
                value={form.zipCode}
                onChange={handleChange}
                required
                className="w-full border border-black bg-white px-3 py-2 rounded"
              />
            </label>
          </div>

          <label className="block">
            <span className="block text-sm text-gray-600 mb-1">Country</span>
            <input
              type="text"
              name="country"
              value={form.country}
              onChange={handleChange}
              required
              className="w-full border border-black bg-white px-3 py-2 rounded"
            />
          </label>
        </section>

        <div className="flex justify-between items-center pt-6 border-t border-black">
          <Link
            to="/cart"
            className="inline-flex items-center gap-2 text-sm text-primary hover:underline"
          >
            <img src="/icons/arrow-left.svg" alt="" className="h-4 w-4" />
            Back to the shopping bag
          </Link>
          <button
            type="submit"
            disabled={loading}
            className="btn-brand"
          >
            {loading ? 'Processing…' : 'Place order'}
          </button>
        </div>
      </form>
    </div>
  )
}
