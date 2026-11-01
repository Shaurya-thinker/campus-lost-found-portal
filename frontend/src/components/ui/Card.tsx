import type { ReactNode, HTMLAttributes } from 'react'
import './Card.css'

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'subdued' | 'strong'
  className?: string
  children: ReactNode
}

export function Card({
  variant = 'default',
  className = '',
  children,
  ...rest
}: CardProps) {
  const classes = [
    'ui-card',
    variant === 'subdued' ? 'ui-card--subdued' : '',
    variant === 'strong' ? 'ui-card--bordered-strong' : '',
    className,
  ].filter(Boolean).join(' ')

  return (
    <div className={classes} {...rest}>
      {children}
    </div>
  )
}

