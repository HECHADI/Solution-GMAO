import React, { useMemo, useState } from 'react'
import { useInterventions, useUpdateStatut } from '../hooks/useInterventions'
import './MaintenancePreventive.css'

// ──────────────────────────────────────────────────────────────────────────
// ⚠️ Aucune fréquence de récurrence n'est stockée en base (pas d'entité
// PlanMaintenance). L'échéance affichée est donc une ESTIMATION calculée
// côté client : dernière réalisation + intervalle par défaut. Elle sera
// remplacée par une vraie donnée dès que le backend exposera un plan de
// maintenance récurrent (fréquence par équipement/type de contrôle).
// ──────────────────────────────────────────────────────────────────────────
const INTERVALLE_ESTIME_JOURS = 90

const URGENCE = {
  RETARD: { label: 'En retard', className: 'urgence-retard', order: 0 },
  BIENTOT: { label: 'Bientôt due', className: 'urgence-bientot', order: 1 },
  A_JOUR: { label: 'Planifiée', className: 'urgence-ok', order: 2 },
  NON_PLANIFIEE: { label: 'Non planifiée', className: 'urgence-inconnue', order: 3 },
}

function ajouterJours(date, jours) {
  const d = new Date(date)
  d.setDate(d.getDate() + jours)
  return d
}

