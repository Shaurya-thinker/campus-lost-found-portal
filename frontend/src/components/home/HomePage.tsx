import { useState, useEffect, useCallback, type FormEvent } from 'react'
import type { ItemReport, ReportType } from '../../types/report'
import { fetchReports } from '../../services/reportsApi'
import { HeroSection } from './HeroSection'
import { BrowseSection } from './BrowseSection'
import { HowItWorksSection } from './HowItWorksSection'

export interface HomePageProps {
  onOpenReportForm: (type: 'LOST' | 'FOUND') => void
  onViewDetails?: (item: ItemReport) => void
  refreshTrigger?: number
}

export function HomePage({ onOpenReportForm, onViewDetails, refreshTrigger = 0 }: HomePageProps) {
  const [reports, setReports] = useState<ItemReport[]>([])
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedType, setSelectedType] = useState<ReportType | 'ALL'>('ALL')
  const [searchQuery, setSearchQuery] = useState<string>('')

  const loadReports = useCallback(async (type: ReportType | 'ALL') => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchReports(type)
      setReports(data)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to connect to registry'
      setError(message)
      setReports([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let isCurrent = true

    async function execute() {
      setLoading(true)
      setError(null)
      try {
        const data = await fetchReports(selectedType)
        if (isCurrent) {
          setReports(data)
        }
      } catch (err) {
        if (isCurrent) {
          const message = err instanceof Error ? err.message : 'Unable to connect to registry'
          setError(message)
          setReports([])
        }
      } finally {
        if (isCurrent) {
          setLoading(false)
        }
      }
    }

    execute()

    return () => {
      isCurrent = false
    }
  }, [selectedType, refreshTrigger])

  const handleSearchSubmit = (e: FormEvent) => {
    e.preventDefault()
    const browseElement = document.getElementById('browse')
    if (browseElement) {
      browseElement.scrollIntoView({ behavior: 'smooth' })
    }
  }

  const handleClearSearch = () => {
    setSearchQuery('')
  }

  return (
    <main id="main-content">
      <HeroSection
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        onSearchSubmit={handleSearchSubmit}
        onOpenReportForm={onOpenReportForm}
      />

      <BrowseSection
        reports={reports}
        loading={loading}
        error={error}
        selectedType={selectedType}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        onFilterChange={setSelectedType}
        onRetry={() => loadReports(selectedType)}
        onClearSearch={handleClearSearch}
        onViewDetails={onViewDetails}
      />

      <HowItWorksSection />
    </main>
  )
}
