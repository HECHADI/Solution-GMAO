import React, { useEffect } from 'react'
import './Modal.css'

/**
 * Modale générique stylée cohérente avec le thème sombre de l'app.
 * Remplace window.prompt() / window.confirm() qui affichent le popup
 * natif du navigateur (avec l'URL en toutes lettres).
 *
 * Fermeture : clic sur l'overlay, touche Échap, ou bouton dédié.
 */
export default function Modal({ title, icon, onClose, children }) {
  useEffect(() => {
    const handleEscape = (e) => e.key === 'Escape' && onClose()
    window.addEventListener('keydown', handleEscape)
    return () => window.removeEventListener('keydown', handleEscape)
  }, [onClose])

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card glass anim-fadein" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h4>{icon && <span className="modal-icon">{icon}</span>}{title}</h4>
          <button className="modal-close" onClick={onClose} aria-label="Fermer">✕</button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  )
}
