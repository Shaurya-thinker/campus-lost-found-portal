import type { ItemReport } from '../../types/report'
import { Card, Badge, Button } from '../ui'
import './ReportCard.css'

export interface ReportCardProps {
  report: ItemReport
  onViewDetails?: (item: ItemReport) => void
}

export function ReportCard({ report, onViewDetails }: ReportCardProps) {
  const typeBadgeVariant = report.reportType === 'FOUND' ? 'info' : 'warning'
  
  let statusBadgeVariant: 'neutral' | 'info' | 'success' | 'warning' = 'neutral'
  if (report.status === 'CLAIMED') {
    statusBadgeVariant = 'warning'
  } else if (report.status === 'RETURNED') {
    statusBadgeVariant = 'success'
  }

  const isClaimEligible = report.reportType === 'FOUND' && report.status === 'REPORTED'

  return (
    <Card className="report-card">
      <div>
        <div className="report-card-header">
          <div className="report-card-badges">
            <Badge variant={typeBadgeVariant}>{report.reportType}</Badge>
            <Badge variant={statusBadgeVariant}>{report.status}</Badge>
          </div>
          <span className="report-card-date">{report.reportDate}</span>
        </div>

        <h3 className="report-card-title">{report.title}</h3>
        {report.description && (
          <p className="report-card-desc">{report.description}</p>
        )}
      </div>

      <div className="report-card-meta">
        <div className="report-card-meta-row">
          <span className="report-card-meta-label">Category:</span>
          <span className="report-card-meta-val">{report.category}</span>
        </div>
        <div className="report-card-meta-row">
          <span className="report-card-meta-label">Location:</span>
          <span className="report-card-meta-val">{report.location}</span>
        </div>
        {report.reporterName && (
          <div className="report-card-meta-row">
            <span className="report-card-meta-label">Reported by:</span>
            <span className="report-card-meta-val">{report.reporterName}</span>
          </div>
        )}

        {onViewDetails && (
          <div style={{ marginTop: 'var(--space-3)', display: 'flex', justifyContent: 'flex-end' }}>
            <Button
              variant={isClaimEligible ? 'primary' : 'outline'}
              size="sm"
              onClick={() => onViewDetails(report)}
            >
              {isClaimEligible ? 'View details & claim' : 'View details'}
            </Button>
          </div>
        )}
      </div>
    </Card>
  )
}
