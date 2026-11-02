import { useState, useEffect, useMemo } from 'react'
import type { ItemReport, ReportType, ItemStatus } from '../../types/report'
import type { Claim, ClaimStatus } from '../../types/claim'
import type { AuthUser } from '../../types/auth'
import type {
  LocationCountReport,
  CategoryCountReport,
  UnclaimedItemDetail,
} from '../../types/analytics'
import { fetchReports, markReportAsReturned } from '../../services/reportsApi'
import {
  fetchClaimsByStatus,
  approveClaim,
  rejectClaim,
} from '../../services/claimsApi'
import {
  fetchUnclaimedByLocation,
  fetchUnclaimedByCategory,
  fetchUnclaimedDetails,
} from '../../services/analyticsApi'
import { Container, Button, Badge, Card } from '../ui'
import { fetchClaimVerificationDetails, requestMoreInfoForClaim } from '../../services/evidenceApi'
import { PrivateEvidenceImage } from '../evidence/PrivateEvidenceImage'
import type { ClaimVerificationDetails } from '../../types/evidence'
import './AdminDashboard.css'

export type AdminTab = 'overview' | 'reports' | 'claims'

export interface AdminDashboardProps {
  currentUser?: AuthUser | null
  onNavigateHome: () => void
  onNavigateReport: () => void
  onNavigateLogin?: () => void
}

