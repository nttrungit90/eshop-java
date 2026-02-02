/**
 * Converted from: src/WebApp/Components/Layout/HeaderBar.razor
 *
 * Application header with navigation.
 */
import { Link } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useCart } from '../../context/CartContext'

export default function Header() {
  const auth = useAuth()
  const { itemCount } = useCart()

  return (
    <header className="bg-primary text-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="text-2xl font-bold">
            eShop
          </Link>

          <nav className="flex items-center space-x-6">
            <Link to="/catalog" className="hover:text-gray-200">
              Catalog
            </Link>

            {auth.isAuthenticated && (
              <Link to="/orders" className="hover:text-gray-200">
                My Orders
              </Link>
            )}

            <Link to="/cart" className="relative hover:text-gray-200">
              Cart
              {itemCount > 0 && (
                <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                  {itemCount}
                </span>
              )}
            </Link>

            {auth.isAuthenticated ? (
              <div className="flex items-center space-x-4">
                <span>{auth.user?.profile.name || auth.user?.profile.email}</span>
                <button
                  onClick={() => auth.signoutRedirect()}
                  className="bg-white text-primary px-4 py-2 rounded hover:bg-gray-100"
                >
                  Logout
                </button>
              </div>
            ) : (
              <button
                onClick={() => auth.signinRedirect()}
                className="bg-white text-primary px-4 py-2 rounded hover:bg-gray-100"
              >
                Login
              </button>
            )}
          </nav>
        </div>
      </div>
    </header>
  )
}
