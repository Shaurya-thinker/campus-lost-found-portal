import type {
  LocationCountReport,
  CategoryCountReport,
  UnclaimedItemDetail,
} from '../types/analytics'
import { ApiError } from './reportsApi'
import { getAuthHeader, clearAuthSession } from '../utils/auth'

/**
 * Retrieves unclaimed found items grouped by campus location.
 * GET /api/analytics/unclaimed-items/by-location
 */
export async function fetchUnclaimedByLocation(): Promise<LocationCountReport[]> {
  const response = await fetch('/api/analytics/unclaimed-items/by-location', {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    throw new ApiError(
      `Failed to load location analytics (HTTP ${response.status})`,
      response.status
    )
  }

  return response.json()
}

/**
 * Retrieves unclaimed found items grouped by category.
 * GET /api/analytics/unclaimed-items/by-category
 */
export async function fetchUnclaimedByCategory(): Promise<CategoryCountReport[]> {
  const response = await fetch('/api/analytics/unclaimed-items/by-category', {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    throw new ApiError(
      `Failed to load category analytics (HTTP ${response.status})`,
      response.status
    )
  }

  return response.json()
}

/**
 * Retrieves detailed row-level attributes for unclaimed found items.
 * GET /api/analytics/unclaimed-items/details
 */
export async function fetchUnclaimedDetails(): Promise<UnclaimedItemDetail[]> {
  const response = await fetch('/api/analytics/unclaimed-items/details', {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    throw new ApiError(
      `Failed to load unclaimed item details (HTTP ${response.status})`,
      response.status
    )
  }

  return response.json()
}

