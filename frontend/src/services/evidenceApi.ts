import type {
  EvidenceResponse,
  ClaimVerificationDetails,
  ReportPrivateDetails,
} from '../types/evidence'
import type { Claim } from '../types/claim'
import { ApiError } from './reportsApi'
import { getAuthHeader, clearAuthSession } from '../utils/auth'

/**
 * Uploads private evidence for a FOUND report via POST /api/reports/{reportId}/evidence.
 */
export async function uploadReportEvidence(reportId: number, file: File): Promise<EvidenceResponse> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(`/api/reports/${reportId}/evidence`, {
    method: 'POST',
    headers: {
      ...getAuthHeader(),
    },
    body: formData,
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    let message = `Evidence upload failed (HTTP ${response.status})`
    try {
      const err = await response.json()
      if (err.message) message = err.message
      else if (err.error) message = err.error
    } catch {
      if (response.statusText) message = response.statusText
    }
    throw new ApiError(message, response.status)
  }

  return response.json()
}

/**
 * Uploads private evidence for an ownership claim via POST /api/claims/{claimId}/evidence.
 */
export async function uploadClaimEvidence(claimId: number, file: File): Promise<EvidenceResponse> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(`/api/claims/${claimId}/evidence`, {
    method: 'POST',
    headers: {
      ...getAuthHeader(),
    },
    body: formData,
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    let message = `Claim evidence upload failed (HTTP ${response.status})`
    try {
      const err = await response.json()
      if (err.message) message = err.message
      else if (err.error) message = err.error
    } catch {
      if (response.statusText) message = response.statusText
    }
    throw new ApiError(message, response.status)
  }

  return response.json()
}

/**
 * Fetches an authenticated private evidence image as an ephemeral in-memory Blob URL.
 * Automatically clears memory on revocation.
 * GET /api/evidence/{id}
 */
export async function fetchEvidenceBlobUrl(evidenceId: number): Promise<string> {
  const response = await fetch(`/api/evidence/${evidenceId}`, {
    headers: {
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    let message = `Unable to retrieve evidence photo (HTTP ${response.status})`
    try {
      const err = await response.json()
      if (err.message) message = err.message
    } catch {
      if (response.statusText) message = response.statusText
    }
    throw new ApiError(message, response.status)
  }

  const blob = await response.blob()
  return URL.createObjectURL(blob)
}

/**
 * Fetches dual-party verification details for a claim (Admin only).
 * GET /api/claims/{id}/verification
 */
export async function fetchClaimVerificationDetails(claimId: number): Promise<ClaimVerificationDetails> {
  const response = await fetch(`/api/claims/${claimId}/verification`, {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    let message = `Failed to fetch verification details (HTTP ${response.status})`
    try {
      const err = await response.json()
      if (err.message) message = err.message
    } catch {
      if (response.statusText) message = response.statusText
    }
    throw new ApiError(message, response.status)
  }

  return response.json()
}

/**
 * Transitions a claim to MORE_INFO_REQUESTED status (Admin only).
 * PATCH /api/claims/{id}/request-info
 */
export async function requestMoreInfoForClaim(claimId: number): Promise<Claim> {
  const response = await fetch(`/api/claims/${claimId}/request-info`, {
    method: 'PATCH',
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    let message = `Failed to request more info (HTTP ${response.status})`
    try {
      const err = await response.json()
      if (err.message) message = err.message
    } catch {
      if (response.statusText) message = response.statusText
    }
    throw new ApiError(message, response.status)
  }

  return response.json()
}

/**
 * Fetches confidential private report details (Reporter or Admin only).
 * GET /api/reports/{id}/private-details
 */
export async function fetchReportPrivateDetails(reportId: number): Promise<ReportPrivateDetails> {
  const response = await fetch(`/api/reports/${reportId}/private-details`, {
    headers: {
      Accept: 'application/json',
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession()
    }
    let message = `Failed to load report private details (HTTP ${response.status})`
    try {
      const err = await response.json()
      if (err.message) message = err.message
    } catch {
      if (response.statusText) message = response.statusText
    }
    throw new ApiError(message, response.status)
  }

  return response.json()
}

/**
 * Deletes evidence file.
 * DELETE /api/evidence/{id}
 */
export async function deleteEvidence(evidenceId: number): Promise<void> {
  const response = await fetch(`/api/evidence/${evidenceId}`, {
    method: 'DELETE',
    headers: {
      ...getAuthHeader(),
    },
  })

  if (!response.ok) {
    let message = `Failed to delete evidence (HTTP ${response.status})`
    try {
      const err = await response.json()
      if (err.message) message = err.message
    } catch {
      if (response.statusText) message = response.statusText
    }
    throw new ApiError(message, response.status)
  }
}

