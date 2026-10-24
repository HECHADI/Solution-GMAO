import React, { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import './AuthPages.css'

export default function LoginPage({ onNaviguerVersMotDePasseOublie }) {
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [motDePasse, setMotDePasse] = useState('')
  const [erreur, setErreur] = useState(null)
  const [chargement, setChargement] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setErreur(null)
    setChargement(true)
    try {
      await login(email, motDePasse)
      // Rien d'autre à faire : App.jsx bascule automatiquement sur
      // l'app une fois isAuthenticated devenu vrai.
    } catch (err) {
      setErreur(err.response?.data?.message || 'Email ou mot de passe incorrect')
    } finally {
      setChargement(false)
    }
  }

  return (
    <div className="auth-frame">
      <div className="auth-card glass anim-fadein">
        <div className="auth-brand">
          <h1 className="gradient-text">⚙️ GMAO Hub</h1>
          <p>Connectez-vous à votre espace</p>
        </div>

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

          <label className="auth-label">
            Mot de passe
            <input
              type="password"
              required
              className="auth-input"
              placeholder="••••••••"
              value={motDePasse}
              onChange={(e) => setMotDePasse(e.target.value)}
            />
          </label>

          {erreur && <div className="auth-error">⚠️ {erreur}</div>}

          <button type="submit" className="btn btn-primary auth-submit" disabled={chargement}>
            {chargement ? 'Connexion…' : 'Se connecter'}
          </button>

          <button
            type="button"
            className="auth-link"
            onClick={onNaviguerVersMotDePasseOublie}
          >
            Mot de passe oublié ?
          </button>
        </form>
      </div>
    </div>
  )
}
