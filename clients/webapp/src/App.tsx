/**
 * Converted from: src/WebApp/Components/App.razor
 *
 * Main application component with routing.
 */
import { Routes, Route } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import Header from './components/layout/Header'
import Footer from './components/layout/Footer'
import CatalogPage from './components/catalog/CatalogPage'
import CartPage from './components/cart/CartPage'
import CheckoutPage from './components/checkout/CheckoutPage'
import OrdersPage from './components/orders/OrdersPage'
import { CartProvider } from './context/CartContext'

function App() {
  const auth = useAuth()

  if (auth.isLoading) {
    return <div className="flex items-center justify-center h-screen">Loading...</div>
  }

  return (
    <CartProvider>
      <div className="min-h-screen flex flex-col">
        <Header />
        <main className="flex-1 container mx-auto px-4 py-8">
          <Routes>
            <Route path="/" element={<CatalogPage />} />
            <Route path="/catalog" element={<CatalogPage />} />
            <Route path="/cart" element={<CartPage />} />
            <Route path="/checkout" element={<CheckoutPage />} />
            <Route path="/orders" element={<OrdersPage />} />
            <Route path="/authentication/login-callback" element={<LoginCallback />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </CartProvider>
  )
}

function LoginCallback() {
  const auth = useAuth()

  if (auth.isLoading) {
    return <div>Processing login...</div>
  }

  if (auth.error) {
    return <div>Error: {auth.error.message}</div>
  }

  window.location.href = '/'
  return null
}

export default App
