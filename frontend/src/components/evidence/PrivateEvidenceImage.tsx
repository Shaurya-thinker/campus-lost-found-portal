import { useState, useEffect } from 'react'
import { fetchEvidenceBlobUrl } from '../../services/evidenceApi'
import './PrivateEvidenceImage.css'

export interface PrivateEvidenceImageProps {
  evidenceId: number
  altText: string
  label?: string
  className?: string
}

export function PrivateEvidenceImage({
  evidenceId,
  altText,
  label = 'Private Photo Evidence',
  className = '',
}: PrivateEvidenceImageProps) {
  const [blobUrl, setBlobUrl] = useState<string | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const [isZoomed, setIsZoomed] = useState<boolean>(false)

  useEffect(() => {
    let isCurrent = true
    let createdUrl: string | null = null

    async function loadImage() {
      setLoading(true)
      setError(null)
      try {
        const url = await fetchEvidenceBlobUrl(evidenceId)
        createdUrl = url
        if (isCurrent) {
          setBlobUrl(url)
        } else {
          URL.revokeObjectURL(url)
        }
      } catch (err: unknown) {
        if (isCurrent) {
          setError(err instanceof Error ? err.message : 'Failed to load private photo')
        }
      } finally {
        if (isCurrent) {
          setLoading(false)
        }
      }
    }

    loadImage()

    return () => {
      isCurrent = false
      if (createdUrl) {
        URL.revokeObjectURL(createdUrl)
      }
    }
  }, [evidenceId])

  return (
    <div className={`private-evidence-container ${className}`}>
      {label && (
        <span className="private-evidence-badge" title="Accessible only to authorized users with JWT credentials">
          🔒 {label}
        </span>
      )}

      <div className="private-evidence-frame">
        {loading && (
          <div className="private-evidence-loading">
            <span>Loading secure photo...</span>
          </div>
        )}

        {error && (
          <div className="private-evidence-error" role="alert">
            <span>{error}</span>
          </div>
        )}

        {blobUrl && !loading && !error && (
          <img
            src={blobUrl}
            alt={altText}
            className="private-evidence-img"
            onClick={() => setIsZoomed(true)}
            title="Click to expand"
          />
        )}
      </div>

      {/* Lightbox modal when clicked */}
      {isZoomed && blobUrl && (
        <div
          className="evidence-modal-backdrop"
          onClick={() => setIsZoomed(false)}
          role="dialog"
          aria-modal="true"
          aria-label="Enlarged private evidence photo"
        >
          <div className="evidence-modal-content" onClick={(e) => e.stopPropagation()}>
            <button
              type="button"
              className="evidence-modal-close"
              onClick={() => setIsZoomed(false)}
              aria-label="Close enlarged preview"
            >
              &times;
            </button>
            <img src={blobUrl} alt={altText} className="evidence-modal-img" />
            <span style={{ color: 'white', fontSize: '13px' }}>
              🔒 Private Evidence #{evidenceId} — {altText}
            </span>
          </div>
        </div>
      )}
    </div>
  )
}

