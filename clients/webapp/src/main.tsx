/**
 * Converted from: src/WebApp/Program.cs
 *
 * React application entry point.
 */
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from 'react-oidc-context'
import App from './App'
import './index.css'

const oidcConfig = {
  authority: import.meta.env.VITE_IDENTITY_URL || 'http://localhost:9100',
  client_id: 'webapp',
  redirect_uri: window.location.origin + '/authentication/login-callback',
  post_logout_redirect_uri: window.location.origin + '/signout-callback-oidc',
  scope: 'openid profile orders basket',
  response_type: 'code',
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
