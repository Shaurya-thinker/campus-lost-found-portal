import { Container, Button } from '../ui'
import './HeroSection.css'

export interface HeroSectionProps {
  onOpenReportForm: (type: 'LOST' | 'FOUND') => void
}

export function HeroSection({ onOpenReportForm }: HeroSectionProps) {
  return (
    <section className="hero-section" aria-labelledby="hero-title">
      <Container size="xl">
        <div className="hero-content">
          <span className="hero-eyebrow">CAMPUS LOST PROPERTY</span>
          <h1 id="hero-title" className="hero-headline">
            Lost something on campus?
          </h1>
          <p className="hero-subtext">
            Search cataloged belongings reported across campus facilities in the catalog below, or check turned-in property to help reunite items with their rightful owners.
          </p>

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
