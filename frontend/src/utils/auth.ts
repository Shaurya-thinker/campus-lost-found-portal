import type { AuthUser } from '../types/auth'

/**
 * Security Architecture Notice:
 * -----------------------------
 * In this client-side single page application, authentication tokens are stored in `sessionStorage`.
 *
 * XSS Risk Disclosure:
 * Any token stored in Web Storage (`localStorage` or `sessionStorage`) is accessible to client-side
 * JavaScript within the same origin, meaning an XSS vulnerability could allow an attacker to extract the token.
 * `sessionStorage` is deliberately chosen over `localStorage` because it automatically purges tokens when
 * the browser tab is closed and does not persist across separate tabs or browser restarts, minimizing token lifetime exposure.
 *
 * In a hardened production environment, authentication would preferably be established using
 * encrypted, HttpOnly, SameSite=Strict session cookies alongside CSRF tokens to completely insulate
 * credentials from JavaScript execution.
 */

const TOKEN_KEY = 'campus_auth_token'
const USER_KEY = 'campus_auth_user'

/**
 * Retrieves the stored JWT bearer token, or null if unauthenticated.
 */
export function getAuthToken(): string | null {
  try {
    return window.sessionStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

/**
 * Retrieves the authenticated user profile, or null if unauthenticated.
 */
export function getAuthUser(): AuthUser | null {
  try {
    const raw = window.sessionStorage.getItem(USER_KEY)
    if (!raw) return null
    return JSON.parse(raw) as AuthUser
  } catch {
    clearAuthSession()
    return null
  }
}

/**
 * Stores the authenticated session token and user profile into sessionStorage.
 */
export function setAuthSession(token: string, user: AuthUser): void {
  try {
    window.sessionStorage.setItem(TOKEN_KEY, token)
    window.sessionStorage.setItem(USER_KEY, JSON.stringify(user))
  } catch (err) {
    console.warn('Failed to save authentication session:', err)
  }
}

/**
 * Clears the authenticated session upon logout or 401 Unauthorized responses.
 */
export function clearAuthSession(): void {
  try {
    window.sessionStorage.removeItem(TOKEN_KEY)
    window.sessionStorage.removeItem(USER_KEY)
  } catch (err) {
    console.warn('Failed to clear authentication session:', err)
  }
}

/**
 * Returns HTTP Authorization headers containing the Bearer token if present.
 */
export function getAuthHeader(): Record<string, string> {
  const token = getAuthToken()
  if (token) {
    return { Authorization: `Bearer ${token}` }
  }
  return {}
}

