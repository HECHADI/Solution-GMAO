import React, { useMemo, useState } from 'react'
import { reinitialiserMotDePasse } from '../api/authApi'
import './AuthPages.css'

export default function ResetPasswordPage({ onRetourConnexion }) {
  const token = useMemo(() => new URLSearchParams(window.location.search).get('token'), [])

  const [nouveauMotDePasse, setNouveauMotDePasse] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [reussi, setReussi] = useState(false)
  const [erreur, setErreur] = useState(null)
  const [chargement, setChargement] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setErreur(null)

    if (nouveauMotDePasse.length < 8) {
      setErreur('Le mot de passe doit contenir au moins 8 caractères')
      return
    }
    if (nouveauMotDePasse !== confirmation) {
      setErreur('Les deux mots de passe ne correspondent pas')
      return
    }

    setChargement(true)
    try {
      await reinitialiserMotDePasse(token, nouveauMotDePasse)
      setReussi(true)
    } catch (err) {
      setErreur(err.response?.data?.message || 'Ce lien est invalide ou a expiré')
    } finally {
      setChargement(false)
    }
  }

  if (!token) {
    return (
      <div className="auth-frame">
        <div className="auth-card glass anim-fadein">
          <div className="auth-brand">
            <h1 className="gradient-text">⚙️ GMAO Hub</h1>
          </div>
          <div className="auth-error">⚠️ Lien de réinitialisation invalide ou incomplet.</div>
          <button type="button" className="auth-link" onClick={onRetourConnexion}>
            ← Retour à la connexion
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-frame">
      <div className="auth-card glass anim-fadein">
        <div className="auth-brand">
          <h1 className="gradient-text">⚙️ GMAO Hub</h1>
          <p>Choisis un nouveau mot de passe</p>
        </div>

        {reussi ? (
          <>
            <div className="auth-success">✅ Mot de passe réinitialisé avec succès.</div>
            <button type="button" className="btn btn-primary auth-submit" onClick={onRetourConnexion}>
              Se connecter
            </button>
          </>
        ) : (
          <form onSubmit={handleSubmit} className="auth-form">
            <label className="auth-label">
              Nouveau mot de passe
              <input
                type="password"
                required
                autoFocus
                minLength={8}
                className="auth-input"
                placeholder="••••••••"
                value={nouveauMotDePasse}
                onChange={(e) => setNouveauMotDePasse(e.target.value)}
              />
              <span className="auth-hint">8 caractères minimum</span>
            </label>

            <label className="auth-label">
              Confirmer le mot de passe
              <input
                type="password"
                required
                className="auth-input"
                placeholder="••••••••"
                value={confirmation}
                onChange={(e) => setConfirmation(e.target.value)}
              />
            </label>

            {erreur && <div className="auth-error">⚠️ {erreur}</div>}

            <button type="submit" className="btn btn-primary auth-submit" disabled={chargement}>
              {chargement ? 'Enregistrement…' : 'Réinitialiser le mot de passe'}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}
