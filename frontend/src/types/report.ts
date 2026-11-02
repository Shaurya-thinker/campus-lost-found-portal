/**
 * Report types and contracts matching the Spring Boot ItemReportResponse and ItemReportCreateRequest DTOs
 */

export type ReportType = 'LOST' | 'FOUND'
export type ItemStatus = 'REPORTED' | 'CLAIMED' | 'RETURNED'

export interface ItemReport {
  id: number
  title: string
  description?: string
  category: string
  location: string
  reportType: ReportType
  reportDate: string // ISO date "YYYY-MM-DD"
  status: ItemStatus
  reporterId?: number
  reporterName?: string
}

export interface CreateReportPayload {
  title: string
  description: string
  privateDescription?: string
  category: string
  location: string
  reportType: ReportType
  reporterId?: number
}

export interface ApiErrorResponse {
  timestamp?: string
  status?: number
  error?: string
  message?: string
  fieldErrors?: Record<string, string>
}
