import { useState, type FormEvent } from 'react'
import type { ItemReport } from '../../types/report'
import type { Claim } from '../../types/claim'
import type { AuthUser } from '../../types/auth'
import { submitClaim } from '../../services/claimsApi'
import { ApiError } from '../../services/reportsApi'
import { Container, Card, Badge, Button } from '../ui'
import './ItemDetailsView.css'

export interface ItemDetailsViewProps {
  item: ItemReport
  currentUser: AuthUser | null
  onBack: () => void
  onViewMyClaims?: (claimantId?: number) => void
  onNavigateRegister?: () => void
  onNavigateLogin?: () => void
}

export function ItemDetailsView({
  item,
  currentUser,
  onBack,
  onViewMyClaims,
  onNavigateRegister,
  onNavigateLogin,
}: ItemDetailsViewProps) {
  const [description, setDescription] = useState<string>('')
  const [descriptionError, setDescriptionError] = useState<string | null>(null)
  const [serverError, setServerError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false)
  const [submittedClaim, setSubmittedClaim] = useState<Claim | null>(null)

  // Eligibility evaluation strictly following backend business rules:
  // 1. Must be a FOUND item
  // 2. Must have REPORTED status
  const isFound = item.reportType === 'FOUND'
  const isReportedStatus = item.status === 'REPORTED'
  const isEligibleForClaim = isFound && isReportedStatus

  // Self-claim check: user cannot claim an item they reported themselves
  const isSelfReported = Boolean(
    currentUser && item.reporterId && currentUser.id === item.reporterId
  )

  const validate = (): boolean => {
    let isValid = true
    setDescriptionError(null)

    const trimmedDesc = description.trim()
    if (!trimmedDesc) {
      setDescriptionError('Ownership verification description is required')
      isValid = false
    } else if (trimmedDesc.length > 1000) {
      setDescriptionError('Description must not exceed 1000 characters')
      isValid = false
    }

    return isValid
  }

  const handleClaimSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setServerError(null)

    if (!currentUser) {
      setServerError('Authentication is required to submit a claim. Please sign in.')
      return
    }

    if (isSelfReported) {
      setServerError('A user cannot submit a claim for an item they reported themselves.')
      return
    }

    if (!validate()) {
      return
    }

    setIsSubmitting(true)

    try {
      const result = await submitClaim({
        itemReportId: item.id,
        description: description.trim(),
      })
      setSubmittedClaim(result)
    } catch (err) {
      if (err instanceof ApiError) {
        setServerError(err.message)
        if (err.fieldErrors && err.fieldErrors.description) {
          setDescriptionError(err.fieldErrors.description)
        }
      } else {
        setServerError('Network error or server unreachable. Please verify backend connection.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  const typeBadgeVariant = item.reportType === 'FOUND' ? 'info' : 'warning'
  let statusBadgeVariant: 'neutral' | 'info' | 'success' | 'warning' = 'neutral'
  if (item.status === 'CLAIMED') {
    statusBadgeVariant = 'warning'
  } else if (item.status === 'RETURNED') {
    statusBadgeVariant = 'success'
  }

  return (
    <div className="item-details-page">
      <Container size="lg">
        <div className="item-details-header">
          <button
            type="button"
            className="item-details-back-btn"
            onClick={onBack}
            aria-label="Return to catalog browsing"
          >
            &larr; Back to catalog
          </button>
          <div className="item-details-title-row">
            <h1 className="item-details-title">{item.title}</h1>
            <div className="item-details-badges">
              <Badge variant={typeBadgeVariant}>{item.reportType}</Badge>
              <Badge variant={statusBadgeVariant}>{item.status}</Badge>
            </div>
          </div>
          <p className="item-details-meta">
            Reference Record #{item.id} &bull; Cataloged on {item.reportDate}
            {item.reporterName && ` by ${item.reporterName}`}
          </p>
        </div>

        <div className="item-details-layout">
          {/* Main Info Card */}
          <Card className="item-details-card">
            <h2 className="item-section-title">Item Specifications</h2>

            <div className="item-details-grid">
              <div className="item-details-meta-item">
                <span className="item-details-meta-label">Category</span>
                <span className="item-details-meta-val">{item.category}</span>
              </div>
              <div className="item-details-meta-item">
                <span className="item-details-meta-label">Campus Location</span>
                <span className="item-details-meta-val">{item.location}</span>
              </div>
              <div className="item-details-meta-item">
                <span className="item-details-meta-label">Report Type</span>
                <span className="item-details-meta-val">{item.reportType}</span>
              </div>
              <div className="item-details-meta-item">
                <span className="item-details-meta-label">Lifecycle Status</span>
                <span className="item-details-meta-val">{item.status}</span>
              </div>
            </div>

            <div className="item-details-desc-section">
              <h3 className="item-details-desc-label">Public Description</h3>
              <p className="item-details-desc-text">
                {item.description || 'No detailed public description was supplied for this catalog item.'}
              </p>
            </div>
          </Card>

          {/* Action / Claim Panel */}
          <Card className="item-action-card">
            <h2 className="item-section-title">Claim Property</h2>

            {/* Ineligibility Explanations */}
            {!isFound && (
              <div className="claim-notice-box claim-notice-box--lost">
                <h3 className="claim-notice-title">Lost Property Notice</h3>
                <p className="claim-notice-desc">
                  This report is a student inquiry for a missing item. If you discovered property matching this description, please submit a separate <strong>FOUND</strong> report to turn it in.
                </p>
              </div>
            )}

            {isFound && !isReportedStatus && (
              <div className="claim-notice-box claim-notice-box--closed">
                <h3 className="claim-notice-title">Item No Longer Available</h3>
                <p className="claim-notice-desc">
                  This turned-in property has status <strong>{item.status}</strong>. Claims are only accepted while an item is actively awaiting owner verification in <strong>REPORTED</strong> status.
                </p>
              </div>
            )}

            {/* Success State */}
            {submittedClaim && (
              <div className="claim-success-box" role="status">
                <Badge variant="success">Claim Submitted Successfully</Badge>
                <h3 className="claim-success-title">Verification In Progress</h3>
                <p className="claim-success-desc">
                  Your claim has been submitted with status <strong>{submittedClaim.claimStatus}</strong>. Campus administrative staff will review your verification details against the turned-in item.
                </p>
                <div style={{ display: 'flex', gap: 'var(--space-3)', marginTop: 'var(--space-2)', flexWrap: 'wrap' }}>
                  {onViewMyClaims && (
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => onViewMyClaims(submittedClaim.claimantId)}
                    >
                      View My Claims
                    </Button>
                  )}
                  <Button variant="outline" size="sm" onClick={onBack}>
                    Back to catalog
                  </Button>
                </div>
              </div>
            )}

            {/* When not logged in: Prompt sign in or registration */}
            {isEligibleForClaim && !submittedClaim && !currentUser && (
              <div className="claim-notice-box" style={{ textAlign: 'center', padding: 'var(--space-6)' }}>
                <h3 className="claim-notice-title">Authentication Required to Claim</h3>
                <p className="claim-notice-desc" style={{ marginBottom: 'var(--space-4)' }}>
                  You must be authenticated with your campus credentials to file an ownership claim.
                </p>
                <div style={{ display: 'flex', gap: 'var(--space-3)', justifyContent: 'center', flexWrap: 'wrap' }}>
                  {onNavigateLogin && (
                    <Button variant="primary" size="sm" onClick={onNavigateLogin}>
                      Sign In
                    </Button>
                  )}
                  {onNavigateRegister && (
                    <Button variant="outline" size="sm" onClick={onNavigateRegister}>
                      Register as a student
                    </Button>
                  )}
                </div>
              </div>
            )}

            {/* When self-reported: Show rule warning */}
            {isEligibleForClaim && !submittedClaim && currentUser && isSelfReported && (
              <div className="form-alert form-alert--danger" role="alert">
                <strong>Self-Claim Prohibited:</strong>
                <span>You reported this item yourself (Reporter ID #{item.reporterId}). Campus policy prevents submitting a claim for property you turned in.</span>
              </div>
            )}

            {/* Claim Submission Form */}
            {isEligibleForClaim && !submittedClaim && currentUser && !isSelfReported && (
              <form className="claim-form" onSubmit={handleClaimSubmit} noValidate>
                {serverError && (
                  <div className="form-alert form-alert--danger" role="alert">
                    <strong>Claim Error:</strong>
                    <span>{serverError}</span>
                  </div>
                )}

                {/* Identified Claimant Box */}
                <div className="identified-student-box" style={{ marginBottom: 'var(--space-4)' }}>
                  <div className="identified-student-header">
                    <span className="identified-student-badge">Authenticated Claimant</span>
                    <span className="identified-student-id">
                      {currentUser.collegeUid || `ID #${currentUser.id}`}
                    </span>
                  </div>
                  <p className="identified-student-name">
                    {currentUser.name} <span className="identified-student-email">({currentUser.email})</span>
                  </p>
                </div>

                <div className="ui-input-group">
                  <label htmlFor="claim-description" className="ui-input-label ui-input-label--required">
                    Ownership Verification Details
                  </label>
                  <textarea
                    id="claim-description"
                    className={`ui-textarea-field ${descriptionError ? 'ui-textarea-field--error' : ''}`}
                    placeholder="Describe distinguishing marks, serial numbers, passwords, inside pockets, or items stored within that confirm you are the true owner..."
                    rows={4}
                    value={description}
                    onChange={(e) => {
                      setDescription(e.target.value)
                      if (descriptionError) setDescriptionError(null)
                      if (serverError) setServerError(null)
                    }}
                    disabled={isSubmitting}
                    maxLength={1000}
                    aria-invalid={Boolean(descriptionError)}
                    aria-describedby={descriptionError ? 'claim-description-error' : 'claim-description-hint'}
                    required
                  />
                  {!descriptionError ? (
                    <p id="claim-description-hint" className="ui-input-hint">
                      Provide specific identifying proof of ownership not revealed in the public description (max 1000 characters).
                    </p>
                  ) : (
                    <p id="claim-description-error" className="ui-input-error" role="alert">
                      {descriptionError}
                    </p>
                  )}
                </div>

                <div className="claim-form-actions">
                  <Button
                    variant="outline"
                    type="button"
                    onClick={onBack}
                    disabled={isSubmitting}
                  >
                    Cancel
                  </Button>
                  <Button
                    variant="primary"
                    type="submit"
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? 'Submitting claim...' : 'Submit ownership claim'}
                  </Button>
                </div>
              </form>
            )}
          </Card>
        </div>
      </Container>
    </div>
  )
}
