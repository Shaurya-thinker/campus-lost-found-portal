import type { ItemReport, ReportType, ItemStatus, CreateReportPayload, ApiErrorResponse } from '../types/report'
import { getAuthHeader, clearAuthSession } from '../utils/auth'

export class ApiError extends Error {
  status: number
  fieldErrors?: Record<string, string>

  constructor(message: string, status: number, fieldErrors?: Record<string, string>) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

/**
 * Fetches item reports from the backend REST API via the Vite /api proxy.
 *
 * @param type Optional filter for LOST or FOUND items.
 * @param status Optional filter for REPORTED, CLAIMED, or RETURNED status.
 * @returns Promise resolving to an array of ItemReport records.
 */
export async function fetchReports(
  type?: ReportType | 'ALL',
  status?: ItemStatus | 'ALL'
): Promise<ItemReport[]> {
  const params = new URLSearchParams()
  if (type && type !== 'ALL') {
    params.set('type', type)
  }
  if (status && status !== 'ALL') {
    params.set('status', status)
  }

  const query = params.toString()
  const url = query ? `/api/reports?${query}` : '/api/reports'

  const response = await fetch(url, {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    throw new ApiError(`Failed to load reports (HTTP ${response.status} ${response.statusText})`, response.status)
  }

  const data: ItemReport[] = await response.json()
  return data
}

/**
 * Retrieves an individual item report by its ID via GET /api/reports/{id}.
 *
 * @param id Report numeric ID.
 * @returns Promise resolving to the ItemReport record.
 */
export async function fetchReportById(id: number): Promise<ItemReport> {
  const response = await fetch(`/api/reports/${id}`, {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    throw new ApiError(`Failed to load report (HTTP ${response.status} ${response.statusText})`, response.status)
  }

  const data: ItemReport = await response.json()
  return data
}

/**
 * Searches reports using backend category and/or location parameters via GET /api/reports/search.
 *
 * @param category Optional category keyword (e.g. "Electronics").
 * @param location Optional location keyword (e.g. "Library").
 * @returns Promise resolving to an array of matching ItemReport records.
 */
export async function searchReports(category?: string, location?: string): Promise<ItemReport[]> {
  const params = new URLSearchParams()
  if (category && category.trim()) {
    params.set('category', category.trim())
  }
  if (location && location.trim()) {
    params.set('location', location.trim())
  }

  const query = params.toString()
  const url = query ? `/api/reports/search?${query}` : '/api/reports/search'

  const response = await fetch(url, {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    throw new ApiError(`Search request failed (HTTP ${response.status} ${response.statusText})`, response.status)
  }

  const data: ItemReport[] = await response.json()
  return data
}

/**
 * Creates a new item report (LOST or FOUND) via POST /api/reports.
 *
 * @param payload Report details matching ItemReportCreateRequest DTO.
 * @returns Promise resolving to the newly created ItemReport.
 */
export async function createReport(payload: CreateReportPayload): Promise<ItemReport> {
  const response = await fetch('/api/reports', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...getAuthHeader(),
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    let message = `Submission failed (HTTP ${response.status})`
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

  const data: ItemReport = await response.json()
  return data
}

/**
 * Marks an item report as RETURNED via PATCH /api/reports/{id}/returned.
 * Note: backend requires the item to be in CLAIMED status.
 *
 * @param id Report numeric ID.
 * @returns Promise resolving to the updated ItemReport.
 */
export async function markReportAsReturned(id: number): Promise<ItemReport> {
  const response = await fetch(`/api/reports/${id}/returned`, {
    method: 'PATCH',
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    let message = `Failed to mark item as returned (HTTP ${response.status})`
    try {
      const errData: ApiErrorResponse = await response.json()
      if (errData.message) {
        message = errData.message
      } else if (errData.error) {
        message = errData.error
      }
    } catch {
      if (response.statusText) {
        message = response.statusText
      }
    }
    throw new ApiError(message, response.status)
  }

  const data: ItemReport = await response.json()
  return data
}
