import type { ReactNode, ElementType, ComponentPropsWithoutRef } from 'react'
import './Container.css'

export type ContainerSize = 'sm' | 'md' | 'lg' | 'xl' | 'full'

export interface ContainerProps<T extends ElementType = 'div'> {
  as?: T
  size?: ContainerSize
  className?: string
  children: ReactNode
}

export function Container<T extends ElementType = 'div'>({
  as,
  size = 'lg',
  className = '',
  children,
  ...rest
}: ContainerProps<T> & Omit<ComponentPropsWithoutRef<T>, keyof ContainerProps<T>>) {
  const Component = as || 'div'
  const classes = ['ui-container', `ui-container--${size}`, className].filter(Boolean).join(' ')

  return (
    <Component className={classes} {...rest}>
      {children}
    </Component>
  )
}

