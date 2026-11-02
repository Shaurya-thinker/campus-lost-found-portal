/**
 * Claim types and contracts matching the Spring Boot ClaimResponse and ClaimSubmissionRequest DTOs
 */

export type ClaimStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'MORE_INFO_REQUESTED'

export interface Claim {
  id: number
  itemReportId: number
  itemTitle?: string
  claimantId: number
  claimantName?: string
  description: string
  identifyingFeatures?: string
  claimantEvidenceId?: number
  claimDate: string // ISO date "YYYY-MM-DD"
  claimStatus: ClaimStatus
}

export interface ClaimSubmissionPayload {
  itemReportId: number
  claimantId?: number
  description: string
  identifyingFeatures?: string
}
