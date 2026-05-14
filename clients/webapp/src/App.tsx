/**
 * Application shell + routing. Pushes the OIDC access token into the shared
 * axios client whenever it changes so all backend calls are auth'd.
 */
import { useEffect } from 'react'
import { Routes, Route, useNavigate } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import SiteHeader from './components/layout/SiteHeader'
import Footer from './components/layout/Footer'
import CatalogPage from './components/catalog/CatalogPage'
import ItemPage from './components/item/ItemPage'
import CartPage from './components/cart/CartPage'
import CheckoutPage from './components/checkout/CheckoutPage'
import OrdersPage from './components/orders/OrdersPage'
import FlowVerifier from './pages/dev/FlowVerifier'
import { CartProvider } from './context/CartContext'
import { PageHeaderProvider } from './components/layout/PageHeaderContext'
import { setAuthToken } from './api/client'

function App() {
  const auth = useAuth()

  useEffect(() => {
    setAuthToken(auth.user?.access_token)
  }, [auth.user?.access_token])

  if (auth.isLoading) {
    return <div className="flex items-center justify-center h-screen">Loading…</div>
  }

  return (
    <PageHeaderProvider>
      <CartProvider>
        <div className="min-h-screen flex flex-col">
          <SiteHeader />
          <main className="flex-1 mx-4 md:mx-12 lg:mx-40 mb-12">
            <Routes>
              <Route path="/" element={<CatalogPage />} />
              <Route path="/catalog" element={<CatalogPage />} />
              <Route path="/item/:itemId" element={<ItemPage />} />
              <Route path="/cart" element={<CartPage />} />
              <Route path="/checkout" element={<CheckoutPage />} />
              <Route path="/orders" element={<OrdersPage />} />
              <Route path="/dev/flow" element={<FlowVerifier />} />
              <Route path="/authentication/login-callback" element={<LoginCallback />} />
            </Routes>
          </main>
          <Footer />
        </div>
      </CartProvider>
    </PageHeaderProvider>
  )
}

/**
 * OIDC return URL. AuthProvider does the code → token exchange under the hood;
 * once `isLoading` flips false and we have a user, navigate back to the home page
 * via react-router (no full reload) so the OIDC session state stays hydrated.
 */
function LoginCallback() {
  const auth = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (!auth.isLoading && !auth.error && auth.isAuthenticated) {
      navigate('/', { replace: true })
    }
  }, [auth.isLoading, auth.error, auth.isAuthenticated, navigate])

  if (auth.error) return <div className="p-8">Login error: {auth.error.message}</div>
  return <div className="p-8">Processing login…</div>
}

export default App
