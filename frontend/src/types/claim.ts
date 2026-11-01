/**
 * Claim types and contracts matching the Spring Boot ClaimResponse and ClaimSubmissionRequest DTOs
 */

export type ClaimStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface Claim {
  id: number
  itemReportId: number
  itemTitle?: string
  claimantId: number
  claimantName?: string
  description: string
  claimDate: string // ISO date "YYYY-MM-DD"
  claimStatus: ClaimStatus
}

export interface ClaimSubmissionPayload {
  itemReportId: number
  claimantId?: number
  description: string
}

