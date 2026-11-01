import type { AuthResponse, AuthUser, LoginPayload, RegisterPayload } from '../types/auth'
import type { ApiErrorResponse } from '../types/report'
import { ApiError } from './reportsApi'
import { getAuthHeader, clearAuthSession } from '../utils/auth'

/**
 * Registers a new student account with College UID, name, email, and password.
 * POST /api/auth/register
 */
export async function register(payload: RegisterPayload): Promise<AuthResponse> {
  const response = await fetch('/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    let message = `Registration failed (HTTP ${response.status})`
    let fieldErrors: Record<string, string> | undefined

    try {
      const errData: ApiErrorResponse = await response.json()
      if (errData.message) {
        message = errData.message
      } else if (errData.error) {
        message = errData.error
      }
      if (errData.fieldErrors) {
        fieldErrors = errData.fieldErrors
      }
    } catch {
      if (response.statusText) {
        message = response.statusText
      }
    }

    throw new ApiError(message, response.status, fieldErrors)
  }

  const data: AuthResponse = await response.json()
  return data
}

/**
 * Authenticates a user by College UID and password.
 * POST /api/auth/login
 */
export async function login(payload: LoginPayload): Promise<AuthResponse> {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    let message = 'Invalid college UID or password.'

    try {
      const errData: ApiErrorResponse = await response.json()
      if (errData.message) {
        message = errData.message
      }
    } catch {
      if (response.statusText) {
        message = response.statusText
      }
    }

    throw new ApiError(message, response.status)
  }

  const data: AuthResponse = await response.json()
  return data
}

/**
 * Retrieves the current authenticated user's profile from the server using the bearer token.
 * GET /api/auth/me
 */
export async function fetchCurrentUser(): Promise<AuthUser> {
  const headers: Record<string, string> = {
    Accept: 'application/json',
    ...getAuthHeader(),
  }

  const response = await fetch('/api/auth/me', { headers })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    throw new ApiError(`Authentication check failed (HTTP ${response.status})`, response.status)
  }

  const data: AuthUser = await response.json()
  return data
}

