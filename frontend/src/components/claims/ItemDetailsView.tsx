import { useState, useEffect, type FormEvent, type ChangeEvent } from 'react'
import type { ItemReport } from '../../types/report'
import type { Claim } from '../../types/claim'
import type { AuthUser } from '../../types/auth'
import { submitClaim } from '../../services/claimsApi'
import { uploadClaimEvidence } from '../../services/evidenceApi'
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
  const [identifyingFeatures, setIdentifyingFeatures] = useState<string>('')
  const [photoFile, setPhotoFile] = useState<File | null>(null)
  const [photoPreview, setPhotoPreview] = useState<string | null>(null)

  const [descriptionError, setDescriptionError] = useState<string | null>(null)
  const [identifyingFeaturesError, setIdentifyingFeaturesError] = useState<string | null>(null)
  const [photoError, setPhotoError] = useState<string | null>(null)
  const [serverError, setServerError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false)
  const [submittedClaim, setSubmittedClaim] = useState<Claim | null>(null)

  // Clean up blob preview on unmount
  useEffect(() => {
    return () => {
      if (photoPreview) {
        URL.revokeObjectURL(photoPreview)
      }
    }
  }, [photoPreview])

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

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    if (file.size > 5 * 1024 * 1024) {
      setPhotoError('Photo evidence must not exceed 5 MB')
      return
    }

    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
    if (!validTypes.includes(file.type.toLowerCase())) {
      setPhotoError('Only JPEG, PNG, and WebP images are permitted')
      return
    }

    if (photoPreview) {
      URL.revokeObjectURL(photoPreview)
    }

    setPhotoPreview(URL.createObjectURL(file))
    setPhotoFile(file)
    setPhotoError(null)
  }

  const handleRemovePhoto = () => {
    if (photoPreview) {
      URL.revokeObjectURL(photoPreview)
    }
    setPhotoFile(null)
    setPhotoPreview(null)
  }

  const validate = (): boolean => {
    let isValid = true
    setDescriptionError(null)
    setIdentifyingFeaturesError(null)
    setPhotoError(null)

    const trimmedDesc = description.trim()
    if (!trimmedDesc) {
      setDescriptionError('Ownership verification description is required')
      isValid = false
    } else if (trimmedDesc.length > 1000) {
      setDescriptionError('Description must not exceed 1000 characters')
      isValid = false
    }

    const trimmedFeatures = identifyingFeatures.trim()
    if (!trimmedFeatures) {
      setIdentifyingFeaturesError('Distinguishing identifying features or evidence is required')
      isValid = false
    } else if (trimmedFeatures.length > 1000) {
      setIdentifyingFeaturesError('Identifying features must not exceed 1000 characters')
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
      // 1. Submit claim with ownership description and identifying features
      const result = await submitClaim({
        itemReportId: item.id,
        description: description.trim(),
        identifyingFeatures: identifyingFeatures.trim(),
      })

      // 2. If supporting photo selected, upload to private evidence endpoint
      if (photoFile) {
        try {
          await uploadClaimEvidence(result.id, photoFile)
        } catch (uploadErr) {
          console.error('Claim evidence upload warning:', uploadErr)
          setServerError('Claim submitted, but photo upload encountered an issue: ' + (uploadErr instanceof Error ? uploadErr.message : 'Unknown error'))
        }
      }

      setSubmittedClaim(result)
    } catch (err) {
      if (err instanceof ApiError) {
        setServerError(err.message)
        if (err.fieldErrors) {
          if (err.fieldErrors.description) setDescriptionError(err.fieldErrors.description)
          if (err.fieldErrors.identifyingFeatures) setIdentifyingFeaturesError(err.fieldErrors.identifyingFeatures)
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
            Catalog Reference #{item.id} &middot; Reported on {item.reportDate} &middot; {item.location}
          </p>
        </div>

        <div className="item-details-grid">
          {/* Left Column: Public Item Attributes */}
          <Card className="item-details-card">
            <h2 className="item-details-section-title">Public Item Details</h2>
            <div className="item-attrs-list">
              <div className="item-attr-row">
                <span className="item-attr-label">Classification Category</span>
                <span className="item-attr-value">{item.category}</span>
              </div>
              <div className="item-attr-row">
                <span className="item-attr-label">Discovery / Incident Location</span>
                <span className="item-attr-value">{item.location}</span>
              </div>
              <div className="item-attr-row">
                <span className="item-attr-label">Logging Date</span>
                <span className="item-attr-value">{item.reportDate}</span>
              </div>
              <div className="item-attr-row">
                <span className="item-attr-label">Custody Status</span>
                <span className="item-attr-value">{item.status}</span>
              </div>
              {item.reporterName && (
                <div className="item-attr-row">
                  <span className="item-attr-label">Reported by Student</span>
                  <span className="item-attr-value">{item.reporterName}</span>
                </div>
              )}
            </div>

            <div className="item-public-desc-block">
              <h3 className="item-public-desc-title">Public Description</h3>
              <p className="item-public-desc-text">
                {item.description || 'No public description provided for this catalog item.'}
              </p>
            </div>
          </Card>

          {/* Right Column: Ownership Claim Panel */}
          <Card className="item-claim-panel">
            <h2 className="item-details-section-title">Ownership Verification &amp; Claim</h2>

            {/* Ineligible: Item is LOST (cannot claim a lost notice) */}
            {!isFound && (
              <div className="claim-notice-box claim-notice-box--neutral">
                <h3 className="claim-notice-title">Lost Item Notice</h3>
                <p className="claim-notice-desc">
                  This report was filed by a student who misplaced this item. Claims can only be filed against items turned into the property desk as <strong>FOUND</strong>.
                </p>
                <p className="claim-notice-desc" style={{ marginTop: 'var(--space-2)' }}>
                  If you found this item, please submit a <strong>FOUND</strong> report to assist the owner.
                </p>
              </div>
            )}

            {/* Ineligible: Status is not REPORTED (already CLAIMED or RETURNED) */}
            {isFound && !isReportedStatus && (
              <div className="claim-notice-box claim-notice-box--warning">
                <h3 className="claim-notice-title">Claim Processing Concluded</h3>
                <p className="claim-notice-desc">
                  This item is currently marked as <strong>{item.status}</strong>. New claims cannot be accepted while an approved claim is undergoing return reconciliation.
                </p>
              </div>
            )}

            {/* Eligible: Show Confirmation after successful submission */}
            {submittedClaim && (
              <div className="claim-notice-box claim-notice-box--success">
                <div className="claim-success-icon" aria-hidden="true">&#10003;</div>
                <h3 className="claim-notice-title">Ownership Claim Submitted</h3>
                <p className="claim-notice-desc">
                  Your claim (<strong>Ref #{submittedClaim.id}</strong>) has been logged in <strong>PENDING</strong> status. Administrative staff will review your statements and evidence against the finder's confidential record.
                </p>
                <div className="claim-success-actions">
                  {onViewMyClaims && (
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => onViewMyClaims(submittedClaim.claimantId)}
                    >
                      View in My Claims
                    </Button>
                  )}
                  <Button variant="outline" size="sm" onClick={onBack}>
                    Return to Catalog
                  </Button>
                </div>
              </div>
            )}

            {/* Eligible: When unauthenticated, prompt to sign in or register */}
            {isEligibleForClaim && !submittedClaim && !currentUser && (
              <div className="claim-notice-box claim-notice-box--neutral">
                <h3 className="claim-notice-title">Student Authentication Required</h3>
                <p className="claim-notice-desc">
                  To file a claim for this found item, sign in with your verified campus credentials so property administrators can review your claim securely.
                </p>
                <div style={{ display: 'flex', gap: 'var(--space-3)', marginTop: 'var(--space-3)' }}>
                  {onNavigateLogin && (
                    <Button variant="primary" size="sm" onClick={onNavigateLogin}>
                      Sign In
                    </Button>
                  )}
                  {onNavigateRegister && (
                    <Button variant="outline" size="sm" onClick={onNavigateRegister}>
                      Register as Student
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
                  <p className="identified-student-note" style={{ fontSize: '11px', margin: '4px 0 0' }}>
                    🔒 Your submitted claim statements and visual evidence are strictly confidential and visible only to authorized administrators.
                  </p>
                </div>

                {/* 1. Ownership Statement */}
                <div className="ui-input-group">
                  <label htmlFor="claim-description" className="ui-input-label ui-input-label--required">
                    Ownership Statement *
                  </label>
                  <textarea
                    id="claim-description"
                    className={`ui-textarea-field ${descriptionError ? 'ui-textarea-field--error' : ''}`}
                    placeholder="Describe how and when you misplaced this item, including approximate date, building, room, or circumstance..."
                    rows={3}
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
                      Describe the circumstances of loss (max 1000 characters).
                    </p>
                  ) : (
                    <p id="claim-description-error" className="ui-input-error" role="alert">
                      {descriptionError}
                    </p>
                  )}
                </div>

                {/* 2. Specific Identifying Features & Evidence */}
                <div className="ui-input-group">
                  <label htmlFor="claim-identifying-features" className="ui-input-label ui-input-label--required">
                    Identifying Features &amp; Proof *
                  </label>
                  <textarea
                    id="claim-identifying-features"
                    className={`ui-textarea-field ${identifyingFeaturesError ? 'ui-textarea-field--error' : ''}`}
                    placeholder="Provide distinguishing secrets only the owner knows: serial number, lock screen wallpaper, scratches, inner pocket contents, engravings, purchase receipt details..."
                    rows={4}
                    value={identifyingFeatures}
                    onChange={(e) => {
                      setIdentifyingFeatures(e.target.value)
                      if (identifyingFeaturesError) setIdentifyingFeaturesError(null)
                      if (serverError) setServerError(null)
                    }}
                    disabled={isSubmitting}
                    maxLength={1000}
                    aria-invalid={Boolean(identifyingFeaturesError)}
                    aria-describedby={identifyingFeaturesError ? 'claim-features-error' : 'claim-features-hint'}
                    required
                  />
                  {!identifyingFeaturesError ? (
                    <p id="claim-features-hint" className="ui-input-hint">
                      Specific details matching the item that confirm your ownership to reviewers (max 1000 characters).
                    </p>
                  ) : (
                    <p id="claim-features-error" className="ui-input-error" role="alert">
                      {identifyingFeaturesError}
                    </p>
                  )}
                </div>

                {/* 3. Optional Photo Evidence */}
                <div className="ui-input-group">
                  <label htmlFor="claim-photo" className="ui-input-label">
                    Supporting Ownership Photo (Optional)
                  </label>
                  <input
                    id="claim-photo"
                    type="file"
                    accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
                    className="evidence-file-input"
                    onChange={handleFileChange}
                    disabled={isSubmitting}
                  />
                  <p className="ui-input-hint" style={{ fontSize: 'var(--font-size-xs)' }}>
                    Attach a pre-loss photograph, proof of purchase receipt, or possession photo (Max 5 MB). Visible only to property administrators.
                  </p>
                  {photoError && (
                    <p className="ui-input-error" role="alert">
                      {photoError}
                    </p>
                  )}

                  {photoPreview && (
                    <div className="evidence-preview-box">
                      <img src={photoPreview} alt="Preview of ownership evidence photo" className="evidence-preview-img" />
                      <button
                        type="button"
                        className="evidence-remove-btn"
                        onClick={handleRemovePhoto}
                        title="Remove photo"
                        aria-label="Remove photo"
                      >
                        &times;
                      </button>
                    </div>
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
