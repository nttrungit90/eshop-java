/**
 * React entry point.
 *
 * The OIDC user store is explicitly set to localStorage so the session is shared
 * across tabs — without this override the user appears logged out if they open a
 * second tab (sessionStorage is per-tab). Practical impact: a sign-in in one tab
 * is immediately visible in /dev/flow opened in another tab.
 */
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from 'react-oidc-context'
import { WebStorageStateStore } from 'oidc-client-ts'
import App from './App'
import './index.css'

const oidcConfig = {
  authority: import.meta.env.VITE_IDENTITY_URL || 'http://localhost:8180/realms/eshop',
  client_id: 'webapp-spa',
  redirect_uri: window.location.origin + '/authentication/login-callback',
  post_logout_redirect_uri: window.location.origin + '/signout-callback-oidc',
  scope: 'openid profile orders basket',
  response_type: 'code',
  userStore: new WebStorageStateStore({ store: window.localStorage }),
  stateStore: new WebStorageStateStore({ store: window.localStorage }),
  automaticSilentRenew: true,
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider {...oidcConfig}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </AuthProvider>
  </React.StrictMode>,
)
