import { useState, useEffect, type ChangeEvent, type KeyboardEvent } from 'react'
import type { ItemReport, ReportType } from '../../types/report'
import { Container, Button, Card } from '../ui'
import { ReportCard } from './ReportCard'
import './BrowseSection.css'

export interface BrowseSectionProps {
  reports: ItemReport[]
  loading: boolean
  error: string | null
  selectedType: ReportType | 'ALL'
  searchQuery: string
  onSearchChange: (value: string) => void
  onFilterChange: (type: ReportType | 'ALL') => void
  onRetry: () => void
  onClearSearch: () => void
  onViewDetails?: (item: ItemReport) => void
}

export function BrowseSection({
  reports,
  loading,
  error,
  selectedType,
  searchQuery,
  onSearchChange,
  onFilterChange,
  onRetry,
  onClearSearch,
  onViewDetails,
}: BrowseSectionProps) {
  // Local state for immediate typing responsiveness with 250ms debounce
  const [localQuery, setLocalQuery] = useState(searchQuery)

  useEffect(() => {
    setLocalQuery(searchQuery)
  }, [searchQuery])

  useEffect(() => {
    const handler = setTimeout(() => {
      if (localQuery !== searchQuery) {
        onSearchChange(localQuery)
      }
    }, 250)

    return () => clearTimeout(handler)
  }, [localQuery, searchQuery, onSearchChange])

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    setLocalQuery(e.target.value)
  }

  const handleInputKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      onSearchChange(localQuery)
    }
  }

  const handleClear = () => {
    setLocalQuery('')
    onSearchChange('')
    onClearSearch()
  }

  const query = searchQuery.trim().toLowerCase()

  // Keyword search across item title, category, campus location, and description
  const filteredReports = reports.filter((item) => {
    if (!query) return true
    const titleMatch = item.title?.toLowerCase().includes(query)
    const descMatch = item.description?.toLowerCase().includes(query)
    const catMatch = item.category?.toLowerCase().includes(query)
    const locMatch = item.location?.toLowerCase().includes(query)
    return Boolean(titleMatch || descMatch || catMatch || locMatch)
  })

  return (
    <section id="browse" className="browse-section" aria-labelledby="browse-heading">
      <Container size="xl">
        <div className="browse-header">
          <div className="browse-title-group">
            <h2 id="browse-heading">Recently reported items</h2>
            <p className="browse-subtitle">
              Official catalog of property logged by students and campus facility staff.
            </p>
          </div>

          <div className="browse-filters" role="group" aria-label="Filter items by report type">
            <button
              type="button"
              className={`browse-filter-btn ${selectedType === 'ALL' ? 'browse-filter-btn--active' : ''}`}
              onClick={() => onFilterChange('ALL')}
              aria-pressed={selectedType === 'ALL'}
            >
              ALL ITEMS
            </button>
            <button
              type="button"
              className={`browse-filter-btn ${selectedType === 'LOST' ? 'browse-filter-btn--active' : ''}`}
              onClick={() => onFilterChange('LOST')}
              aria-pressed={selectedType === 'LOST'}
            >
              LOST
            </button>
            <button
              type="button"
              className={`browse-filter-btn ${selectedType === 'FOUND' ? 'browse-filter-btn--active' : ''}`}
              onClick={() => onFilterChange('FOUND')}
              aria-pressed={selectedType === 'FOUND'}
            >
              FOUND
            </button>
          </div>
        </div>

        {/* Catalog Search Bar */}
        <div className="browse-search-bar" role="search">
          <label htmlFor="catalog-search-input" className="browse-search-label">
            Search items by title, category, or campus location
          </label>
          <div className="browse-search-field-wrapper">
            <span className="browse-search-icon" aria-hidden="true">
              🔍
            </span>
            <input
              id="catalog-search-input"
              type="search"
              className="browse-search-input"
              placeholder="Search by title, category, or location (e.g. Laptop, Calculator, Library)..."
              value={localQuery}
              onChange={handleInputChange}
              onKeyDown={handleInputKeyDown}
              aria-label="Search catalog items by title, category, or campus location"
            />
            {localQuery && (
              <button
                type="button"
                className="browse-search-clear-btn"
                onClick={handleClear}
                aria-label="Clear search input"
                title="Clear search"
              >
                ✕
              </button>
            )}
          </div>
        </div>

        {/* Real search / filter count (only shown when loaded from real API) */}
        {!loading && !error && (
          <div className="browse-count-bar">
            <span>
              Showing {filteredReports.length} {filteredReports.length === 1 ? 'record' : 'records'}
              {selectedType !== 'ALL' ? ` (${selectedType})` : ''}
              {query ? ` matching "${searchQuery}"` : ''}
            </span>
            {query && (
              <Button variant="ghost" size="sm" onClick={handleClear}>
                Clear search filter
              </Button>
            )}
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <Card className="browse-state-card" variant="subdued">
            <h3 className="browse-state-title">Checking campus registry...</h3>
            <p className="browse-state-desc">
              Retrieving recently cataloged lost and found reports from the backend service.
            </p>
          </Card>
        )}

        {/* Error State */}
        {!loading && error && (
          <Card className="browse-state-card" variant="subdued">
            <h3 className="browse-state-title">Registry currently unreachable</h3>
            <p className="browse-state-desc">
              {error}
            </p>
            <p className="browse-state-desc" style={{ fontSize: 'var(--font-size-xs)' }}>
              If the backend server is offline, start the Spring Boot application on port 8080 and refresh.
            </p>
            <Button variant="outline" size="sm" onClick={onRetry}>
              Retry connection
            </Button>
          </Card>
        )}

        {/* Empty State */}
        {!loading && !error && filteredReports.length === 0 && (
          <Card className="browse-state-card" variant="default">
            <h3 className="browse-state-title">No cataloged items found</h3>
            <p className="browse-state-desc">
              {query
                ? `No reported items matched "${searchQuery}" under ${selectedType === 'ALL' ? 'all categories' : `${selectedType} reports`}. Try searching by a different title, category, or campus location.`
                : `There are currently no active ${selectedType !== 'ALL' ? selectedType : ''} property records logged in this category.`}
            </p>
            <p className="browse-state-desc" style={{ fontSize: 'var(--font-size-xs)' }}>
              If you misplaced or discovered an item on campus, you can file a report with the property desk below.
            </p>
            {query && (
              <Button variant="outline" size="sm" onClick={handleClear}>
                Clear search filter
              </Button>
            )}
          </Card>
        )}

        {/* Real Results Grid */}
        {!loading && !error && filteredReports.length > 0 && (
          <div className="browse-grid">
            {filteredReports.map((report) => (
              <ReportCard
                key={report.id}
                report={report}
                onViewDetails={onViewDetails}
              />
            ))}
          </div>
        )}
      </Container>
    </section>
  )
}
