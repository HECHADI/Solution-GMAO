import React, { useMemo, useState } from 'react'
import { useChantiers } from '../hooks/useChantiers'
import './SiteProjectManager.css'

const STATUT_INTERVENTION_LABELS = {
  A_FAIRE: 'À faire',
  EN_COURS: 'En cours',
  REALISE: 'Réalisé',
}

const STATUT_CHANTIER_LABELS = {
  PLANIFIE: 'Planifié',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  ANNULE: 'Annulé',
}

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

export default function SiteProjectManager() {
  const { data: chantiers = [], isLoading } = useChantiers()
  const [searchTerm, setSearchTerm] = useState('')
  const [filtreStatut, setFiltreStatut] = useState('')

  const filtres = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    return chantiers.filter((c) => {
      if (filtreStatut && c.statut !== filtreStatut) return false
      if (!term) return true
      return [c.nom, c.clientNom, c.siteNom].filter(Boolean).join(' ').toLowerCase().includes(term)
    })
  }, [chantiers, searchTerm, filtreStatut])

  if (isLoading) {
    return (
      <div className="project-container anim-fadein">
        <div className="project-card glass skeleton-card" />
        <div className="project-card glass skeleton-card" />
      </div>
    )
  }

  return (
    <div className="project-wrapper anim-fadein">
      {/* ── Barre d'outils ─────────────────────────────────────────────── */}
      <div className="project-toolbar">
        <input
          type="text"
          className="search-input"
          placeholder="🔍 Rechercher (nom, client, site)…"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <select value={filtreStatut} onChange={(e) => setFiltreStatut(e.target.value)} className="filtre-select">
          <option value="">Tous les statuts</option>
          {Object.entries(STATUT_CHANTIER_LABELS).map(([valeur, label]) => (
            <option key={valeur} value={valeur}>{label}</option>
          ))}
        </select>
      </div>

      {filtres.length === 0 ? (
        <div className="empty-state">
          {chantiers.length === 0
            ? 'Aucun chantier enregistré.'
            : 'Aucun chantier ne correspond à ces critères.'}
        </div>
      ) : (
        <div className="project-container">
          {filtres.map((chantier) => (
            <div key={chantier.id} className="project-card glass">
              <div className="project-title-row">
                <h3 className="project-title">{chantier.nom}</h3>
                <span className={`badge-chantier badge-chantier-${chantier.statut.toLowerCase().replace('_', '-')}`}>
                  {STATUT_CHANTIER_LABELS[chantier.statut]}
                </span>
              </div>

              <div className="project-meta">
                <span><strong>Client :</strong> {chantier.clientNom ?? '—'}</span>
                <span><strong>Site :</strong> {chantier.siteNom ?? '—'}</span>
                <span><strong>Début prévu :</strong> {formatDate(chantier.dateDebutPrevue)}</span>
                {chantier.dateFinPrevue && (
                  <span><strong>Fin prévue :</strong> {formatDate(chantier.dateFinPrevue)}</span>
                )}
                {chantier.budgetEstime != null && (
                  <span><strong>Budget estimé :</strong> {chantier.budgetEstime.toFixed(2)} €</span>
                )}
                {chantier.responsableNom && (
                  <span><strong>Responsable :</strong> {chantier.responsableNom}</span>
                )}
              </div>

              <div className="project-progress-row">
                <div className="progress-track">
                  <div
                    className="progress-fill"
                    style={{ width: `${chantier.avancementPourcentage}%` }}
                  />
                </div>
                <strong className="progress-label">{chantier.avancementPourcentage}%</strong>
                <span className="progress-detail">
                  ({chantier.nombreInterventionsRealisees}/{chantier.nombreInterventions})
                </span>
              </div>

              <div className="project-intervention-list">
                <h4 className="intervention-list-title">Ordres de travail associés :</h4>
                {chantier.interventions.length === 0 ? (
                  <p className="cell-muted">Aucune intervention rattachée à ce chantier.</p>
                ) : (
                  chantier.interventions.map((interv) => (
                    <div key={interv.id} className="project-intervention-item">
                      <div>
                        <div className="intervention-title">{interv.titre}</div>
                        <div className="intervention-tech">
                          Assigné à : {interv.technicienNom ?? 'Non assigné'}
                        </div>
                      </div>
                      <span className={`badge badge-${interv.statut.toLowerCase().replace('_', '-')}`}>
                        {STATUT_INTERVENTION_LABELS[interv.statut]}
                      </span>
                    </div>
                  ))
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
