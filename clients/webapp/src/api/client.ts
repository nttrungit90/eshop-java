/**
 * Shared axios instance for all Java backends.
 *
 * - All requests carry ?api-version=1.0 (catalog enforces this; harmless elsewhere).
 * - Authorization: Bearer <access_token> is injected from the OIDC session — App.tsx
 *   wires this up by calling setAuthToken() whenever the auth state changes.
 * - Goes through nginx proxy at /api/* in production, vite proxy in dev — both
 *   keep the SPA same-origin so no CORS is required.
 */
import axios from 'axios'

let currentToken: string | undefined

export function setAuthToken(token: string | undefined) {
  currentToken = token
}

const client = axios.create({
  baseURL: '',
})

client.interceptors.request.use((config) => {
  config.params = { 'api-version': '1.0', ...(config.params || {}) }
  if (currentToken) {
    config.headers.Authorization = `Bearer ${currentToken}`
  }
  return config
})

export default client
