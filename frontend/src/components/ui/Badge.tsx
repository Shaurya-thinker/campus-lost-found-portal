import type { ReactNode } from 'react'
import './Badge.css'

export type BadgeVariant = 'neutral' | 'info' | 'success' | 'warning' | 'danger'

export interface BadgeProps {
  variant?: BadgeVariant
  className?: string
  children: ReactNode
}

export function Badge({
  variant = 'neutral',
  className = '',
  children,
}: BadgeProps) {
  const classes = ['ui-badge', `ui-badge--${variant}`, className].filter(Boolean).join(' ')

  return <span className={classes}>{children}</span>
}