export function AdminDashboard({
  currentUser,
  onNavigateHome,
  onNavigateReport,
  onNavigateLogin,
}: AdminDashboardProps) {
  const [activeTab, setActiveTab] = useState<AdminTab>('overview')

  // Main state
  const [reports, setReports] = useState<ItemReport[]>([])
  const [claims, setClaims] = useState<Claim[]>([])
  const [locationAnalytics, setLocationAnalytics] = useState<LocationCountReport[]>([])
  const [categoryAnalytics, setCategoryAnalytics] = useState<CategoryCountReport[]>([])
  const [unclaimedDetails, setUnclaimedDetails] = useState<UnclaimedItemDetail[]>([])

  // Loading & In-flight state
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null)
  const [processingReportId, setProcessingReportId] = useState<number | null>(null)
  const [processingClaimId, setProcessingClaimId] = useState<number | null>(null)

  // Report Filters
  const [reportTypeFilter, setReportTypeFilter] = useState<ReportType | 'ALL'>('ALL')
  const [reportStatusFilter, setReportStatusFilter] = useState<ItemStatus | 'ALL'>('ALL')
  const [reportSearchQuery, setReportSearchQuery] = useState<string>('')

  // Claims Filter
  const [claimStatusFilter, setClaimStatusFilter] = useState<ClaimStatus>('PENDING')
  const [expandedClaimId, setExpandedClaimId] = useState<number | null>(null)
  const [verificationData, setVerificationData] = useState<Record<number, ClaimVerificationDetails>>({})
  const [loadingVerificationId, setLoadingVerificationId] = useState<number | null>(null)
  const [verificationError, setVerificationError] = useState<Record<number, string>>({})

  // Initial load & refresh function
  const loadDashboardData = async () => {
    setLoading(true)
    setError(null)
    try {
      const [
        reportsData,
        pendingClaimsData,
        locData,
        catData,
        detailsData,
      ] = await Promise.all([
        fetchReports('ALL'),
        fetchClaimsByStatus('PENDING'),
        fetchUnclaimedByLocation(),
        fetchUnclaimedByCategory(),
        fetchUnclaimedDetails(),
      ])

      setReports(reportsData)
      setClaims(pendingClaimsData)
      setLocationAnalytics(locData)
      setCategoryAnalytics(catData)
      setUnclaimedDetails(detailsData)
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load admin dashboard data.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (currentUser?.role === 'ADMIN') {
      loadDashboardData()
    }
  }, [currentUser])

  // Refresh claims whenever claimStatusFilter changes
  useEffect(() => {
    if (currentUser?.role !== 'ADMIN') {
      return
    }
    let isCancelled = false
    const loadFilteredClaims = async () => {
      try {
        const claimsData = await fetchClaimsByStatus(claimStatusFilter)
        if (!isCancelled) {
          setClaims(claimsData)
        }
      } catch (err: unknown) {
        if (!isCancelled) {
          const msg = err instanceof Error ? err.message : 'Failed to load claims.'
          setFeedback({ type: 'error', message: msg })
        }
      }
    }

    loadFilteredClaims()
    return () => {
      isCancelled = true
    }
  }, [claimStatusFilter, currentUser])

  // Overview Metrics computation
  const metrics = useMemo(() => {
    const totalReports = reports.length
    const lostCount = reports.filter((r) => r.reportType === 'LOST').length
    const foundCount = reports.filter((r) => r.reportType === 'FOUND').length
    const claimedCount = reports.filter((r) => r.status === 'CLAIMED').length
    const returnedCount = reports.filter((r) => r.status === 'RETURNED').length
    const pendingClaimsCount = claims.filter((c) => c.claimStatus === 'PENDING').length

    return {
      totalReports,
      lostCount,
      foundCount,
      claimedCount,
      returnedCount,
      pendingClaimsCount,
    }
  }, [reports, claims])

  // Total unclaimed count for percentage computation in charts
  const totalUnclaimedLocation = useMemo(
    () => locationAnalytics.reduce((acc, curr) => acc + curr.itemCount, 0),
    [locationAnalytics]
  )

  const totalUnclaimedCategory = useMemo(
    () => categoryAnalytics.reduce((acc, curr) => acc + curr.itemCount, 0),
    [categoryAnalytics]
  )

  // Filtered reports for the Manage Reports table
  const filteredReports = useMemo(() => {
    return reports.filter((report) => {
      if (reportTypeFilter !== 'ALL' && report.reportType !== reportTypeFilter) {
        return false
      }
      if (reportStatusFilter !== 'ALL' && report.status !== reportStatusFilter) {
        return false
      }
      if (reportSearchQuery.trim()) {
        const query = reportSearchQuery.toLowerCase()
        const matchTitle = report.title.toLowerCase().includes(query)
        const matchCategory = report.category.toLowerCase().includes(query)
        const matchLocation = report.location.toLowerCase().includes(query)
        const matchReporter = (report.reporterName || '').toLowerCase().includes(query)
        if (!matchTitle && !matchCategory && !matchLocation && !matchReporter) {
          return false
        }
      }
      return true
    })
  }, [reports, reportTypeFilter, reportStatusFilter, reportSearchQuery])

  // Action: Mark Report as Returned
  const handleMarkReturned = async (reportId: number) => {
    if (!window.confirm(`Confirm marking item report #${reportId} as RETURNED? This confirms the verified owner has received the item.`)) {
      return
    }

    setProcessingReportId(reportId)
    setFeedback(null)

    try {
      const updated = await markReportAsReturned(reportId)
      // Update local report list
      setReports((prev) =>
        prev.map((r) => (r.id === reportId ? { ...r, status: updated.status } : r))
      )
      // Refresh analytics in background
      fetchUnclaimedByLocation().then(setLocationAnalytics).catch(() => {})
      fetchUnclaimedByCategory().then(setCategoryAnalytics).catch(() => {})
      fetchUnclaimedDetails().then(setUnclaimedDetails).catch(() => {})

      setFeedback({
        type: 'success',
        message: `Item report #${reportId} ("${updated.title}") has been marked as RETURNED.`,
      })
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to mark item as returned.'
      setFeedback({ type: 'error', message: msg })
    } finally {
      setProcessingReportId(null)
    }
  }

  // Action: Approve Claim
  const handleApprove = async (claimId: number, itemTitle: string) => {
    if (!window.confirm(`Approve claim #${claimId} for "${itemTitle}"? This will mark the item as CLAIMED.`)) {
      return
    }

    setProcessingClaimId(claimId)
    setFeedback(null)

    try {
      const updatedClaim = await approveClaim(claimId)
      // Update local claims
      if (claimStatusFilter === 'PENDING') {
        setClaims((prev) => prev.filter((c) => c.id !== claimId))
      } else {
        setClaims((prev) =>
          prev.map((c) => (c.id === claimId ? { ...c, claimStatus: updatedClaim.claimStatus } : c))
        )
      }

      // Update associated report status in local state to CLAIMED
      setReports((prev) =>
        prev.map((r) =>
          r.id === updatedClaim.itemReportId ? { ...r, status: 'CLAIMED' } : r
        )
      )

      // Refresh analytics
      fetchUnclaimedByLocation().then(setLocationAnalytics).catch(() => {})
      fetchUnclaimedByCategory().then(setCategoryAnalytics).catch(() => {})
      fetchUnclaimedDetails().then(setUnclaimedDetails).catch(() => {})

      setFeedback({
        type: 'success',
        message: `Claim #${claimId} approved! Associated item #${updatedClaim.itemReportId} is now marked as CLAIMED.`,
      })
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to approve claim.'
      setFeedback({ type: 'error', message: msg })
    } finally {
      setProcessingClaimId(null)
    }
  }

  // Action: Reject Claim
  const handleReject = async (claimId: number, itemTitle: string) => {
    if (!window.confirm(`Reject claim #${claimId} for "${itemTitle}"?`)) {
      return
    }

    setProcessingClaimId(claimId)
    setFeedback(null)

    try {
      const updatedClaim = await rejectClaim(claimId)
      // Update local claims
      if (claimStatusFilter === 'PENDING') {
        setClaims((prev) => prev.filter((c) => c.id !== claimId))
      } else {
        setClaims((prev) =>
          prev.map((c) => (c.id === claimId ? { ...c, claimStatus: updatedClaim.claimStatus } : c))
        )
      }

      setFeedback({
        type: 'success',
        message: `Claim #${claimId} has been rejected.`,
      })
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to reject claim.'
      setFeedback({ type: 'error', message: msg })
    } finally {
      setProcessingClaimId(null)
    }
  }

    // Action: Toggle Dual-Party Private Verification Review
  const handleToggleVerification = async (claimId: number) => {
    if (expandedClaimId === claimId) {
      setExpandedClaimId(null);
      return;
    }
    if (verificationData[claimId]) {
      setExpandedClaimId(claimId);
      return;
    }

    setLoadingVerificationId(claimId);
    setVerificationError((prev) => ({ ...prev, [claimId]: '' }));
    try {
      const details = await fetchClaimVerificationDetails(claimId);
      setVerificationData((prev) => ({ ...prev, [claimId]: details }));
      setExpandedClaimId(claimId);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load verification details.';
      setVerificationError((prev) => ({ ...prev, [claimId]: msg }));
      setFeedback({ type: 'error', message: msg });
    } finally {
      setLoadingVerificationId(null);
    }
  };

  // Action: Request More Info from Claimant
  const handleRequestMoreInfo = async (claimId: number, itemTitle: string) => {
    if (!window.confirm(`Request additional evidence or information for claim #${claimId} ("${itemTitle}")?`)) {
      return;
    }

    setProcessingClaimId(claimId);
    setFeedback(null);

    try {
      const updatedClaim = await requestMoreInfoForClaim(claimId);
      if (claimStatusFilter === 'PENDING') {
        setClaims((prev) => prev.filter((c) => c.id !== claimId));
      } else {
        setClaims((prev) =>
          prev.map((c) => (c.id === claimId ? { ...c, claimStatus: updatedClaim.claimStatus } : c))
        );
      }

      setFeedback({
        type: 'success',
        message: `Claim #${claimId} updated to MORE_INFO_REQUESTED. Student will see guidance to provide additional evidence.`,
      });
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to request more info.';
      setFeedback({ type: 'error', message: msg });
    } finally {
      setProcessingClaimId(null);
    }
  };

  // Helpers for badge styling
  const renderStatusBadge = (status: ItemStatus) => {
    switch (status) {
      case 'REPORTED':
        return <Badge variant="neutral">Reported</Badge>
      case 'CLAIMED':
        return <Badge variant="info">Claimed</Badge>
      case 'RETURNED':
        return <Badge variant="success">Returned</Badge>
      default:
        return <Badge>{status}</Badge>
    }
  }

  const renderClaimStatusBadge = (status: ClaimStatus) => {
    switch (status) {
      case 'PENDING':
        return <Badge variant="warning">Pending Review</Badge>
      case 'APPROVED':
        return <Badge variant="success">Approved</Badge>
      case 'REJECTED':
        return <Badge variant="danger">Rejected</Badge>
      default:
        return <Badge>{status}</Badge>
    }
  }

  // Access control guard
  if (!currentUser || currentUser.role !== 'ADMIN') {
    return (
      <main id="main-content" className="admin-dashboard">
        <Container size="md">
          <Card style={{ textAlign: 'center', padding: 'var(--space-8)' }}>
            <Badge variant="danger">403 Access Denied</Badge>
            <h1 style={{ fontFamily: 'var(--font-family-serif)', fontSize: 'var(--font-size-2xl)', color: 'var(--color-navy)', margin: 'var(--space-3) 0 var(--space-2)' }}>
              Administrative Privileges Required
            </h1>
            <p style={{ color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-sm)', maxWidth: '440px', margin: '0 auto var(--space-6)' }}>
              This portal is restricted to authorized campus staff. Please authenticate with an administrator account to access operations.
            </p>
            <div style={{ display: 'flex', gap: 'var(--space-3)', justifyContent: 'center' }}>
              {onNavigateLogin && (
                <Button variant="primary" onClick={onNavigateLogin}>
                  Sign in as Administrator
                </Button>
              )}
              <Button variant="outline" onClick={onNavigateHome}>
                Return to Student Catalog
              </Button>
            </div>
          </Card>
        </Container>
      </main>
    )
  }

  return (
    <main id="main-content" className="admin-dashboard">
      <Container size="xl">
        {/* Administrator Notice Banner */}
        <aside className="admin-demo-banner" role="region" aria-label="Administrator session notice">
          <div className="admin-demo-banner-content">
            <span className="admin-demo-badge">Admin Session</span>
            <span>
              Authenticated as <strong>{currentUser.name}</strong> ({currentUser.collegeUid}).
              Backend permissions are strictly enforced on all operations.
            </span>
          </div>
          <Button variant="ghost" size="sm" onClick={onNavigateHome}>
            Exit to Student Portal
          </Button>
        </aside>

        {/* Header */}
        <header className="admin-header">
          <div className="admin-header-title-group">
            <span className="admin-eyebrow">Operations &amp; Governance</span>
            <h1 className="admin-title">Campus Lost &amp; Found Administration</h1>
            <p className="admin-description">
              Review campus property intake, verify ownership claims, execute handover returns, and
              monitor unclaimed inventory.
            </p>
          </div>
          <div className="admin-header-actions">
            <Button variant="outline" size="sm" onClick={loadDashboardData} disabled={loading}>
              ↻ Refresh All
            </Button>
            <Button variant="primary" size="sm" onClick={onNavigateReport}>
              + Submit New Report
            </Button>
          </div>
        </header>

        {/* Global Feedback Banner */}
        {feedback && (
          <div
            className={`admin-feedback admin-feedback--${feedback.type}`}
            role="status"
            aria-live="polite"
          >
            <span>{feedback.message}</span>
            <button
              type="button"
              className="admin-feedback-close"
              onClick={() => setFeedback(null)}
              aria-label="Close notification"
            >
              ✕
            </button>
          </div>
        )}

        {/* Top-level KPI Metrics Grid */}
        <section className="admin-metrics-grid" aria-label="Portal summary metrics">
          <div className="admin-metric-card">
            <span className="admin-metric-label">Total Reports</span>
            <span className="admin-metric-value">{metrics.totalReports}</span>
            <span className="admin-metric-detail">
              {metrics.lostCount} Lost · {metrics.foundCount} Found
            </span>
          </div>

          <div
            className={`admin-metric-card ${
              metrics.pendingClaimsCount > 0 ? 'admin-metric-card--highlight' : ''
            }`}
          >
            <span className="admin-metric-label">Pending Claims</span>
            <span className="admin-metric-value">{metrics.pendingClaimsCount}</span>
            <span className="admin-metric-detail">Awaiting staff review</span>
          </div>

          <div className="admin-metric-card">
            <span className="admin-metric-label">Items Claimed</span>
            <span className="admin-metric-value">{metrics.claimedCount}</span>
            <span className="admin-metric-detail">Ready for return handover</span>
          </div>

          <div className="admin-metric-card">
            <span className="admin-metric-label">Items Returned</span>
            <span className="admin-metric-value">{metrics.returnedCount}</span>
            <span className="admin-metric-detail">Reconciled with owners</span>
          </div>
        </section>

        {/* Navigation Tabs */}
        <nav className="admin-tabs" aria-label="Admin sections">
          <button
            type="button"
            className={`admin-tab-btn ${activeTab === 'overview' ? 'admin-tab-btn--active' : ''}`}
            onClick={() => setActiveTab('overview')}
          >
            Overview &amp; Analytics
          </button>
          <button
            type="button"
            className={`admin-tab-btn ${activeTab === 'reports' ? 'admin-tab-btn--active' : ''}`}
            onClick={() => setActiveTab('reports')}
          >
            Manage Item Reports
            <span className="admin-tab-count">{reports.length}</span>
          </button>
          <button
            type="button"
            className={`admin-tab-btn ${activeTab === 'claims' ? 'admin-tab-btn--active' : ''}`}
            onClick={() => setActiveTab('claims')}
          >
            Review Student Claims
            {metrics.pendingClaimsCount > 0 && (
              <span className="admin-tab-count">{metrics.pendingClaimsCount}</span>
            )}
          </button>
        </nav>

        {/* Loading / Error States */}
        {loading && (
          <div className="admin-loading-state" role="status">
            <span>Loading admin data from server...</span>
          </div>
        )}

        {error && !loading && (
          <div className="admin-feedback admin-feedback--error" role="alert">
            <span>Error: {error}</span>
            <Button variant="outline" size="sm" onClick={loadDashboardData}>
              Try Again
            </Button>
          </div>
        )}

        {/* =========================================================================
            TAB 1: Overview & Analytics
            ========================================================================= */}
        {!loading && !error && activeTab === 'overview' && (
          <div className="admin-overview-view">
            <div className="admin-section-header">
              <h2 className="admin-section-title">Unclaimed Found Items Analytics</h2>
              <p className="admin-section-subtitle">
                Aggregate insights generated via Spring JDBC queries for custody tracking and
                inventory audits.
              </p>
            </div>

            {/* Analytics Breakdown Grid */}
            <div className="admin-analytics-grid">
              {/* Unclaimed by Location */}
              <div className="admin-analytics-panel">
                <h3 className="admin-analytics-panel-title">Unclaimed Items by Campus Location</h3>
                <p className="admin-analytics-panel-desc">
                  Distribution of found items awaiting claimant identification across campus facilities.
                </p>
                {locationAnalytics.length === 0 ? (
                  <p className="admin-empty-desc">No unclaimed found items recorded.</p>
                ) : (
                  <div className="admin-bar-list">
                    {locationAnalytics.map((item) => {
                      const pct =
                        totalUnclaimedLocation > 0
                          ? Math.round((item.itemCount / totalUnclaimedLocation) * 100)
                          : 0
                      return (
                        <div key={item.location} className="admin-bar-item">
                          <div className="admin-bar-header">
                            <span className="admin-bar-name">{item.location}</span>
                            <span className="admin-bar-value">
                              {item.itemCount} <span className="admin-bar-pct">({pct}%)</span>
                            </span>
                          </div>
                          <div className="admin-bar-track">
                            <div
                              className="admin-bar-fill admin-bar-fill--location"
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                        </div>
                      )
                    })}
                  </div>
                )}
              </div>

              {/* Unclaimed by Category */}
              <div className="admin-analytics-panel">
                <h3 className="admin-analytics-panel-title">Unclaimed Items by Category</h3>
                <p className="admin-analytics-panel-desc">
                  Breakdown by item classification to evaluate high-frequency lost property groups.
                </p>
                {categoryAnalytics.length === 0 ? (
                  <p className="admin-empty-desc">No unclaimed found items recorded.</p>
                ) : (
                  <div className="admin-bar-list">
                    {categoryAnalytics.map((item) => {
                      const pct =
                        totalUnclaimedCategory > 0
                          ? Math.round((item.itemCount / totalUnclaimedCategory) * 100)
                          : 0
                      return (
                        <div key={item.category} className="admin-bar-item">
                          <div className="admin-bar-header">
                            <span className="admin-bar-name">{item.category}</span>
                            <span className="admin-bar-value">
                              {item.itemCount} <span className="admin-bar-pct">({pct}%)</span>
                            </span>
                          </div>
                          <div className="admin-bar-track">
                            <div
                              className="admin-bar-fill admin-bar-fill--category"
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                        </div>
                      )
                    })}
                  </div>
                )}
              </div>
            </div>

            {/* Unclaimed Items Detailed Inventory Table */}
            <div className="admin-section-header" style={{ marginTop: 'var(--space-8)' }}>
              <h3 className="admin-section-title">Detailed Unclaimed Found Inventory</h3>
              <p className="admin-section-subtitle">
                Individual found item records held in custody without active approved claims.
              </p>
            </div>

            <div className="admin-table-container">
              <table className="admin-table" aria-label="Detailed unclaimed items inventory">
                <thead>
                  <tr>
                    <th>Report #</th>
                    <th>Item Title</th>
                    <th>Category</th>
                    <th>Found Location</th>
                    <th>Date Reported</th>
                    <th>Finder / Reporter</th>
                  </tr>
                </thead>
                <tbody>
                  {unclaimedDetails.length === 0 ? (
                    <tr>
                      <td colSpan={6} style={{ textAlign: 'center', padding: 'var(--space-6)' }}>
                        No unclaimed found items currently in custody.
                      </td>
                    </tr>
                  ) : (
                    unclaimedDetails.map((item) => (
                      <tr key={item.reportId}>
                        <td>#{item.reportId}</td>
                        <td className="admin-table-item-title">{item.title}</td>
                        <td>
                          <Badge variant="neutral">{item.category}</Badge>
                        </td>
                        <td>{item.location}</td>
                        <td>{item.reportDate}</td>
                        <td>
                          {item.reporterName} <span style={{ color: 'var(--color-text-muted)' }}>(#{item.reporterId})</span>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* =========================================================================
            TAB 2: Manage Item Reports
            ========================================================================= */}
        {!loading && !error && activeTab === 'reports' && (
          <div className="admin-reports-view">
            {/* Filter Bar */}
            <div className="admin-filter-bar">
              <div className="admin-filter-group">
                <label htmlFor="admin-filter-type" className="admin-filter-label">
                  Type
                </label>
                <select
                  id="admin-filter-type"
                  className="admin-select"
                  value={reportTypeFilter}
                  onChange={(e) => setReportTypeFilter(e.target.value as ReportType | 'ALL')}
                >
                  <option value="ALL">All Types</option>
                  <option value="LOST">Lost</option>
                  <option value="FOUND">Found</option>
                </select>

                <label htmlFor="admin-filter-status" className="admin-filter-label">
                  Status
                </label>
                <select
                  id="admin-filter-status"
                  className="admin-select"
                  value={reportStatusFilter}
                  onChange={(e) => setReportStatusFilter(e.target.value as ItemStatus | 'ALL')}
                >
                  <option value="ALL">All Statuses</option>
                  <option value="REPORTED">Reported (Unclaimed)</option>
                  <option value="CLAIMED">Claimed (Pending Return)</option>
                  <option value="RETURNED">Returned (Completed)</option>
                </select>
              </div>

              <div className="admin-filter-group">
                <input
                  type="text"
                  className="admin-search-input"
                  placeholder="Filter by title, category, location..."
                  value={reportSearchQuery}
                  onChange={(e) => setReportSearchQuery(e.target.value)}
                  aria-label="Filter item reports"
                />
                {(reportTypeFilter !== 'ALL' ||
                  reportStatusFilter !== 'ALL' ||
                  reportSearchQuery) && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      setReportTypeFilter('ALL')
                      setReportStatusFilter('ALL')
                      setReportSearchQuery('')
                    }}
                  >
                    Reset Filters
                  </Button>
                )}
              </div>
            </div>

            {/* Reports Table */}
            <div className="admin-table-container">
              <table className="admin-table" aria-label="Campus item reports list">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Item</th>
                    <th>Type</th>
                    <th>Category</th>
                    <th>Location</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th>Reporter</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredReports.length === 0 ? (
                    <tr>
                      <td colSpan={9} style={{ textAlign: 'center', padding: 'var(--space-8)' }}>
                        <div className="admin-empty-state" style={{ border: 'none', padding: 0 }}>
                          <p className="admin-empty-title">No matching reports found</p>
                          <p className="admin-empty-desc">
                            Try adjusting your filters or search keywords.
                          </p>
                        </div>
                      </td>
                    </tr>
                  ) : (
                    filteredReports.map((report) => {
                      const isClaimed = report.status === 'CLAIMED'
                      const isReturned = report.status === 'RETURNED'
                      const isProcessing = processingReportId === report.id

                      return (
                        <tr key={report.id}>
                          <td>#{report.id}</td>
                          <td>
                            <div className="admin-table-item-title">{report.title}</div>
                            {report.description && (
                              <div className="admin-table-item-desc" title={report.description}>
                                {report.description}
                              </div>
                            )}
                          </td>
                          <td>
                            <Badge variant={report.reportType === 'FOUND' ? 'info' : 'warning'}>
                              {report.reportType}
                            </Badge>
                          </td>
                          <td>{report.category}</td>
                          <td>{report.location}</td>
                          <td>{report.reportDate}</td>
                          <td>{renderStatusBadge(report.status)}</td>
                          <td>
                            {report.reporterName || `User #${report.reporterId}`}
                          </td>
                          <td className="admin-table-actions">
                            {isReturned ? (
                              <span style={{ color: 'var(--color-status-success-text)', fontSize: 'var(--font-size-xs)', fontWeight: 'bold' }}>
                                ✓ Returned
                              </span>
                            ) : isClaimed ? (
                              <Button
                                variant="primary"
                                size="sm"
                                disabled={isProcessing}
                                onClick={() => handleMarkReturned(report.id)}
                                title="Click to verify item handover and mark as RETURNED"
                              >
                                {isProcessing ? 'Updating...' : 'Mark as Returned'}
                              </Button>
                            ) : (
                              <span
                                style={{
                                  color: 'var(--color-text-muted)',
                                  fontSize: 'var(--font-size-xs)',
                                  fontStyle: 'italic',
                                }}
                                title="An item report can only be marked as RETURNED once an approved claim puts it in CLAIMED status."
                              >
                                Awaiting claim
                              </span>
                            )}
                          </td>
                        </tr>
                      )
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* =========================================================================
            TAB 3: Review Student Claims
            ========================================================================= */}
        {!loading && !error && activeTab === 'claims' && (
          <div className="admin-claims-view">
            {/* Filter Bar */}
            <div className="admin-filter-bar">
              <div className="admin-filter-group">
                <label htmlFor="admin-claim-filter" className="admin-filter-label">
                  Claim Status
                </label>
                <select
                  id="admin-claim-filter"
                  className="admin-select"
                  value={claimStatusFilter}
                  onChange={(e) => setClaimStatusFilter(e.target.value as ClaimStatus)}
                >
                  <option value="PENDING">Pending Review (Action Required)</option>
                  <option value="APPROVED">Approved</option>
                  <option value="REJECTED">Rejected</option>
                  <option value="MORE_INFO_REQUESTED">More Info Requested</option>
                </select>
              </div>

              <div className="admin-filter-group">
                <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-text-secondary)' }}>
                  Showing {claims.length} {claimStatusFilter.toLowerCase()} claim(s)
                </span>
              </div>
            </div>

            {/* Claims Queue */}
            {claims.length === 0 ? (
              <div className="admin-empty-state">
                <h3 className="admin-empty-title">No {claimStatusFilter.toLowerCase()} claims found</h3>
                <p className="admin-empty-desc">
                  {claimStatusFilter === 'PENDING'
                    ? 'All student claims have been reviewed. New claims submitted by students will appear here.'
                    : `There are currently no claims marked as ${claimStatusFilter}.`}
                </p>
              </div>
            ) : (
              <div className="admin-claims-list">
                {claims.map((claim) => {
                  const isProcessing = processingClaimId === claim.id
                  const isPending = claim.claimStatus === 'PENDING'

                  return (
                    <article key={claim.id} className="admin-claim-card">
                      <div className="admin-claim-card-header">
                        <div className="admin-claim-card-title-group">
                          <span className="admin-claim-id">Claim #{claim.id}</span>
                          <h3 className="admin-claim-item-name">
                            {claim.itemTitle || `Item Report #${claim.itemReportId}`}
                          </h3>
                        </div>
                        {renderClaimStatusBadge(claim.claimStatus)}
                      </div>

                      <div className="admin-claim-grid">
                        <div className="admin-claim-proof-box">
                          <div className="admin-claim-proof-label">Claimant Verification &amp; Ownership Proof</div>
                          <p className="admin-claim-proof-text">"{claim.description}"</p>
                        </div>

                        <div className="admin-claim-meta">
                          <div className="admin-claim-meta-row">
                            <span style={{ color: 'var(--color-text-muted)' }}>Student Claimant:</span>
                            <span style={{ fontWeight: 'var(--font-weight-medium)', color: 'var(--color-navy)' }}>
                              {claim.claimantName || `Student #${claim.claimantId}`}
                            </span>
                          </div>
                          <div className="admin-claim-meta-row">
                            <span style={{ color: 'var(--color-text-muted)' }}>Student ID:</span>
                            <span>#{claim.claimantId}</span>
                          </div>
                          <div className="admin-claim-meta-row">
                            <span style={{ color: 'var(--color-text-muted)' }}>Item Report ID:</span>
                            <span>#{claim.itemReportId}</span>
                          </div>
                          <div className="admin-claim-meta-row">
                            <span style={{ color: 'var(--color-text-muted)' }}>Claim Date:</span>
                            <span>{claim.claimDate}</span>
                          </div>
                        </div>
                      </div>

                      {/* Review Private Evidence Toggle Button */}
                      <div className="admin-claim-evidence-toggle">
                        <Button
                          variant="outline"
                          size="sm"
                          disabled={loadingVerificationId === claim.id}
                          onClick={() => handleToggleVerification(claim.id)}
                        >
                          {loadingVerificationId === claim.id
                            ? 'Loading Evidence...'
                            : expandedClaimId === claim.id
                            ? 'Hide Private Evidence ▲'
                            : '🔍 Review Private Evidence ▼'}
                        </Button>
                        {verificationError[claim.id] && (
                          <div className="admin-claim-inline-error" role="alert">
                            <span>⚠️ {verificationError[claim.id]}</span>
                          </div>
                        )}
                      </div>

                      {/* Expanded Dual-Party Evidence Panel */}
                      {expandedClaimId === claim.id && verificationData[claim.id] && (
                        <div className="admin-dual-evidence-panel">
                          {/* Finder Panel */}
                          <div className="admin-evidence-party admin-evidence-party--finder">
                            <div className="admin-evidence-party-header">
                              <span className="admin-evidence-tag">Finder / Reporter Details</span>
                              <span className="admin-evidence-party-name">
                                {verificationData[claim.id].reporterName || `Student #${verificationData[claim.id].reporterId}`}
                              </span>
                            </div>
                            <div className="admin-evidence-party-body">
                              <div className="admin-evidence-item">
                                <span className="admin-evidence-item-label">Public Description:</span>
                                <p className="admin-evidence-item-text">{verificationData[claim.id].itemPublicDescription}</p>
                              </div>
                              <div className="admin-evidence-item admin-evidence-item--highlight">
                                <span className="admin-evidence-item-label">🔒 Private Distinguishing Details:</span>
                                <p className="admin-evidence-item-text">
                                  {verificationData[claim.id].reporterPrivateDescription || 'No private distinguishing description provided.'}
                                </p>
                              </div>
                              {verificationData[claim.id].reporterEvidenceId ? (
                                <PrivateEvidenceImage
                                  evidenceId={verificationData[claim.id].reporterEvidenceId!}
                                  altText="Finder's private item photograph"
                                  label="Finder's Private Photograph"
                                />
                              ) : (
                                <div className="admin-no-photo-box">
                                  <span>No private photograph attached by finder</span>
                                </div>
                              )}
                            </div>
                          </div>

                          {/* Claimant Panel */}
                          <div className="admin-evidence-party admin-evidence-party--claimant">
                            <div className="admin-evidence-party-header">
                              <span className="admin-evidence-tag">Claimant Verification</span>
                              <span className="admin-evidence-party-name">
                                {verificationData[claim.id].claimantName || `Student #${verificationData[claim.id].claimantId}`}
                              </span>
                            </div>
                            <div className="admin-evidence-party-body">
                              <div className="admin-evidence-item">
                                <span className="admin-evidence-item-label">Ownership Statement:</span>
                                <p className="admin-evidence-item-text">{verificationData[claim.id].claimantDescription}</p>
                              </div>
                              <div className="admin-evidence-item admin-evidence-item--highlight">
                                <span className="admin-evidence-item-label">🔒 Claimed Identifying Features:</span>
                                <p className="admin-evidence-item-text">
                                  {verificationData[claim.id].claimantIdentifyingFeatures || 'No specific features specified.'}
                                </p>
                              </div>
                              {verificationData[claim.id].claimantEvidenceId ? (
                                <PrivateEvidenceImage
                                  evidenceId={verificationData[claim.id].claimantEvidenceId!}
                                  altText="Claimant's submitted photograph"
                                  label="Claimant's Photo Evidence"
                                />
                              ) : (
                                <div className="admin-no-photo-box">
                                  <span>No photo evidence attached by claimant</span>
                                </div>
                              )}
                            </div>
                          </div>
                        </div>
                      )}

                      {/* Action buttons on actionable claims */}
                      {(isPending || claim.claimStatus === 'MORE_INFO_REQUESTED') && (
                        <div className="admin-claim-card-footer">
                          {isPending && (
                            <Button
                              variant="outline"
                              size="sm"
                              disabled={isProcessing}
                              onClick={() => handleRequestMoreInfo(claim.id, claim.itemTitle || `Item #${claim.itemReportId}`)}
                            >
                              {isProcessing ? 'Processing...' : 'Request More Info'}
                            </Button>
                          )}
                          <Button
                            variant="danger"
                            size="sm"
                            disabled={isProcessing}
                            onClick={() => handleReject(claim.id, claim.itemTitle || `Item #${claim.itemReportId}`)}
                          >
                            {isProcessing ? 'Processing...' : 'Reject Claim'}
                          </Button>
                          <Button
                            variant="primary"
                            size="sm"
                            disabled={isProcessing}
                            onClick={() => handleApprove(claim.id, claim.itemTitle || `Item #${claim.itemReportId}`)}
                          >
                            {isProcessing ? 'Processing...' : 'Approve Claim'}
                          </Button>
                        </div>
                      )}
                    </article>
                  )
                })}
              </div>
            )}
          </div>
        )}
      </Container>
    </main>
  )
}

