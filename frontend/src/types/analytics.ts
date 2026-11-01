/**
 * Analytics types matching Spring Boot AnalyticsController DTOs
 */

export interface LocationCountReport {
  location: string
  itemCount: number
}

export interface CategoryCountReport {
  category: string
  itemCount: number
}

export interface UnclaimedItemDetail {
  reportId: number
  title: string
  category: string
  location: string
  reportDate: string
  reporterId: number
  reporterName: string
}