function formatDate(date) {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

/**
 * Calcule une échéance estimée + un niveau d'urgence pour une intervention
 * préventive, à partir des dates disponibles (aucune fréquence réelle en base).
 */
function calculerEcheance(interv) {
  const maintenant = new Date()

  if (interv.statut === 'REALISE' && interv.dateFin) {
    const prochaine = ajouterJours(interv.dateFin, INTERVALLE_ESTIME_JOURS)
    const joursRestants = Math.ceil((prochaine - maintenant) / (1000 * 60 * 60 * 24))
    return {
      dateReference: interv.dateFin,
      dateEcheance: prochaine,
      libelleReference: 'Dernière réalisation',
      urgence: joursRestants < 0 ? URGENCE.RETARD : joursRestants <= 15 ? URGENCE.BIENTOT : URGENCE.A_JOUR,
      joursRestants,
    }
  }

  if (interv.dateDebut) {
    const echeance = new Date(interv.dateDebut)
    const joursRestants = Math.ceil((echeance - maintenant) / (1000 * 60 * 60 * 24))
    return {
      dateReference: interv.dateDebut,
      dateEcheance: echeance,
      libelleReference: 'Planifiée pour',
      urgence: joursRestants < 0 ? URGENCE.RETARD : joursRestants <= 7 ? URGENCE.BIENTOT : URGENCE.A_JOUR,
      joursRestants,
    }
  }

  return {
    dateReference: null,
    dateEcheance: null,
    libelleReference: null,
    urgence: URGENCE.NON_PLANIFIEE,
    joursRestants: null,
  }
}

function libelleJours(joursRestants) {
  if (joursRestants === null) return null
  if (joursRestants < 0) return `en retard de ${Math.abs(joursRestants)} j`
  if (joursRestants === 0) return "aujourd'hui"
  return `dans ${joursRestants} j`
}

export default function MaintenancePreventive() {
  const { data: interventions = [], isLoading } = useInterventions()
  const updateStatut = useUpdateStatut()

  const [searchTerm, setSearchTerm] = useState('')
  const [sortKey, setSortKey] = useState('urgence')

  const preventifsEnrichis = useMemo(() => {
    return interventions
      .filter((interv) => interv.type === 'PREVENTIF')
      .map((interv) => ({ ...interv, echeance: calculerEcheance(interv) }))
  }, [interventions])

  const filtres = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    if (!term) return preventifsEnrichis

    return preventifsEnrichis.filter((p) => {
      const haystack = [p.titre, p.equipementNom, p.siteNom, p.description]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
      return haystack.includes(term)
    })
  }, [preventifsEnrichis, searchTerm])

  const tries = useMemo(() => {
    const copie = [...filtres]
    if (sortKey === 'urgence') {
      copie.sort((a, b) => {
        if (a.echeance.urgence.order !== b.echeance.urgence.order) {
          return a.echeance.urgence.order - b.echeance.urgence.order
        }
        // À l'intérieur d'un même niveau d'urgence, le plus proche/plus en retard d'abord
        const ja = a.echeance.joursRestants ?? Infinity
        const jb = b.echeance.joursRestants ?? Infinity
        return ja - jb
      })
    } else if (sortKey === 'titre') {
      copie.sort((a, b) => (a.titre ?? '').localeCompare(b.titre ?? ''))
    } else if (sortKey === 'equipement') {
      copie.sort((a, b) => (a.equipementNom ?? '').localeCompare(b.equipementNom ?? ''))
    }
    return copie
  }, [filtres, sortKey])

  const compteurs = useMemo(() => {
    const c = { retard: 0, bientot: 0, ok: 0, inconnue: 0 }
    preventifsEnrichis.forEach((p) => {
      if (p.echeance.urgence === URGENCE.RETARD) c.retard++
      else if (p.echeance.urgence === URGENCE.BIENTOT) c.bientot++
      else if (p.echeance.urgence === URGENCE.A_JOUR) c.ok++
      else c.inconnue++
    })
    return c
  }, [preventifsEnrichis])

  const handleAction = (interv) => {
    if (interv.statut === 'A_FAIRE') {
      updateStatut.mutate({ id: interv.id, statut: 'EN_COURS' })
    } else if (interv.statut === 'EN_COURS') {
      updateStatut.mutate({ id: interv.id, statut: 'REALISE' })
    }
  }

  const libelleAction = (statut) => {
    if (statut === 'A_FAIRE') return '▶️ Démarrer'
    if (statut === 'EN_COURS') return '✅ Clôturer'
    return null
  }

  if (isLoading) {
    return (
      <div className="preventive-grid anim-fadein">
        {Array.from({ length: 4 }).map((_, i) => (
          <div className="maintenance-card-skeleton" key={i} />
        ))}
      </div>
    )
  }

  return (
    <div className="maintenance-wrapper anim-fadein">
      {/* ── Barre d'outils ─────────────────────────────────────────────── */}
      <div className="maintenance-toolbar">
        <input
          type="text"
          className="search-input"
          placeholder="🔍 Rechercher (titre, équipement, site)…"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <select value={sortKey} onChange={(e) => setSortKey(e.target.value)} className="sort-select">
          <option value="urgence">Trier par urgence</option>
          <option value="titre">Trier par titre</option>
          <option value="equipement">Trier par équipement</option>
        </select>
      </div>

      {/* ── Résumé compteurs ───────────────────────────────────────────── */}
      <div className="maintenance-summary">
        <span className="summary-chip chip-retard">🔴 {compteurs.retard} en retard</span>
        <span className="summary-chip chip-bientot">🟡 {compteurs.bientot} bientôt dues</span>
        <span className="summary-chip chip-ok">🟢 {compteurs.ok} à jour</span>
        {compteurs.inconnue > 0 && (
          <span className="summary-chip chip-inconnue">⚪ {compteurs.inconnue} non planifiées</span>
        )}
      </div>

      <p className="estimation-disclaimer">
        ℹ️ Échéances estimées automatiquement (intervalle par défaut : {INTERVALLE_ESTIME_JOURS} jours) —
        en attendant un vrai plan de maintenance récurrent en base.
      </p>

      {/* ── Grille de cartes ───────────────────────────────────────────── */}
      {tries.length === 0 ? (
        <div className="empty-state">
          {searchTerm
            ? 'Aucun plan préventif ne correspond à cette recherche.'
            : 'Aucun plan de maintenance préventive enregistré.'}
        </div>
      ) : (
        <div className="preventive-grid">
          {tries.map((plan) => {
            const { echeance } = plan
            const action = libelleAction(plan.statut)

            return (
              <div key={plan.id} className={`maintenance-card glass urgence-border-${echeance.urgence.className}`}>
                <div className="card-top">
                  <h3 className="card-title">{plan.titre}</h3>
                  <span className={`period-badge ${echeance.urgence.className}`}>
                    {echeance.urgence.label}
                  </span>
                </div>

                <div className="card-meta">
                  <span>
                    <strong>📍 Équipement :</strong> {plan.equipementNom ?? `ID #${plan.equipementId ?? '?'}`}
                  </span>
                  {plan.siteNom && (
                    <span><strong>🏭 Site :</strong> {plan.siteNom}</span>
                  )}
                  <span><strong>⏱️ Durée requise :</strong> {plan.dureeEstimee ?? '—'} minutes</span>
                  {plan.description && (
                    <span><strong>📝 Description :</strong> {plan.description}</span>
                  )}
                </div>

                <div className="card-echeance">
                  {echeance.dateEcheance ? (
                    <>
                      <span className="echeance-label">{echeance.libelleReference} :</span>
                      <span className="echeance-date">{formatDate(echeance.dateReference)}</span>
                      {echeance.urgence !== URGENCE.A_JOUR && (
                        <span className={`echeance-relatif ${echeance.urgence.className}`}>
                          {libelleJours(echeance.joursRestants)}
                        </span>
                      )}
                    </>
                  ) : (
                    <span className="echeance-label">Aucune date renseignée</span>
                  )}
                </div>

                {action ? (
                  <button className="btn btn-primary btn-schedule" onClick={() => handleAction(plan)}>
                    {action}
                  </button>
                ) : (
                  <button className="btn btn-schedule btn-disabled" disabled>
                    ✔️ Réalisée le {formatDate(plan.dateFin)}
                  </button>
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
