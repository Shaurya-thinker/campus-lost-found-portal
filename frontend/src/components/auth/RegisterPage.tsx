import { useState, type FormEvent } from 'react'
import type { AuthUser } from '../../types/auth'
import { register } from '../../services/authApi'
import { setAuthSession } from '../../utils/auth'
import { ApiError } from '../../services/reportsApi'
import { Container, Card, Button, Input, Badge } from '../ui'
import './RegisterPage.css'

export interface RegisterPageProps {
  onRegisterSuccess: (user: AuthUser) => void
  onNavigateLogin?: () => void
  onCancel: () => void
  onNavigateReport?: () => void
}

export function RegisterPage({
  onRegisterSuccess,
  onNavigateLogin,
  onCancel,
  onNavigateReport,
}: RegisterPageProps) {
  const [collegeUid, setCollegeUid] = useState<string>('')
  const [name, setName] = useState<string>('')
  const [email, setEmail] = useState<string>('')
  const [password, setPassword] = useState<string>('')
  const [confirmPassword, setConfirmPassword] = useState<string>('')

  const [uidError, setUidError] = useState<string | null>(null)
  const [nameError, setNameError] = useState<string | null>(null)
  const [emailError, setEmailError] = useState<string | null>(null)
  const [passwordError, setPasswordError] = useState<string | null>(null)
  const [confirmError, setConfirmError] = useState<string | null>(null)
  const [serverError, setServerError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false)
  const [registeredUser, setRegisteredUser] = useState<AuthUser | null>(null)

  const validate = (): boolean => {
    let isValid = true
    setUidError(null)
    setNameError(null)
    setEmailError(null)
    setPasswordError(null)
    setConfirmError(null)

    const trimmedUid = collegeUid.trim()
    if (!trimmedUid) {
      setUidError('College UID is required (e.g. 24BCS10586)')
      isValid = false
    } else if (trimmedUid.length > 50) {
      setUidError('College UID must not exceed 50 characters')
      isValid = false
    }

    const trimmedName = name.trim()
    if (!trimmedName) {
      setNameError('Full name is required')
      isValid = false
    } else if (trimmedName.length > 100) {
      setNameError('Name must not exceed 100 characters')
      isValid = false
    }

    const trimmedEmail = email.trim()
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!trimmedEmail) {
      setEmailError('Campus email address is required')
      isValid = false
    } else if (!emailRegex.test(trimmedEmail)) {
      setEmailError('Please enter a valid email format (e.g. student@campus.edu)')
      isValid = false
    } else if (trimmedEmail.length > 150) {
      setEmailError('Email must not exceed 150 characters')
      isValid = false
    }

    if (!password) {
      setPasswordError('Password is required')
      isValid = false
    } else if (password.length < 12) {
      setPasswordError('Password must be at least 12 characters long')
      isValid = false
    }

    if (password !== confirmPassword) {
      setConfirmError('Passwords do not match')
      isValid = false
    }

    return isValid
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setServerError(null)

    if (!validate()) {
      return
    }

    setIsSubmitting(true)

    try {
      const response = await register({
        collegeUid: collegeUid.trim().toUpperCase(),
        name: name.trim(),
        email: email.trim().toLowerCase(),
        password,
      })

      // Establish tab-scoped authentication session
      setAuthSession(response.token, response.user)
      setRegisteredUser(response.user)
      onRegisterSuccess(response.user)
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          setServerError(err.message)
        } else {
          setServerError(err.message)
          if (err.fieldErrors) {
            if (err.fieldErrors.collegeUid) setUidError(err.fieldErrors.collegeUid)
            if (err.fieldErrors.name) setNameError(err.fieldErrors.name)
            if (err.fieldErrors.email) setEmailError(err.fieldErrors.email)
            if (err.fieldErrors.password) setPasswordError(err.fieldErrors.password)
          }
        }
      } else {
        setServerError('Network error or server unreachable. Please verify backend connection.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleReset = () => {
    setCollegeUid('')
    setName('')
    setEmail('')
    setPassword('')
    setConfirmPassword('')
    setUidError(null)
    setNameError(null)
    setEmailError(null)
    setPasswordError(null)
    setConfirmError(null)
    setServerError(null)
    setRegisteredUser(null)
  }

  if (registeredUser) {
    return (
      <div className="register-page">
        <Container size="md">
          <Card className="register-success-card">
            <Badge variant="success">Registration &amp; Authentication Confirmed</Badge>
            <h2 className="register-success-title">Welcome, {registeredUser.name}</h2>
            <p className="register-success-desc">
              Your account has been securely created with BCrypt password protection. You are now
              authenticated with the STUDENT role for this browser session.
            </p>

            <div className="register-success-details">
              <div className="register-success-row">
                <span className="register-detail-label">College UID:</span>
                <strong className="register-detail-val">{registeredUser.collegeUid}</strong>
              </div>
              <div className="register-success-row">
                <span className="register-detail-label">Assigned User ID:</span>
                <strong className="register-detail-val">#{registeredUser.id}</strong>
              </div>
              <div className="register-success-row">
                <span className="register-detail-label">Full Name:</span>
                <span className="register-detail-val">{registeredUser.name}</span>
              </div>
              <div className="register-success-row">
                <span className="register-detail-label">Campus Email:</span>
                <span className="register-detail-val">{registeredUser.email}</span>
              </div>
              <div className="register-success-row">
                <span className="register-detail-label">System Role:</span>
                <Badge variant="info">{registeredUser.role}</Badge>
              </div>
            </div>

            <div className="register-success-actions">
              <Button
                variant="primary"
                onClick={() => {
                  handleReset()
                  onCancel()
                }}
              >
                Return to catalog
              </Button>
              {onNavigateReport && (
                <Button
                  variant="outline"
                  onClick={() => {
                    handleReset()
                    onNavigateReport()
                  }}
                >
                  File an item report
                </Button>
              )}
            </div>
          </Card>
        </Container>
      </div>
    )
  }

  return (
    <div className="register-page">
      <Container size="md">
        <div className="register-header">
          <button
            type="button"
            className="register-back-btn"
            onClick={onCancel}
            aria-label="Return to catalog"
          >
            &larr; Back to catalog
          </button>
          <h1 className="register-title">Register as a Student</h1>
          <p className="register-desc">
            Register your verified college credentials to report lost/found items, file ownership
            claims, and track property return statuses across campus facilities.
          </p>
        </div>

        <Card className="register-card">
          <form className="register-form" onSubmit={handleSubmit} noValidate>
            {serverError && (
              <div className="form-alert form-alert--danger" role="alert">
                <strong>Registration Error:</strong>
                <span>{serverError}</span>
              </div>
            )}

            <Input
              id="student-college-uid"
              label="College UID"
              placeholder="e.g. 24BCS10586"
              hint="Your university registration / roll number (unique)"
              value={collegeUid}
              onChange={(e) => {
                setCollegeUid(e.target.value.toUpperCase())
                if (uidError) setUidError(null)
                if (serverError) setServerError(null)
              }}
              error={uidError || undefined}
              required
              disabled={isSubmitting}
              maxLength={50}
              autoFocus
            />

            <Input
              id="student-name"
              label="Full Name"
              placeholder="e.g. Alice Smith"
              hint="Your official university directory name (max 100 characters)"
              value={name}
              onChange={(e) => {
                setName(e.target.value)
                if (nameError) setNameError(null)
                if (serverError) setServerError(null)
              }}
              error={nameError || undefined}
              required
              disabled={isSubmitting}
              maxLength={100}
            />

            <Input
              id="student-email"
              label="Campus Email Address"
              type="email"
              placeholder="e.g. alice.smith@campus.edu"
              hint="Unique institutional email address (max 150 characters)"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value)
                if (emailError) setEmailError(null)
                if (serverError) setServerError(null)
              }}
              error={emailError || undefined}
              required
              disabled={isSubmitting}
              maxLength={150}
            />

            <Input
              id="student-password"
              label="Password"
              type="password"
              placeholder="Minimum 12 characters"
              hint="Choose a strong password (minimum 12 characters)"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value)
                if (passwordError) setPasswordError(null)
                if (serverError) setServerError(null)
              }}
              error={passwordError || undefined}
              required
              disabled={isSubmitting}
            />

            <Input
              id="student-confirm-password"
              label="Confirm Password"
              type="password"
              placeholder="Re-enter your password"
              value={confirmPassword}
              onChange={(e) => {
                setConfirmPassword(e.target.value)
                if (confirmError) setConfirmError(null)
                if (serverError) setServerError(null)
              }}
              error={confirmError || undefined}
              required
              disabled={isSubmitting}
            />

            <div className="register-notice-box">
              <p className="register-notice-title">Security &amp; Authorization</p>
              <p>
                All student accounts are protected with salted BCrypt password hashing. Self-registration
                exclusively assigns the <strong>STUDENT</strong> role. Administrative access is provisioned
                separately through secure procedures.
              </p>
            </div>

            <div className="register-actions">
              <Button variant="outline" type="button" onClick={onCancel} disabled={isSubmitting}>
                Cancel
              </Button>
              <Button variant="primary" type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Registering...' : 'Register Account'}
              </Button>
            </div>

            {onNavigateLogin && (
              <div style={{ textAlign: 'center', marginTop: 'var(--space-4)', fontSize: 'var(--font-size-sm)' }}>
                Already have an account?{' '}
                <button
                  type="button"
                  className="register-back-btn"
                  style={{ display: 'inline', margin: 0, padding: 0, textDecoration: 'underline' }}
                  onClick={onNavigateLogin}
                >
                  Sign in here
                </button>
              </div>
            )}
          </form>
        </Card>
      </Container>
    </div>
  )
}
