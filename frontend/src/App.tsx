import { useState, useEffect } from 'react'
import type { ReportType, ItemReport } from './types/report'
import type { AuthUser } from './types/auth'
import { getAuthUser, clearAuthSession } from './utils/auth'
import { fetchReportById } from './services/reportsApi'
import { SiteHeader } from './components/layout/SiteHeader'
import { SiteFooter } from './components/layout/SiteFooter'
import { HomePage } from './components/home/HomePage'
import { ReportForm } from './components/report/ReportForm'
import { ItemDetailsView } from './components/claims/ItemDetailsView'
import { MyClaimsView } from './components/claims/MyClaimsView'
import { RegisterPage } from './components/auth/RegisterPage'
import { LoginPage } from './components/auth/LoginPage'
import { AdminDashboard } from './components/admin/AdminDashboard'

export type AppView = 'home' | 'report' | 'item-details' | 'my-claims' | 'register' | 'login' | 'admin'

function App() {
  const [currentView, setCurrentView] = useState<AppView>('home')
  const [reportType, setReportType] = useState<ReportType>('LOST')
  const [selectedItem, setSelectedItem] = useState<ItemReport | null>(null)
  const [myClaimsClaimantId, setMyClaimsClaimantId] = useState<number | undefined>(undefined)
  const [refreshTrigger, setRefreshTrigger] = useState<number>(0)

  // Authenticated user state restored from sessionStorage
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(() => getAuthUser())

  // Synchronize view with hash changes for browser back/forward support and direct hash links
  useEffect(() => {
    const handleHashChange = () => {
      const hash = window.location.hash
      if (hash.startsWith('#login')) {
        setCurrentView('login')
      } else if (hash.startsWith('#register')) {
        setCurrentView('register')
      } else if (hash.startsWith('#admin')) {
        setCurrentView('admin')
      } else if (hash.startsWith('#report')) {
        setCurrentView('report')
        if (hash.includes('type=FOUND')) {
          setReportType('FOUND')
        } else if (hash.includes('type=LOST')) {
          setReportType('LOST')
        }
      } else if (hash.startsWith('#my-claims')) {
        setCurrentView('my-claims')
      } else if (hash.startsWith('#item-details')) {
        // Direct hash link support: #item-details?id=123
        const queryString = hash.split('?')[1] || ''
        const params = new URLSearchParams(queryString)
        const idParam = params.get('id')

        if (idParam) {
          const parsedId = Number(idParam)
          if (selectedItem && selectedItem.id === parsedId) {
            setCurrentView('item-details')
          } else {
            fetchReportById(parsedId)
              .then((item) => {
                setSelectedItem(item)
                setCurrentView('item-details')
              })
              .catch(() => {
                setCurrentView('home')
              })
          }
        } else if (selectedItem) {
          setCurrentView('item-details')
        } else {
          setCurrentView('home')
        }
      } else {
        setCurrentView('home')
      }
    }

    handleHashChange()
    window.addEventListener('hashchange', handleHashChange)
    return () => window.removeEventListener('hashchange', handleHashChange)
  }, [selectedItem])

  const handleOpenReport = (type: ReportType = 'LOST') => {
    setReportType(type)
    setCurrentView('report')
    window.location.hash = `#report?type=${type}`
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleNavigateHome = () => {
    setCurrentView('home')
    window.location.hash = '#home'
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleNavigateLogin = () => {
    setCurrentView('login')
    window.location.hash = '#login'
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleNavigateRegister = () => {
    setCurrentView('register')
    window.location.hash = '#register'
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleNavigateAdmin = () => {
    setCurrentView('admin')
    window.location.hash = '#admin'
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleNavigateMyClaims = (claimantId?: number) => {
    if (claimantId) {
      setMyClaimsClaimantId(claimantId)
    } else if (currentUser) {
      setMyClaimsClaimantId(currentUser.id)
    }
    setCurrentView('my-claims')
    window.location.hash = '#my-claims'
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleViewItemDetails = (item: ItemReport) => {
    setSelectedItem(item)
    setCurrentView('item-details')
    window.location.hash = `#item-details?id=${item.id}`
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleLoginSuccess = (user: AuthUser) => {
    setCurrentUser(user)
    if (user.role === 'ADMIN') {
      handleNavigateAdmin()
    } else {
      handleNavigateHome()
    }
  }

  const handleRegisterSuccess = (user: AuthUser) => {
    setCurrentUser(user)
  }

  const handleSignOut = () => {
    clearAuthSession()
    setCurrentUser(null)
    setMyClaimsClaimantId(undefined)
    handleNavigateHome()
  }

  const handleReportSuccess = (_newReport: ItemReport) => {
    setRefreshTrigger((prev) => prev + 1)
  }

  return (
    <div className="app-shell">
      <a href="#main-content" className="skip-link">
        Skip to main content
      </a>

      <SiteHeader
        currentUser={currentUser}
        onReportClick={() => handleOpenReport('LOST')}
        onNavigateHome={handleNavigateHome}
        onNavigateMyClaims={() => handleNavigateMyClaims()}
        onNavigateRegister={handleNavigateRegister}
        onNavigateLogin={handleNavigateLogin}
        onNavigateAdmin={handleNavigateAdmin}
        onSignOut={handleSignOut}
      />

      {currentView === 'login' && (
        <LoginPage
          onLoginSuccess={handleLoginSuccess}
          onNavigateRegister={handleNavigateRegister}
          onCancel={handleNavigateHome}
        />
      )}

      {currentView === 'register' && (
        <RegisterPage
          onRegisterSuccess={handleRegisterSuccess}
          onNavigateLogin={handleNavigateLogin}
          onCancel={handleNavigateHome}
          onNavigateReport={() => handleOpenReport('LOST')}
        />
      )}

      {currentView === 'report' && (
        <ReportForm
          initialType={reportType}
          currentUser={currentUser}
          onCancel={handleNavigateHome}
          onSuccess={handleReportSuccess}
          onNavigateRegister={handleNavigateRegister}
          onNavigateLogin={handleNavigateLogin}
        />
      )}

      {currentView === 'item-details' && selectedItem && (
        <ItemDetailsView
          item={selectedItem}
          currentUser={currentUser}
          onBack={handleNavigateHome}
          onViewMyClaims={handleNavigateMyClaims}
          onNavigateRegister={handleNavigateRegister}
          onNavigateLogin={handleNavigateLogin}
        />
      )}

      {currentView === 'my-claims' && (
        <MyClaimsView
          currentUser={currentUser}
          initialClaimantId={myClaimsClaimantId}
          onBack={handleNavigateHome}
          onNavigateRegister={handleNavigateRegister}
          onNavigateLogin={handleNavigateLogin}
        />
      )}

      {currentView === 'admin' && (
        <AdminDashboard
          currentUser={currentUser}
          onNavigateHome={handleNavigateHome}
          onNavigateReport={() => handleOpenReport('LOST')}
          onNavigateLogin={handleNavigateLogin}
        />
      )}

      {currentView === 'home' && (
        <HomePage
          onOpenReportForm={handleOpenReport}
          onViewDetails={handleViewItemDetails}
          refreshTrigger={refreshTrigger}
        />
      )}

      <SiteFooter />
    </div>
  )
}

export default App
