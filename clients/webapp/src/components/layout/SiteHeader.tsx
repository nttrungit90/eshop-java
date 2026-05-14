/**
 * Site-wide header — full visual port of .NET Blazor HeaderBar.razor.
 *
 * The <header> itself defines the hero height; the image is absolutely
 * positioned behind, full-bleed (no max-width). Content (navbar + page
 * title overlay) sits on top in a relative wrapper with the same height,
 * so layout is independent of the image's intrinsic dimensions.
 *
 * Heights and image source match Blazor:
 *   home (catalog at "/" or "/catalog") → 38rem, header-home.webp
 *   inner page                          → 15rem, header.webp
 */
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useCart } from '../../context/CartContext'
import { usePageHeaderState } from './PageHeaderContext'

export default function SiteHeader() {
  const { pathname } = useLocation()
  const auth = useAuth()
  const { itemCount } = useCart()
  const { title, subtitle } = usePageHeaderState()

  const isHome = pathname === '/' || pathname === '/catalog'
  const heroImg = isHome ? '/images/header-home.webp' : '/images/header.webp'
  const heightClass = isHome ? 'h-[38rem]' : 'h-60'

  return (
    <header className={`relative w-full ${heightClass} overflow-hidden mb-12`}>
      {/* Hero image — full bleed behind everything */}
      <img
        src={heroImg}
        alt=""
        className="absolute inset-0 w-full h-full object-cover object-center select-none"
      />

      {/* Foreground content sits on the image */}
      <div className="relative h-full mx-4 md:mx-12 lg:mx-40 flex flex-col">
        {/* Top navbar */}
        <nav className="flex items-center justify-end gap-6 pt-5 shrink-0">
          <Link to="/" className="mr-auto">
            <img
              src="/images/logo-header.svg"
              alt="AdventureWorks"
              className="h-8 md:h-10 w-auto"
            />
          </Link>

          <Link to="/catalog" className="text-sm font-medium text-black hover:opacity-70 hidden sm:inline">
            Products
          </Link>

          {auth.isAuthenticated && (
            <Link to="/orders" className="text-sm font-medium text-black hover:opacity-70 hidden sm:inline">
              My orders
            </Link>
          )}

          <Link to="/cart" className="relative inline-flex items-center" aria-label="Cart">
            <img src="/icons/cart.svg" alt="" className="h-6 w-6" />
            {itemCount > 0 && (
              <span className="absolute -top-2 -right-2 bg-primary text-white text-xs font-bold rounded-full h-5 min-w-5 px-1 flex items-center justify-center">
                {itemCount}
              </span>
            )}
          </Link>

          {auth.isAuthenticated ? (
            <div className="flex items-center gap-2">
              <img src="/icons/user.svg" alt="" className="h-5 w-5" />
              <span className="text-sm font-medium text-black hidden md:inline">
                {(auth.user?.profile.name as string) ||
                  (auth.user?.profile.preferred_username as string)}
              </span>
              <button
                onClick={() => auth.signoutRedirect()}
                className="text-xs bg-white border border-black text-black px-3 py-1 rounded ml-2 hover:bg-gray-100"
              >
                Log out
              </button>
            </div>
          ) : (
            <button
              onClick={() => auth.signinRedirect()}
              className="text-xs bg-white border border-black text-black px-3 py-1.5 rounded hover:bg-gray-100"
            >
              Log in
            </button>
          )}
        </nav>

        {/* Bottom-left intro overlay: title + subtitle */}
        {(title || subtitle) && (
          <div className="mt-auto mb-12 max-w-3xl">
            {title && (
              <h1 className="text-black text-4xl md:text-5xl lg:text-6xl font-bold leading-none m-0">
                {title}
              </h1>
            )}
            {subtitle && (
              <p className="text-black text-xl md:text-2xl font-bold leading-tight mt-3 m-0">
                {subtitle}
              </p>
            )}
          </div>
        )}
      </div>
    </header>
  )
}
