import type { InputHTMLAttributes } from 'react'
import { useId } from 'react'
import './Input.css'

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  hint?: string
  error?: string
  required?: boolean
}

export function Input({
  id: explicitId,
  label,
  hint,
  error,
  required = false,
  className = '',
  disabled = false,
  ...rest
}: InputProps) {
  const generatedId = useId()
  const inputId = explicitId || generatedId
  const hintId = hint ? `${inputId}-hint` : undefined
  const errorId = error ? `${inputId}-error` : undefined

  const describedBy = [hintId, errorId].filter(Boolean).join(' ') || undefined

  const fieldClasses = [
    'ui-input-field',
    error ? 'ui-input-field--error' : '',
    className,
  ].filter(Boolean).join(' ')

  return (
    <div className="ui-input-group">
      {label && (
        <label
          htmlFor={inputId}
          className={`ui-input-label ${required ? 'ui-input-label--required' : ''}`}
        >
          {label}
        </label>
      )}
      <input
        id={inputId}
        className={fieldClasses}
        disabled={disabled}
        aria-invalid={Boolean(error)}
        aria-describedby={describedBy}
        required={required}
        {...rest}
      />
      {hint && !error && (
        <p id={hintId} className="ui-input-hint">
          {hint}
        </p>
      )}
      {error && (
        <p id={errorId} className="ui-input-error" role="alert">
          {error}
        </p>
      )}
    </div>
  )
}

