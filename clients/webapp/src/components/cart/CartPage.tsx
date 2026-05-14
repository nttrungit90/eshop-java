/**
 * Shopping bag — mirrors .NET Blazor Components/Pages/Cart/CartPage.razor.
 *
 * Layout:
 *   - Title "Shopping bag"
 *   - 3-column rows (Products / Quantity / Total) — image + name + unit price
 *     on the left, an editable quantity with an "Update" submit on the middle,
 *     line total on the right
 *   - Right-side summary card with cart icon + total qty badge, grand total,
 *     "Check out" primary button, "Continue shopping" link back to /catalog
 */
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useCart } from '../../context/CartContext'
import { useDocumentTitle } from '../../hooks/useDocumentTitle'
import { usePageHeader } from '../layout/PageHeaderContext'

export default function CartPage() {
  useDocumentTitle('Shopping Bag | AdventureWorks')
  usePageHeader('Shopping bag')
  const auth = useAuth()
  const { items, updateQuantity, total, itemCount } = useCart()
  const [pending, setPending] = useState<Record<string, number>>({})

  if (!auth.isAuthenticated) {
    return (
      <div className="text-center py-12">
        <h1 className="text-3xl font-bold mb-4">Please Sign In</h1>
        <p className="text-gray-600 mb-8">You need to be signed in to view your shopping bag.</p>
        <button onClick={() => auth.signinRedirect()} className="btn-brand">Sign In</button>
      </div>
    )
  }

  if (items.length === 0) {
    return (
      <div>
        <p className="mt-6">
          Your shopping bag is empty.{' '}
          <Link to="/catalog" className="text-primary underline">Continue shopping.</Link>
        </p>
      </div>
    )
  }

  function setPendingQty(id: string, q: number) {
    setPending({ ...pending, [id]: q })
  }
  function commit(id: string) {
    const q = pending[id]
    if (q === undefined) return
    updateQuantity(id, q)
    const next = { ...pending }
    delete next[id]
    setPending(next)
  }

  return (
    <div>
      <div className="grid grid-cols-1 lg:grid-cols-[1fr_22rem] gap-10">
        {/* Items list */}
        <section>
          <div className="hidden md:grid grid-cols-[2fr_1fr_8rem] gap-4 pb-2 border-b border-gray-300 font-semibold text-sm">
            <div>Products</div>
            <div>Quantity</div>
            <div className="text-right">Total</div>
          </div>

          {items.map((item) => {
            const qty = pending[item.id] ?? item.quantity
            return (
              <div
                key={item.id}
                className="grid grid-cols-1 md:grid-cols-[2fr_1fr_8rem] gap-4 py-4 border-b border-gray-200 items-center"
              >
                {/* Product */}
                <div className="flex items-center gap-4">
                  <img
                    src={item.pictureUrl}
                    alt={item.productName}
                    className="w-20 h-20 object-contain"
                  />
                  <div>
                    <p className="font-semibold leading-tight">{item.productName}</p>
                    <p className="text-gray-600 text-sm mt-1">${item.unitPrice.toFixed(2)}</p>
                  </div>
                </div>

                {/* Quantity + Update */}
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    min={0}
                    value={qty}
                    aria-label={`Quantity for ${item.productName}`}
                    onChange={(e) => setPendingQty(item.id, parseInt(e.target.value, 10) || 0)}
                    className="w-20 border border-black bg-white px-2 py-1 rounded text-center"
                  />
                  <button
                    type="button"
                    onClick={() => commit(item.id)}
                    disabled={pending[item.id] === undefined || pending[item.id] === item.quantity}
                    className="px-3 py-1.5 border border-black rounded text-sm hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    Update
                  </button>
                </div>

                {/* Line total */}
                <div className="text-right font-semibold">
                  ${(item.unitPrice * qty).toFixed(2)}
                </div>
              </div>
            )
          })}
        </section>

        {/* Summary */}
        <aside>
          <div className="bg-white border border-gray-200 rounded-lg p-6 sticky top-4">
            <div className="flex items-center gap-3 pb-4 border-b border-gray-200">
              <img src="/icons/cart.svg" alt="" className="h-6 w-6" />
              <span className="font-semibold flex-1">Your shopping bag</span>
              <span className="bg-primary text-white text-xs font-bold rounded-full h-6 min-w-6 px-2 flex items-center justify-center">
                {itemCount}
              </span>
            </div>

            <div className="flex justify-between items-center py-4 text-lg font-bold">
              <div>Total</div>
              <div>${total.toFixed(2)}</div>
            </div>

            <Link to="/checkout" className="btn-brand w-full block text-center mb-3">
              Check out
            </Link>

            <Link
              to="/catalog"
              className="flex items-center gap-2 text-sm text-primary hover:underline"
            >
              <img src="/icons/arrow-left.svg" alt="" className="h-4 w-4" />
              Continue shopping
            </Link>
          </div>
        </aside>
      </div>
    </div>
  )
}
