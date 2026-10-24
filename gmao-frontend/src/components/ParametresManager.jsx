import React, { useState } from 'react'
import { useParametres, useModifierParametre } from '../hooks/useParametres'
import { useTheme } from '../hooks/useTheme'
import './ParametresManager.css'

// Unité affichée à côté de certains paramètres connus, pour plus de clarté
const UNITES = {
  taux_horaire_defaut: '€/h',
  seuil_alerte_stock_defaut: 'unités',
  delai_alerte_maintenance_defaut_jours: 'jours',
  intervalle_maintenance_estimee_jours: 'jours',
}

export default function ParametresManager() {
  const { data: parametres = [], isLoading } = useParametres()
  const modifierParametre = useModifierParametre()
  const { theme, toggleTheme, isDark } = useTheme()

  const [valeursEnEdition, setValeursEnEdition] = useState({}) // { [cle]: valeur en cours de saisie }

  const handleChange = (cle, valeur) => {
    setValeursEnEdition((prev) => ({ ...prev, [cle]: valeur }))
  }

  const handleValider = (parametre) => {
    const brut = valeursEnEdition[parametre.cle]
    if (brut === undefined || brut === parametre.valeur) return

    modifierParametre.mutate({ cle: parametre.cle, valeur: brut })
    setValeursEnEdition((prev) => {
      const next = { ...prev }
      delete next[parametre.cle]
      return next
    })
  }

  return (
    <div className="param-container anim-fadein">
      {/* ── Apparence ──────────────────────────────────────────────── */}
      <div className="param-card glass">
        <h3 className="param-title">🎨 Apparence</h3>
        <div className="theme-row">
          <div>
            <div className="theme-label">Thème de l'interface</div>
            <div className="theme-sub">
              {isDark ? 'Mode sombre activé' : 'Mode clair activé'}
            </div>
          </div>
          <button
            className={`theme-switch ${isDark ? 'theme-switch-dark' : 'theme-switch-light'}`}
            onClick={toggleTheme}
            role="switch"
            aria-checked={isDark}
            title="Changer de thème"
          >
            <span className="theme-switch-thumb">{isDark ? '🌙' : '☀️'}</span>
          </button>
        </div>
      </div>

      {/* ── Paramètres applicatifs ────────────────────────────────── */}
      <div className="param-card glass">
        <h3 className="param-title">⚙️ Paramètres généraux</h3>

        {isLoading ? (
          <p className="cell-muted">Chargement…</p>
        ) : parametres.length === 0 ? (
          <div className="empty-state">Aucun paramètre configuré.</div>
        ) : (
          <div className="param-list">
            {parametres.map((p) => (
              <div key={p.cle} className="param-row">
                <div className="param-info">
                  <div className="param-nom">{p.description || p.cle}</div>
                  <div className="param-cle">{p.cle}</div>
                </div>
                <div className="param-input-group">
                  <input
                    type="text"
                    className="param-input"
                    value={valeursEnEdition[p.cle] ?? p.valeur}
                    onChange={(e) => handleChange(p.cle, e.target.value)}
                    onBlur={() => handleValider(p)}
                    onKeyDown={(e) => e.key === 'Enter' && e.target.blur()}
                  />
                  {UNITES[p.cle] && <span className="param-unite">{UNITES[p.cle]}</span>}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
