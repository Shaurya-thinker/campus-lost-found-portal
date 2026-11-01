import type { FormEvent } from 'react'
import { Container, Button } from '../ui'
import './HeroSection.css'

export interface HeroSectionProps {
  searchQuery: string
  onSearchChange: (value: string) => void
  onSearchSubmit: (e: FormEvent) => void
  onOpenReportForm: (type: 'LOST' | 'FOUND') => void
}

export function HeroSection({
  searchQuery,
  onSearchChange,
  onSearchSubmit,
  onOpenReportForm,
}: HeroSectionProps) {
  return (
    <section className="hero-section" aria-labelledby="hero-title">
      <Container size="xl">
        <div className="hero-content">
          <span className="hero-eyebrow">CAMPUS LOST PROPERTY</span>
          <h1 id="hero-title" className="hero-headline">
            Lost something on campus?
          </h1>
          <p className="hero-subtext">
            Search cataloged belongings reported across campus facilities, or check turned-in property to help reunite items with their rightful owners.
          </p>

          <form className="hero-search-form" onSubmit={onSearchSubmit} role="search">
            <div className="hero-search-input-wrapper">
              <label htmlFor="hero-search-input" className="visually-hidden" style={{ position: 'absolute', width: '1px', height: '1px', padding: 0, margin: '-1px', overflow: 'hidden', clip: 'rect(0, 0, 0, 0)', whiteSpace: 'nowrap', border: 0 }}>
                Search items by keyword or location
              </label>
              <input
                id="hero-search-input"
                type="search"
                className="hero-search-input"
                placeholder="Search by item name, location, or keyword..."
                value={searchQuery}
                onChange={(e) => onSearchChange(e.target.value)}
              />
            </div>
            <Button type="submit" variant="primary" size="lg">
              Search items
            </Button>
          </form>

          <div className="hero-entry-points">
            <span className="hero-entry-label">File a report:</span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onOpenReportForm('LOST')}
            >
              I lost something
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onOpenReportForm('FOUND')}
            >
              I found something
            </Button>
          </div>
        </div>
      </Container>
    </section>
  )
}
