/**
 * Product detail page — mirrors .NET Blazor Components/Pages/Item/ItemPage.razor.
 *
 * Anonymous user: "Log in to purchase" button → triggers OIDC login.
 * Authenticated user: "Add to shopping bag" button → adds to cart, shows
 *                     "X in shopping bag" link if already present.
 */
import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useCart } from '../../context/CartContext'
import { catalogApi } from '../../api/catalogApi'
import { CatalogItem } from '../../types'

export default function ItemPage() {
  const { itemId } = useParams<{ itemId: string }>()
  const auth = useAuth()
  const { items: cartItems, addItem } = useCart()
  const [item, setItem] = useState<CatalogItem | null>(null)
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!itemId) return
    setLoading(true)
    setNotFound(false)
    catalogApi
      .getItemById(parseInt(itemId, 10))
      .then((it) => setItem(it))
      .catch((err) => {
        if (err?.response?.status === 404) setNotFound(true)
        else console.error('Failed to load item', err)
      })
      .finally(() => setLoading(false))
  }, [itemId])

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-lg">Loading…</div>
      </div>
    )
  }
  if (notFound || !item) {
    return (
      <div className="text-center py-12">
        <h1 className="text-3xl font-bold mb-4">Not found</h1>
        <p className="text-gray-600">Sorry, we couldn't find any such product.</p>
        <Link to="/catalog" className="inline-block mt-6 btn-brand">Back to catalog</Link>
      </div>
    )
  }

  const numInCart = cartItems.find((i) => i.productId === item.id)?.quantity ?? 0
  const isLoggedIn = auth.isAuthenticated

  function handleAdd() {
    if (!isLoggedIn) {
      auth.signinRedirect()
      return
    }
    setBusy(true)
    addItem({
      id: '',
      productId: item!.id,
      productName: item!.name,
      unitPrice: item!.price,
      quantity: 1,
      pictureUrl: catalogApi.pictureUrl(item!),
    })
    setTimeout(() => setBusy(false), 200)
  }

  return (
    <div>
      <div className="mb-6">
        <Link to="/catalog" className="text-primary hover:underline text-sm">
          ← Back to catalog
        </Link>
      </div>

      <div className="grid md:grid-cols-2 gap-10 items-start">
        <img
          src={catalogApi.pictureUrl(item)}
          alt={item.name}
          className="w-full max-h-[28rem] object-contain bg-white rounded-lg shadow-md"
        />

        <div>
          <div className="text-sm uppercase tracking-wide text-gray-500 mb-2">
            {item.catalogBrand?.brand}
          </div>
          <h1 className="text-4xl font-bold mb-3">{item.name}</h1>
          <p className="text-gray-700 leading-relaxed mb-6">{item.description}</p>

          <p className="mb-4 text-sm">
            Brand: <strong>{item.catalogBrand?.brand}</strong>
            {item.catalogType?.type && (
              <>
                <span className="mx-2 text-gray-400">·</span>
                Category: <strong>{item.catalogType.type}</strong>
              </>
            )}
          </p>

          <div className="flex items-center gap-5 mb-3">
            <span className="text-3xl font-extrabold">${item.price.toFixed(2)}</span>

            <button
              type="button"
              onClick={handleAdd}
              disabled={busy || item.availableStock === 0}
              className="btn-brand flex items-center gap-2"
              title={isLoggedIn ? 'Add to shopping bag' : 'Log in to purchase'}
            >
              {isLoggedIn ? (
                <>
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6 2L3 6V20C3 20.5304 3.21071 21.0391 3.58579 21.4142C3.96086 21.7893 4.46957 22 5 22H19C19.5304 22 20.0391 21.7893 20.4142 21.4142C20.7893 21.0391 21 20.5304 21 20V6L18 2H6Z" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                    <path d="M3 6H21" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                    <path d="M16 10C16 11.0609 15.5786 12.0783 14.8284 12.8284C14.0783 13.5786 13.0609 14 12 14C10.9391 14 9.92172 13.5786 9.17157 12.8284C8.42143 12.0783 8 11.0609 8 10" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                  Add to shopping bag
                </>
              ) : (
                <>
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                    <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                  Log in to purchase
                </>
              )}
            </button>
          </div>

          {item.availableStock > 0 && item.availableStock < 10 && (
            <p className="text-orange-600 text-sm mb-2">
              Only {item.availableStock} left in stock!
            </p>
          )}
          {item.availableStock === 0 && (
            <p className="text-red-600 font-medium">Out of stock</p>
          )}

          {numInCart > 0 && (
            <p className="text-sm">
              <strong>{numInCart}</strong> in{' '}
              <Link to="/cart" className="text-primary underline">shopping bag</Link>
            </p>
          )}
        </div>
      </div>
    </div>
  )
}
