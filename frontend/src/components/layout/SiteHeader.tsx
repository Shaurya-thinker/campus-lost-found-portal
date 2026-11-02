import type { AuthUser } from '../../types/auth'
import { Container, Button, Badge } from '../ui'
import './SiteHeader.css'

export interface SiteHeaderProps {
  currentUser: AuthUser | null
  onReportClick: () => void
  onNavigateHome: () => void
  onNavigateMyClaims: () => void
  onNavigateRegister: () => void
  onNavigateLogin: () => void
  onNavigateAdmin: () => void
  onSignOut: () => void
}

export function SiteHeader({
  currentUser,
  onReportClick,
  onNavigateHome,
  onNavigateMyClaims,
  onNavigateRegister,
  onNavigateLogin,
  onNavigateAdmin,
  onSignOut,
}: SiteHeaderProps) {
  const handleBrowseClick = (e: React.MouseEvent) => {
    e.preventDefault()
    onNavigateHome()
    setTimeout(() => {
      const el = document.getElementById('browse')
      if (el) {
        el.scrollIntoView({ behavior: 'smooth' })
      }
    }, 50)
  }

  const handleHowItWorksClick = (e: React.MouseEvent) => {
    e.preventDefault()
    onNavigateHome()
    setTimeout(() => {
      const el = document.getElementById('how-it-works')
      if (el) {
        el.scrollIntoView({ behavior: 'smooth' })
      }
    }, 50)
  }

  const handleBrandClick = (e: React.MouseEvent) => {
    e.preventDefault()
    onNavigateHome()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleMyClaimsClick = (e: React.MouseEvent) => {
    e.preventDefault()
    if (!currentUser) {
      onNavigateLogin()
    } else {
      onNavigateMyClaims()
    }
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const isAdmin = currentUser?.role === 'ADMIN'

  return (
    <header className="site-header" role="banner">
      <Container size="xl">
        <div className="site-header-inner">
          <a
            href="#home"
            className="site-brand"
            onClick={handleBrandClick}
            aria-label="Campus Lost &amp; Found Portal Home"
          >
            <span className="site-brand-title">Campus Lost &amp; Found</span>
            <span className="site-brand-desc">University property desk</span>
          </a>

          <div className="site-header-actions">
            <nav className="site-nav" aria-label="Primary navigation">
              <a href="#browse" className="site-nav-link" onClick={handleBrowseClick}>
                Browse items
              </a>
              <a href="#how-it-works" className="site-nav-link" onClick={handleHowItWorksClick}>
                How it works
              </a>
              <a href="#my-claims" className="site-nav-link" onClick={handleMyClaimsClick}>
                My claims
              </a>

              {/* Admin Portal is strictly visible to verified administrators */}
              {isAdmin && (
                <a
                  href="#admin"
                  className="site-nav-link site-nav-link--admin"
                  onClick={(e) => {
                    e.preventDefault()
                    onNavigateAdmin()
                  }}
                >
                  <span className="site-admin-nav-icon" aria-hidden="true">🛡️</span>
                  <span>Admin Portal</span>
                </a>
              )}

              {!currentUser && (
                <>
                  <a
                    href="#login"
                    className="site-nav-link"
                    onClick={(e) => {
                      e.preventDefault()
                      onNavigateLogin()
                    }}
                  >
                    Sign In
                  </a>
                  <a
                    href="#register"
                    className="site-nav-link site-nav-link--highlight"
                    onClick={(e) => {
                      e.preventDefault()
                      onNavigateRegister()
                    }}
                  >
                    Register
                  </a>
                </>
              )}
            </nav>

            {/* Session Identity or Action button */}
            {currentUser ? (
              <div className="site-session-box">
                <span className="site-session-badge" title={`Signed in as ${currentUser.email} (${currentUser.role})`}>
                  <span className="site-session-icon" aria-hidden="true">
                    {isAdmin ? '🛡️' : '👤'}
                  </span>
                  <span className="site-session-name">{currentUser.name}</span>
                  <span className="site-session-id">
                    {currentUser.collegeUid || `#${currentUser.id}`}
                  </span>
                  {isAdmin && (
                    <Badge variant="warning" className="site-admin-role-badge">
                      Admin
                    </Badge>
                  )}
                </span>
                <button
                  type="button"
                  className="site-signout-btn"
                  onClick={onSignOut}
                  title="Sign out of this browser session"
                  aria-label="Sign out"
                >
                  Sign out
                </button>
              </div>
            ) : (
              <Button
                variant="outline"
                size="sm"
                onClick={onNavigateLogin}
              >
                Sign In
              </Button>
            )}

            <Button
              variant="primary"
              size="sm"
              onClick={onReportClick}
            >
              Report an item
            </Button>
          </div>
        </div>
      </Container>
    </header>
  )
}
