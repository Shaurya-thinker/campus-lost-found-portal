import { useState, type FormEvent } from 'react'
import type { AuthUser } from '../../types/auth'
import { login } from '../../services/authApi'
import { setAuthSession } from '../../utils/auth'
import { ApiError } from '../../services/reportsApi'
import { Container, Card, Button, Input } from '../ui'
import './LoginPage.css'

export interface LoginPageProps {
  onLoginSuccess: (user: AuthUser) => void
  onNavigateRegister: () => void
  onCancel: () => void
}

export function LoginPage({ onLoginSuccess, onNavigateRegister, onCancel }: LoginPageProps) {
  const [collegeUid, setCollegeUid] = useState<string>('')
  const [password, setPassword] = useState<string>('')

  const [uidError, setUidError] = useState<string | null>(null)
  const [passwordError, setPasswordError] = useState<string | null>(null)
  const [serverError, setServerError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false)

  const validate = (): boolean => {
    let isValid = true
    setUidError(null)
    setPasswordError(null)

    const trimmedUid = collegeUid.trim()
    if (!trimmedUid) {
      setUidError('College UID or campus email is required')
      isValid = false
    }

    if (!password) {
      setPasswordError('Password is required')
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
      const response = await login({
        collegeUid: collegeUid.trim().toUpperCase(),
        password,
      })

      // Store token and user in secure tab-scoped sessionStorage
      setAuthSession(response.token, response.user)
      onLoginSuccess(response.user)
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setServerError(err.message)
      } else if (err instanceof Error) {
        setServerError(err.message)
      } else {
        setServerError('An unexpected error occurred during sign-in. Please try again.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main id="main-content" className="login-page">
      <Container size="sm">
        <div className="login-card-wrapper">
          <header className="login-header">
            <h1 className="login-title">Sign In to Campus Portal</h1>
            <p className="login-subtitle">
              Authenticate using your College UID and password to report items, manage claims, or
              access administrative controls.
            </p>
          </header>

          <Card variant="default">
            <form onSubmit={handleSubmit} className="login-form" noValidate>
              {serverError && (
                <div className="login-error-banner" role="alert">
                  {serverError}
                </div>
              )}

              <Input
                label="College UID or Campus Email"
                id="login-college-uid"
                value={collegeUid}
                onChange={(e) => setCollegeUid(e.target.value)}
                placeholder="e.g. 24BCS10586 or student@campus.edu"
                required
                error={uidError || undefined}
                hint="Your unique university roll, registered email, or admin identifier"
                disabled={isSubmitting}
                autoFocus
              />

              <Input
                label="Password"
                id="login-password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your account password"
                required
                error={passwordError || undefined}
                disabled={isSubmitting}
              />

              <div className="login-actions">
                <Button
                  type="submit"
                  variant="primary"
                  size="md"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? 'Signing In...' : 'Sign In'}
                </Button>

                <Button
                  type="button"
                  variant="ghost"
                  size="md"
                  onClick={onCancel}
                  disabled={isSubmitting}
                >
                  Cancel
                </Button>
              </div>
            </form>
          </Card>

          <div className="login-footer-links">
            Don't have a student account yet?{' '}
            <button
              type="button"
              className="login-link-btn"
              onClick={onNavigateRegister}
            >
              Register here
            </button>
          </div>
        </div>
      </Container>
    </main>
  )
}

