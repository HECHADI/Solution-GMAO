import React, { useState } from 'react'
import { demanderReinitialisation } from '../api/authApi'
import './AuthPages.css'

export default function ForgotPasswordPage({ onRetourConnexion }) {
  const [email, setEmail] = useState('')
  const [envoye, setEnvoye] = useState(false)
  const [erreur, setErreur] = useState(null)
  const [chargement, setChargement] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setErreur(null)
    setChargement(true)
    try {
      await demanderReinitialisation(email)
      setEnvoye(true)
    } catch (err) {
      setErreur(err.response?.data?.message || "Une erreur est survenue, réessaie plus tard.")
    } finally {
      setChargement(false)
    }
  }

  return (
    <div className="auth-frame">
      <div className="auth-card glass anim-fadein">
        <div className="auth-brand">
          <h1 className="gradient-text">⚙️ GMAO Hub</h1>
          <p>Réinitialiser ton mot de passe</p>
        </div>

        {envoye ? (
          <div className="auth-success">
            📧 Si un compte existe avec cet email, un lien de réinitialisation vient d'être envoyé.
            Vérifie ta boîte de réception (et tes spams).
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="auth-form">
            <label className="auth-label">
              Email
              <input
                type="email"
                required
                autoFocus
                className="auth-input"
                placeholder="vous@entreprise.fr"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </label>

            {erreur && <div className="auth-error">⚠️ {erreur}</div>}

            <button type="submit" className="btn btn-primary auth-submit" disabled={chargement}>
              {chargement ? 'Envoi…' : 'Envoyer le lien de réinitialisation'}
            </button>
          </form>
        )}

        <button type="button" className="auth-link" onClick={onRetourConnexion}>
          ← Retour à la connexion
        </button>
      </div>
    </div>
  )
}
