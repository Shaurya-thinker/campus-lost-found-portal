export type EvidenceType = 'REPORTER_PRIVATE' | 'CLAIMANT_PRIVATE'

export interface EvidenceResponse {
  id: number
  evidenceType: EvidenceType
  itemReportId?: number | null
  claimId?: number | null
  uploadedById?: number
  originalFilename?: string | null
  contentType: string
  fileSize: number
  createdAt: string
}

export interface ClaimVerificationDetails {
  claimId: number
  claimStatus: 'PENDING' | 'APPROVED' | 'REJECTED' | 'MORE_INFO_REQUESTED'
  claimDate: string

  // Public item details
  itemReportId: number
  itemTitle: string
  itemCategory: string
  itemLocation: string
  itemPublicDescription: string
  reportDate: string
  itemStatus: string

  // Finder / Reporter Private Evidence
  reporterId?: number
  reporterName?: string
  reporterPrivateDescription?: string | null
  reporterEvidenceId?: number | null

  // Claimant Evidence
  claimantId: number
  claimantName?: string
  claimantDescription: string
  claimantIdentifyingFeatures?: string | null
  claimantEvidenceId?: number | null
}

export interface ReportPrivateDetails {
  reportId: number
  privateDescription?: string | null
  evidenceId?: number | null
  originalFilename?: string | null
  fileSize?: number | null
  contentType?: string | null
}

