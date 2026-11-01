import type { User, UserRegistrationPayload } from '../types/user'
import type { ApiErrorResponse } from '../types/report'
import { ApiError } from './reportsApi'

/**
 * Registers a new student or campus member via POST /api/users.
 *
 * @param payload Name and Email matching UserRegistrationRequest DTO.
 * @returns Promise resolving to the newly created User entity.
 */
export async function registerUser(payload: UserRegistrationPayload): Promise<User> {
  const response = await fetch('/api/users', {
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

  const data: User = await response.json()
  return data
}

/**
 * Retrieves a user by their unique primary key ID via GET /api/users/{id}.
 */
export async function fetchUserById(id: number): Promise<User> {
  const response = await fetch(`/api/users/${id}`, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new ApiError(`User lookup failed (HTTP ${response.status})`, response.status)
  }

  const data: User = await response.json()
  return data
}

/**
 * Retrieves a user by their registered email address via GET /api/users?email={email}.
 */
export async function fetchUserByEmail(email: string): Promise<User> {
  const response = await fetch(`/api/users?email=${encodeURIComponent(email)}`, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new ApiError(`User lookup failed (HTTP ${response.status})`, response.status)
  }

  const data: User = await response.json()
  return data
}

