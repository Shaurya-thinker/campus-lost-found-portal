import { useState, useEffect } from 'react'
import type { Claim } from '../../types/claim'
import type { AuthUser } from '../../types/auth'
import { fetchClaimsByClaimant } from '../../services/claimsApi'
import { ApiError } from '../../services/reportsApi'
import { Container, Card, Badge, Button } from '../ui'
import './MyClaimsView.css'

export interface MyClaimsViewProps {
  currentUser: AuthUser | null
  initialClaimantId?: number
  onBack: () => void
  onNavigateRegister?: () => void
  onNavigateLogin?: () => void
}

export function MyClaimsView({
  currentUser,
  initialClaimantId,
  onBack,
  onNavigateRegister,
  onNavigateLogin,
}: MyClaimsViewProps) {
  const effectiveInitialId = initialClaimantId || currentUser?.id || null

  const [activeClaimantId, setActiveClaimantId] = useState<number | null>(effectiveInitialId)
  const [claims, setClaims] = useState<Claim[]>([])
  const [loading, setLoading] = useState<boolean>(false)
  const [error, setError] = useState<string | null>(null)
  const [hasSearched, setHasSearched] = useState<boolean>(Boolean(effectiveInitialId))

  // Sync if currentUser or initialClaimantId changes
  useEffect(() => {
    const targetId = initialClaimantId || currentUser?.id || null
    if (targetId !== activeClaimantId) {
      setActiveClaimantId(targetId)
      setHasSearched(Boolean(targetId))
      if (!targetId) {
        setClaims([])
        setError(null)
      }
    }
  }, [currentUser, initialClaimantId, activeClaimantId])

  useEffect(() => {
    if (!activeClaimantId) return

    let isCurrent = true
    setLoading(true)
    setError(null)

    async function load() {
      try {
        const results = await fetchClaimsByClaimant(activeClaimantId!)
        if (isCurrent) {
          setClaims(results)
        }
      } catch (err) {
        if (isCurrent) {
          const msg = err instanceof ApiError ? err.message : 'Unable to retrieve claims'
          setError(msg)
          setClaims([])
        }
      } finally {
        if (isCurrent) {
          setLoading(false)
        }
      }
    }

    load()

    return () => {
      isCurrent = false
    }
  }, [activeClaimantId])

  return (
    <div className="my-claims-page">
      <Container size="lg">
        <div className="my-claims-header">
          <button
            type="button"
            className="item-details-back-btn"
            onClick={onBack}
            aria-label="Return to catalog browsing"
          >
            &larr; Back to catalog
          </button>
          <h1 className="my-claims-title">My Property Claims</h1>
          <p className="my-claims-desc">
            Check the verification and evaluation status of claims you submitted for items turned in across campus.
          </p>
        </div>

        {/* When no user in session */}
        {!currentUser && (
          <Card className="browse-state-card" variant="subdued" style={{ maxWidth: '800px', margin: '0 auto var(--space-6)' }}>
            <h3 className="browse-state-title">Authentication Required</h3>
            <p className="browse-state-desc">
              Sign in with your campus credentials to view and track your submitted ownership claims.
            </p>
            <div style={{ display: 'flex', gap: 'var(--space-3)', justifyContent: 'center', marginTop: 'var(--space-4)' }}>
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
          </Card>
        )}

        {/* Current User Banner */}
        {currentUser && (
          <div className="identified-student-box" style={{ maxWidth: '800px', margin: '0 auto var(--space-4)' }}>
            <div className="identified-student-header">
              <span className="identified-student-badge">Active Authenticated Session</span>
              <span className="identified-student-id">
                {currentUser.collegeUid || `ID #${currentUser.id}`}
              </span>
            </div>
            <p className="identified-student-name">
              {currentUser.name} <span className="identified-student-email">({currentUser.email})</span>
            </p>
            <p className="identified-student-note">
              Displaying property claims submitted under your authenticated account record.
            </p>
          </div>
        )}

        {/* Error Notification */}
        {error && (
          <div className="form-alert form-alert--danger" style={{ maxWidth: '800px', margin: '0 auto var(--space-6)' }} role="alert">
            <strong>Error:</strong>
            <span>{error}</span>
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <Card className="browse-state-card" variant="subdued" style={{ maxWidth: '800px' }}>
            <h3 className="browse-state-title">Retrieving your claims...</h3>
            <p className="browse-state-desc">
              Checking claim evaluation records with the campus registry.
            </p>
          </Card>
        )}

        {/* Empty State */}
        {!loading && hasSearched && claims.length === 0 && !error && (
          <Card className="browse-state-card" variant="default" style={{ maxWidth: '800px' }}>
            <h3 className="browse-state-title">No Claims on File</h3>
            <p className="browse-state-desc">
              No ownership claims were found for student record #{activeClaimantId}.
            </p>
            <p className="browse-state-desc" style={{ fontSize: 'var(--font-size-xs)' }}>
              If you lost an item that matches a turned-in listing, you can open that item in the catalog and submit a claim.
            </p>
            <Button variant="outline" size="sm" onClick={onBack}>
              Browse catalog items
            </Button>
          </Card>
        )}

        {/* Claims List */}
        {!loading && claims.length > 0 && (
          <div className="my-claims-list">
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--color-text-secondary)' }}>
              Showing {claims.length} {claims.length === 1 ? 'claim' : 'claims'} filed under Student ID #{activeClaimantId}
            </p>

            {claims.map((claim) => {
              let badgeVariant: 'warning' | 'success' | 'danger' = 'warning'
              if (claim.claimStatus === 'APPROVED') badgeVariant = 'success'
              if (claim.claimStatus === 'REJECTED') badgeVariant = 'danger'

              return (
                <Card key={claim.id} className="claim-record-card">
                  <div className="claim-record-header">
                    <div>
                      <h3 className="claim-record-title">
                        {claim.itemTitle || `Claim for Item #${claim.itemReportId}`}
                      </h3>
                      <div className="claim-record-meta">
                        <span>Claim Ref: #{claim.id}</span>
                        <span>&bull;</span>
                        <span>Filed: {claim.claimDate}</span>
                        {claim.claimantName && (
                          <>
                            <span>&bull;</span>
                            <span>Claimant: {claim.claimantName}</span>
                          </>
                        )}
                      </div>
                    </div>
                    <Badge variant={badgeVariant}>{claim.claimStatus}</Badge>
                  </div>

                  <div className="claim-record-desc-box">
                    <strong style={{ display: 'block', fontSize: 'var(--font-size-xs)', color: 'var(--color-text-muted)', marginBottom: 'var(--space-1)' }}>
                      Submitted Proof of Ownership:
                    </strong>
                    <p>{claim.description}</p>
                  </div>

                  <div className={`claim-record-status-note claim-record-status-note--${claim.claimStatus.toLowerCase()}`}>
                    {claim.claimStatus === 'PENDING' && (
                      <p>
                        &bull; Pending Review: Campus property staff will verify your details against the physical item.
                      </p>
                    )}
                    {claim.claimStatus === 'APPROVED' && (
                      <p>
                        &bull; Claim Approved: Please bring your campus identification to the central property desk to collect your item.
                      </p>
                    )}
                    {claim.claimStatus === 'REJECTED' && (
                      <p>
                        &bull; Claim Closed: The submitted details could not be matched with the turned-in property.
                      </p>
                    )}
                  </div>
                </Card>
              )
            })}
          </div>
        )}
      </Container>
    </div>
  )
}
