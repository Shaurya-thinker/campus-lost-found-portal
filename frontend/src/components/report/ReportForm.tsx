import { useState, useEffect, type FormEvent, type ChangeEvent } from 'react'
import type { ReportType, ItemReport } from '../../types/report'
import type { AuthUser } from '../../types/auth'
import { createReport, ApiError } from '../../services/reportsApi'
import { uploadReportEvidence } from '../../services/evidenceApi'
import { Container, Card, Button, Input, Badge } from '../ui'
import './ReportForm.css'

export interface ReportFormProps {
  initialType?: ReportType
  currentUser: AuthUser | null
  onCancel: () => void
  onSuccess: (newReport: ItemReport) => void
  onNavigateRegister?: () => void
  onNavigateLogin?: () => void
}

interface FormFields {
  reportType: ReportType
  title: string
  category: string
  location: string
  description: string
  privateDescription: string
  photoFile: File | null
  photoPreview: string | null
}

interface FormErrors {
  reportType?: string
  title?: string
  category?: string
  location?: string
  description?: string
  privateDescription?: string
  photo?: string
}

export function ReportForm({
  initialType = 'LOST',
  currentUser,
  onCancel,
  onSuccess,
  onNavigateRegister,
  onNavigateLogin,
}: ReportFormProps) {
  const [fields, setFields] = useState<FormFields>({
    reportType: initialType,
    title: '',
    category: '',
    location: '',
    description: '',
    privateDescription: '',
    photoFile: null,
    photoPreview: null,
  })

  const [errors, setErrors] = useState<FormErrors>({})
  const [serverError, setServerError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false)
  const [submittedReport, setSubmittedReport] = useState<ItemReport | null>(null)

  // Synchronize report type if initialType changes from parent/hash navigation
  useEffect(() => {
    setFields((prev) => ({ ...prev, reportType: initialType }))
  }, [initialType])

  // Clean up object URL when component unmounts or preview changes
  useEffect(() => {
    return () => {
      if (fields.photoPreview) {
        URL.revokeObjectURL(fields.photoPreview)
      }
    }
  }, [fields.photoPreview])

  const handleFieldChange = (field: keyof FormFields, value: string) => {
    setFields((prev) => ({ ...prev, [field]: value }))
    if (errors[field as keyof FormErrors]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
    if (serverError) {
      setServerError(null)
    }
  }

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    // Validate size (5 MB limit)
    if (file.size > 5 * 1024 * 1024) {
      setErrors((prev) => ({ ...prev, photo: 'File size must not exceed 5 MB' }))
      return
    }

    // Validate type
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
    if (!validTypes.includes(file.type.toLowerCase())) {
      setErrors((prev) => ({ ...prev, photo: 'Only JPEG, PNG, and WebP images are permitted' }))
      return
    }

    // Revoke previous preview if existed
    if (fields.photoPreview) {
      URL.revokeObjectURL(fields.photoPreview)
    }

    const previewUrl = URL.createObjectURL(file)
    setFields((prev) => ({
      ...prev,
      photoFile: file,
      photoPreview: previewUrl,
    }))
    setErrors((prev) => ({ ...prev, photo: undefined }))
  }

  const handleRemovePhoto = () => {
    if (fields.photoPreview) {
      URL.revokeObjectURL(fields.photoPreview)
    }
    setFields((prev) => ({
      ...prev,
      photoFile: null,
      photoPreview: null,
    }))
  }

  const validate = (): boolean => {
    const newErrors: FormErrors = {}

    const titleTrimmed = fields.title.trim()
    if (!titleTrimmed) {
      newErrors.title = 'Title is required'
    } else if (titleTrimmed.length > 150) {
      newErrors.title = 'Title must not exceed 150 characters'
    }

    const catTrimmed = fields.category.trim()
    if (!catTrimmed) {
      newErrors.category = 'Category is required'
    } else if (catTrimmed.length > 50) {
      newErrors.category = 'Category must not exceed 50 characters'
    }

    const locTrimmed = fields.location.trim()
    if (!locTrimmed) {
      newErrors.location = 'Campus location is required'
    } else if (locTrimmed.length > 100) {
      newErrors.location = 'Location must not exceed 100 characters'
    }

    const descTrimmed = fields.description.trim()
    if (!descTrimmed) {
      newErrors.description = 'Public description is required'
    } else if (descTrimmed.length > 1000) {
      newErrors.description = 'Description must not exceed 1000 characters'
    }

    // Milestone 13: FOUND reports strictly require private distinguishing details
    if (fields.reportType === 'FOUND') {
      const privateTrimmed = fields.privateDescription.trim()
      if (!privateTrimmed) {
        newErrors.privateDescription =
          'Private distinguishing details are required for FOUND items to enable secure administrator ownership verification.'
      } else if (privateTrimmed.length > 1000) {
        newErrors.privateDescription = 'Private description must not exceed 1000 characters'
      }
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setServerError(null)

    if (!currentUser) {
      setServerError('Authentication is required to file a report. Please sign in or register.')
      return
    }

    if (!validate()) {
      return
    }

    setIsSubmitting(true)

    try {
      // 1. Create the report
      const created = await createReport({
        title: fields.title.trim(),
        category: fields.category.trim(),
        location: fields.location.trim(),
        description: fields.description.trim(),
        privateDescription: fields.reportType === 'FOUND' ? fields.privateDescription.trim() : undefined,
        reportType: fields.reportType,
      })

      // 2. If a private photo was selected for a FOUND report, upload it to the secure evidence endpoint
      if (fields.reportType === 'FOUND' && fields.photoFile) {
        try {
          await uploadReportEvidence(created.id, fields.photoFile)
        } catch (uploadErr) {
          console.error('Evidence upload warning:', uploadErr)
          // Report was created, alert user if evidence upload had a secondary issue
          setServerError('Report was logged, but private photo upload failed: ' + (uploadErr instanceof Error ? uploadErr.message : 'Unknown error'))
        }
      }

      setSubmittedReport(created)
      onSuccess(created)
    } catch (err) {
      if (err instanceof ApiError) {
        setServerError(err.message)
        if (err.fieldErrors) {
          setErrors((prev) => ({ ...prev, ...err.fieldErrors }))
        }
      } else {
        setServerError('Network error or server unreachable. Please check backend connection.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleReset = () => {
    if (fields.photoPreview) {
      URL.revokeObjectURL(fields.photoPreview)
    }
    setFields({
      reportType: initialType,
      title: '',
      category: '',
      location: '',
      description: '',
      privateDescription: '',
      photoFile: null,
      photoPreview: null,
    })
    setErrors({})
    setServerError(null)
    setSubmittedReport(null)
  }

  // When no authenticated user is in session
  if (!currentUser) {
    return (
      <div className="report-form-page">
        <Container size="md">
          <Card className="report-gate-card">
            <h2 className="report-gate-title">Authentication Required</h2>
            <p className="report-gate-desc">
              To ensure campus property accountability, all lost and found reports must be filed under a verified student account.
            </p>
            <div className="report-gate-actions">
              {onNavigateLogin && (
                <Button variant="primary" onClick={onNavigateLogin}>
                  Sign in
                </Button>
              )}
              {onNavigateRegister && (
                <Button variant="outline" onClick={onNavigateRegister}>
                  Register account
                </Button>
              )}
            </div>
            <button
              type="button"
              className="report-gate-cancel"
              onClick={onCancel}
            >
              &larr; Return to browse catalog
            </button>
          </Card>
        </Container>
      </div>
    )
  }

  // Confirmation view after report is successfully submitted
  if (submittedReport) {
    return (
      <div className="report-form-page">
        <Container size="md">
          <Card className="report-success-card">
            <div className="report-success-icon" aria-hidden="true">&#10003;</div>
            <h2 className="report-success-title">Report Filed Successfully</h2>
            <p className="report-success-desc">
              Your {submittedReport.reportType.toLowerCase()} item report has been logged in the university property system.
              {submittedReport.reportType === 'FOUND' && (
                <span> Private distinguishing evidence was securely recorded for administrative ownership verification.</span>
              )}
            </p>

            <div className="report-success-details">
              <div className="report-success-row">
                <span style={{ color: 'var(--color-text-muted)' }}>Report Reference:</span>
                <span style={{ fontWeight: 'var(--font-weight-semibold)' }}>#{submittedReport.id}</span>
              </div>
              <div className="report-success-row">
                <span style={{ color: 'var(--color-text-muted)' }}>Item Title:</span>
                <span>{submittedReport.title}</span>
              </div>
              <div className="report-success-row">
                <span style={{ color: 'var(--color-text-muted)' }}>Report Type:</span>
                <Badge variant={submittedReport.reportType === 'FOUND' ? 'info' : 'warning'}>
                  {submittedReport.reportType}
                </Badge>
              </div>
              <div className="report-success-row">
                <span style={{ color: 'var(--color-text-muted)' }}>Campus Location:</span>
                <span>{submittedReport.location}</span>
              </div>
              <div className="report-success-row">
                <span style={{ color: 'var(--color-text-muted)' }}>Current Status:</span>
                <Badge variant="neutral">{submittedReport.status}</Badge>
              </div>
            </div>

            <div className="report-success-actions">
              <Button
                variant="primary"
                onClick={() => {
                  handleReset()
                  onCancel()
                }}
              >
                Return to catalog
              </Button>
              <Button variant="outline" onClick={handleReset}>
                Submit another report
              </Button>
            </div>
          </Card>
        </Container>
      </div>
    )
  }

  return (
    <div className="report-form-page">
      <Container size="lg">
        <div className="report-form-header">
          <button
            type="button"
            className="report-back-btn"
            onClick={onCancel}
            aria-label="Return to catalog browsing"
          >
            &larr; Back to catalog
          </button>
          <h1 className="report-form-title">File an Item Report</h1>
          <p className="report-form-desc">
            Provide details about an item lost or found within university grounds. Accurate descriptions and locations assist administrative staff in verifying property claims.
          </p>
        </div>

        <Card className="report-form-card">
          <form className="report-form" onSubmit={handleSubmit} noValidate>
            {serverError && (
              <div className="form-alert form-alert--danger" role="alert">
                <strong>Submission Error:</strong>
                <span>{serverError}</span>
              </div>
            )}

            {/* Current Session Student Identity Banner */}
            <div className="identified-student-box">
              <div className="identified-student-header">
                <span className="identified-student-badge">Reporting Student Identity</span>
                <span className="identified-student-id">
                  {currentUser.collegeUid ? `UID: ${currentUser.collegeUid}` : `User #${currentUser.id}`}
                </span>
              </div>
              <p className="identified-student-name">
                {currentUser.name} <span className="identified-student-email">({currentUser.email})</span>
              </p>
              <p className="identified-student-note">
                This report will be automatically submitted under your active authenticated session.
              </p>
            </div>

            {/* 1. Report Type Selector: LOST or FOUND */}
            <div className="type-selector-group">
              <span className="type-selector-label">
                Report Type <span style={{ color: 'var(--color-status-danger-text)' }}>*</span>
              </span>
              <div className="type-selector-options" role="radiogroup" aria-label="Select report type">
                <button
                  type="button"
                  className={`type-option-btn ${fields.reportType === 'LOST' ? 'type-option-btn--active' : ''}`}
                  onClick={() => handleFieldChange('reportType', 'LOST')}
                  role="radio"
                  aria-checked={fields.reportType === 'LOST'}
                  disabled={isSubmitting}
                >
                  <span className="type-option-title">I Lost Something</span>
                  <span className="type-option-hint">Register an item you have misplaced</span>
                </button>

                <button
                  type="button"
                  className={`type-option-btn ${fields.reportType === 'FOUND' ? 'type-option-btn--active' : ''}`}
                  onClick={() => handleFieldChange('reportType', 'FOUND')}
                  role="radio"
                  aria-checked={fields.reportType === 'FOUND'}
                  disabled={isSubmitting}
                >
                  <span className="type-option-title">I Found Something</span>
                  <span className="type-option-hint">Register an item discovered on campus</span>
                </button>
              </div>
            </div>

            {/* 2. Item Title */}
            <Input
              id="report-title"
              label="Item Title"
              placeholder="e.g. TI-84 Plus Graphing Calculator"
              hint="Concise, recognizable title (max 150 characters)"
              value={fields.title}
              onChange={(e) => handleFieldChange('title', e.target.value)}
              error={errors.title}
              required
              disabled={isSubmitting}
              maxLength={150}
            />

            {/* 3. Category */}
            <Input
              id="report-category"
              label="Category"
              placeholder="e.g. Electronics, Books, Personal Belongings, Keys"
              hint="Broad classification to assist filtering (max 50 characters)"
              value={fields.category}
              onChange={(e) => handleFieldChange('category', e.target.value)}
              error={errors.category}
              required
              disabled={isSubmitting}
              maxLength={50}
            />

            {/* 4. Campus Location */}
            <Input
              id="report-location"
              label="Campus Location"
              placeholder="e.g. Main Library 2nd Floor Room 204"
              hint="Specific campus building, room, or area (max 100 characters)"
              value={fields.location}
              onChange={(e) => handleFieldChange('location', e.target.value)}
              error={errors.location}
              required
              disabled={isSubmitting}
              maxLength={100}
            />

            {/* 5. Public Description */}
            <div className="ui-input-group">
              <label htmlFor="report-description" className="ui-input-label ui-input-label--required">
                Public Description
              </label>
              <textarea
                id="report-description"
                className={`ui-textarea-field ${errors.description ? 'ui-textarea-field--error' : ''}`}
                placeholder="General description visible to all students (e.g. Black backpack found in hallway)..."
                rows={3}
                value={fields.description}
                onChange={(e) => handleFieldChange('description', e.target.value)}
                disabled={isSubmitting}
                maxLength={1000}
                aria-invalid={Boolean(errors.description)}
                aria-describedby={errors.description ? 'report-description-error' : 'report-description-hint'}
                required
              />
              {!errors.description ? (
                <p id="report-description-hint" className="ui-input-hint">
                  Provide general description for public browsing (max 1000 characters). Do not reveal unique identifying secrets here.
                </p>
              ) : (
                <p id="report-description-error" className="ui-input-error" role="alert">
                  {errors.description}
                </p>
              )}
            </div>

            {/* 6. Milestone 13: FOUND Items Confidential Private Details Section */}
            {fields.reportType === 'FOUND' && (
              <div className="report-private-evidence-section">
                <div className="report-private-notice">
                  <span className="report-private-badge">🔒 Confidential Admin Evidence</span>
                  <h3 className="report-private-title">Private Identifying Information</h3>
                  <p className="report-private-desc">
                    These private distinguishing details and optional photograph are <strong>strictly hidden from the public catalog and student claimants</strong>. They are accessible only to university property desk administrators to verify ownership claims before approving an item return.
                  </p>
                </div>

                {/* Required Private Description */}
                <div className="ui-input-group">
                  <label htmlFor="report-private-description" className="ui-input-label ui-input-label--required">
                    Private Distinguishing Description *
                  </label>
                  <textarea
                    id="report-private-description"
                    className={`ui-textarea-field ${errors.privateDescription ? 'ui-textarea-field--error' : ''}`}
                    placeholder="Specify unique identifying details that only the true owner would know (e.g. serial numbers, wallpaper picture, inner pocket contents, engravings, scratches)..."
                    rows={4}
                    value={fields.privateDescription}
                    onChange={(e) => handleFieldChange('privateDescription', e.target.value)}
                    disabled={isSubmitting}
                    maxLength={1000}
                    aria-invalid={Boolean(errors.privateDescription)}
                    aria-describedby={errors.privateDescription ? 'report-private-desc-error' : 'report-private-desc-hint'}
                    required
                  />
                  {!errors.privateDescription ? (
                    <p id="report-private-desc-hint" className="ui-input-hint">
                      Required for FOUND items. Kept private to protect item ownership verification.
                    </p>
                  ) : (
                    <p id="report-private-desc-error" className="ui-input-error" role="alert">
                      {errors.privateDescription}
                    </p>
                  )}
                </div>

                {/* Optional Private Photo */}
                <div className="evidence-file-input-wrapper">
                  <label htmlFor="report-photo" className="ui-input-label">
                    Private Photograph of Found Item (Optional)
                  </label>
                  <input
                    id="report-photo"
                    type="file"
                    accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
                    className="evidence-file-input"
                    onChange={handleFileChange}
                    disabled={isSubmitting}
                  />
                  <p className="ui-input-hint" style={{ fontSize: 'var(--font-size-xs)' }}>
                    Supported formats: JPEG, PNG, WebP (Max 5 MB). Stored securely outside public directories.
                  </p>
                  {errors.photo && (
                    <p className="ui-input-error" role="alert">
                      {errors.photo}
                    </p>
                  )}

                  {fields.photoPreview && (
                    <div className="evidence-preview-box">
                      <img src={fields.photoPreview} alt="Preview of private photo" className="evidence-preview-img" />
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
              </div>
            )}

            {/* Form Actions */}
            <div className="report-form-actions">
              <Button
                variant="outline"
                type="button"
                onClick={onCancel}
                disabled={isSubmitting}
              >
                Cancel
              </Button>
              <Button
                variant="primary"
                type="submit"
                disabled={isSubmitting}
              >
                {isSubmitting ? 'Submitting report...' : `Submit ${fields.reportType.toLowerCase()} report`}
              </Button>
            </div>
          </form>
        </Card>
      </Container>
    </div>
  )
}
