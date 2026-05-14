/**
 * Site header. Visuals mirror the .NET Blazor HeaderBar — AdventureWorks logo
 * on the dark-purple bar, Catalog / My Orders nav, cart icon with badge,
 * Login / username menu on the right.
 */
import { Link } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useCart } from '../../context/CartContext'

export default function Header() {
  const auth = useAuth()
  const { itemCount } = useCart()

  return (
    <header className="bg-primary text-white">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center">
            <img src="/images/logo-header.svg" alt="AdventureWorks" className="h-8" />
          </Link>

          <nav className="flex items-center space-x-6 text-sm font-medium">
            <Link to="/catalog" className="hover:opacity-80">
              Products
            </Link>

            {auth.isAuthenticated && (
              <Link to="/orders" className="hover:opacity-80">
                My orders
              </Link>
            )}

            <Link to="/cart" className="relative hover:opacity-80 flex items-center" aria-label="Cart">
              <img src="/icons/cart.svg" alt="" className="h-6 w-6 invert" />
              {itemCount > 0 && (
                <span className="absolute -top-2 -right-2 bg-accent text-primary text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                  {itemCount}
                </span>
              )}
            </Link>

            {auth.isAuthenticated ? (
              <div className="flex items-center space-x-3">
                <div className="flex items-center space-x-2">
                  <img src="/icons/user.svg" alt="" className="h-5 w-5 invert" />
                  <span className="text-sm">
                    {(auth.user?.profile.name as string) || (auth.user?.profile.preferred_username as string)}
                  </span>
                </div>
                <button
                  onClick={() => auth.signoutRedirect()}
                  className="text-sm bg-white text-primary px-3 py-1.5 rounded hover:bg-gray-100"
                >
                  Log out
                </button>
              </div>
            ) : (
              <button
                onClick={() => auth.signinRedirect()}
                className="text-sm bg-white text-primary px-3 py-1.5 rounded hover:bg-gray-100"
              >
                Log in
              </button>
            )}
          </nav>
        </div>
      </div>
    </header>
  )
}
