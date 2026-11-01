import { Container } from '../ui'
import './SiteFooter.css'

export function SiteFooter() {
  return (
    <footer className="site-footer" role="contentinfo">
      <Container size="xl">
        <div className="site-footer-inner">
          <div>
            <span className="site-footer-brand">Campus Lost &amp; Found Portal</span>
            <p className="site-footer-desc">
              A university utility service facilitating the recording, verification, and return of personal belongings lost or found within campus facilities.
            </p>
          </div>

          <div className="site-footer-meta">
            <span className="site-footer-copy">
              Property Desk Reference Utility
            </span>
            <span className="site-footer-copy">
              Campus Lost &amp; Found
            </span>
          </div>
        </div>
      </Container>
    </footer>
  )
}

