import { useState, useEffect, type FormEvent } from 'react'
import type { ReportType, ItemReport } from '../../types/report'
import type { AuthUser } from '../../types/auth'
import { createReport, ApiError } from '../../services/reportsApi'
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
}

interface FormErrors {
  reportType?: string
  title?: string
  category?: string
  location?: string
  description?: string
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
  })

  const [errors, setErrors] = useState<FormErrors>({})
  const [serverError, setServerError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false)
  const [submittedReport, setSubmittedReport] = useState<ItemReport | null>(null)

  // Synchronize report type if initialType changes from parent/hash navigation
  useEffect(() => {
    setFields((prev) => ({ ...prev, reportType: initialType }))
  }, [initialType])

  const handleFieldChange = (field: keyof FormFields, value: string) => {
    setFields((prev) => ({ ...prev, [field]: value }))
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
    if (serverError) {
      setServerError(null)
    }
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
      const created = await createReport({
        title: fields.title.trim(),
        category: fields.category.trim(),
        location: fields.location.trim(),
        description: fields.description.trim(),
        reportType: fields.reportType,
      })

      // Backend confirmed creation with HTTP 201 Created and persisted entity
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
    setFields({
      reportType: initialType,
      title: '',
      category: '',
      location: '',
      description: '',
    })
    setErrors({})
    setServerError(null)
    setSubmittedReport(null)
  }

  // If no user is authenticated, show prompt with sign-in and registration links
  if (!currentUser) {
    return (
      <div className="report-form-page">
        <Container size="md">
          <div className="report-form-header">
            <button
              type="button"
              className="report-back-btn"
              onClick={onCancel}
              aria-label="Return to catalog"
            >
              &larr; Back to catalog
            </button>
            <h1 className="report-form-title">Authentication Required</h1>
            <p className="report-form-desc">
              To file a lost or found property report, please authenticate using your campus credentials.
              This ensures reports are verified and accountable to enrolled campus members.
            </p>
          </div>

          <Card className="report-form-card" style={{ textAlign: 'center', padding: 'var(--space-8)' }}>
            <h2 style={{ fontFamily: 'var(--font-family-display)', fontSize: 'var(--font-size-2xl)', color: 'var(--color-navy)', marginBottom: 'var(--space-2)' }}>
              Sign In to Submit Reports
            </h2>
            <p style={{ color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-sm)', maxWidth: '480px', margin: '0 auto var(--space-6)', lineHeight: 'var(--line-height-normal)' }}>
              Sign in with your College UID and password to report items. Your identity and student role will be automatically verified by the server.
            </p>
            <div style={{ display: 'flex', gap: 'var(--space-3)', justifyContent: 'center', flexWrap: 'wrap' }}>
              {onNavigateLogin && (
                <Button variant="primary" onClick={onNavigateLogin}>
                  Sign In
                </Button>
              )}
              {onNavigateRegister && (
                <Button variant="outline" onClick={onNavigateRegister}>
                  Register as a student
                </Button>
              )}
              <Button variant="ghost" onClick={onCancel}>
                Return to catalog
              </Button>
            </div>
          </Card>
        </Container>
      </div>
    )
  }

  // 3. Success state: display confirmation with actual backend response values
  if (submittedReport) {
    return (
      <div className="report-form-page">
        <Container size="lg">
          <Card className="report-success-card">
            <Badge variant="success">Report Logged Successfully</Badge>
            <h2 className="report-success-title">Item Registered in Campus Catalog</h2>
            <p style={{ color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-sm)' }}>
              Your {submittedReport.reportType.toLowerCase()} item report has been cataloged and is now visible to campus staff and students.
            </p>

            <div className="report-success-details">
              <div className="report-success-row">
                <span style={{ color: 'var(--color-text-muted)' }}>Report Reference ID:</span>
                <strong>#{submittedReport.id}</strong>
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
                placeholder="Include color, brand, stickers, unique scratches, or contents inside..."
                rows={4}
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
                  Provide distinguishing features to verify ownership before return (max 1000 characters).
                </p>
              ) : (
                <p id="report-description-error" className="ui-input-error" role="alert">
                  {errors.description}
                </p>
              )}
            </div>

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
