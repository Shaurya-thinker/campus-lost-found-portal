import { Container } from '../ui'
import './HowItWorksSection.css'

export function HowItWorksSection() {
  return (
    <section id="how-it-works" className="how-it-works-section" aria-labelledby="how-it-works-heading">
      <Container size="xl">
        <div className="how-it-works-header">
          <span className="how-it-works-eyebrow">CAMPUS PROCEDURES</span>
          <h2 id="how-it-works-heading">How the portal works</h2>
          <p>
            The campus lost property workflow ensures personal belongings are logged accurately and verified before return.
          </p>
        </div>

        <div className="steps-grid">
          <div className="step-card">
            <span className="step-number">STEP 01</span>
            <h3 className="step-title">Browse reports or search by keyword</h3>
            <p className="step-desc">
              Search by item category, campus building, or description to identify whether your missing property has been logged or handed in to an administrative desk.
            </p>
          </div>

          <div className="step-card">
            <span className="step-number">STEP 02</span>
            <h3 className="step-title">Report a lost or found belonging</h3>
            <p className="step-desc">
              Log detailed information regarding a missing item or register an item you discovered on university grounds to alert campus security and property desk staff.
            </p>
          </div>

          <div className="step-card">
            <span className="step-number">STEP 03</span>
            <h3 className="step-title">Submit a claim and verify ownership</h3>
            <p className="step-desc">
              Submit proof of ownership with unique identifying features. Once administrative staff approve the claim, the item is marked claimed and prepared for physical collection.
            </p>
          </div>
        </div>

        <div id="report-info" className="report-notice-card" tabIndex={-1}>
          <h3 className="report-notice-title">Property Reporting &amp; Claim Submissions</h3>
          <p className="report-notice-text">
            Online item submission forms and ownership verification flows are being integrated with the campus backend services. For urgent inquiries or items containing personal identification, please contact your building's facilities reception directly.
          </p>
        </div>
      </Container>
    </section>
  )
}

