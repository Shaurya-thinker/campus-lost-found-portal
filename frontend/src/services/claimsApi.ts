import type { Claim, ClaimStatus, ClaimSubmissionPayload } from '../types/claim'
import { ApiError } from './reportsApi'
import { getAuthHeader, clearAuthSession } from '../utils/auth'

/**
 * Submits a new ownership claim for an eligible found item report.
 * POST /api/claims
 */
export async function submitClaim(payload: ClaimSubmissionPayload): Promise<Claim> {
  const response = await fetch('/api/claims', {
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
    let message = `Claim submission failed (HTTP ${response.status})`
    let fieldErrors: Record<string, string> | undefined

    try {
      const errData = await response.json()
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

  const data: Claim = await response.json()
  return data
}

/**
 * Retrieves all claims filed by a specific student/claimant.
 * GET /api/claims?claimantId={claimantId}
 */
export async function fetchClaimsByClaimant(claimantId: number): Promise<Claim[]> {
  const response = await fetch(`/api/claims?claimantId=${claimantId}`, {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    let message = `Failed to fetch claims (HTTP ${response.status})`
    try {
      const errData = await response.json()
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

  const data: Claim[] = await response.json()
  return data
}

/**
 * Retrieves all claims filtered by status (e.g. 'PENDING', 'APPROVED', 'REJECTED').
 * GET /api/claims?status={status}
 */
export async function fetchClaimsByStatus(status: ClaimStatus): Promise<Claim[]> {
  const response = await fetch(`/api/claims?status=${status}`, {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    let message = `Failed to fetch claims with status ${status} (HTTP ${response.status})`
    try {
      const errData = await response.json()
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

  const data: Claim[] = await response.json()
  return data
}

/**
 * Approves a pending claim and marks its item report as CLAIMED.
 * PATCH /api/claims/{id}/approve
 */
export async function approveClaim(id: number): Promise<Claim> {
  const response = await fetch(`/api/claims/${id}/approve`, {
    method: 'PATCH',
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    let message = `Failed to approve claim (HTTP ${response.status})`
    try {
      const errData = await response.json()
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

  const data: Claim = await response.json()
  return data
}

/**
 * Rejects a pending claim.
 * PATCH /api/claims/{id}/reject
 */
export async function rejectClaim(id: number): Promise<Claim> {
  const response = await fetch(`/api/claims/${id}/reject`, {
    method: 'PATCH',
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    let message = `Failed to reject claim (HTTP ${response.status})`
    try {
      const errData = await response.json()
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

  const data: Claim = await response.json()
  return data
}

/**
 * Retrieves all claims filed for a specific item report.
 * GET /api/claims?reportId={reportId}
 */
export async function fetchClaimsByReport(reportId: number): Promise<Claim[]> {
  const response = await fetch(`/api/claims?reportId=${reportId}`, {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    let message = `Failed to fetch claims for report (HTTP ${response.status})`
    try {
      const errData = await response.json()
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

  const data: Claim[] = await response.json()
  return data
}

