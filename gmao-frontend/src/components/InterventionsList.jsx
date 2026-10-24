import React, { useMemo, useState } from 'react'
import {
  useInterventions,
  useAssignerTechnicien,
  useUpdateStatut,
  useDeleteIntervention,
} from '../hooks/useInterventions'
import './InterventionsList.css'

// ──────────────────────────────────────────────────────────────────────────
// ⚠️ Hypothèse sur le shape des données renvoyées par fetchInterventions()
// (InterventionResponse côté backend). Adapte les noms de champs ci-dessous
// si ton DTO diffère :
//   { id, titre, type, statut, dateDebut, dateFin, dureeEstimee,
//     equipementNom, siteNom, clientNom, technicienId, technicienNom }
// ──────────────────────────────────────────────────────────────────────────

const STATUTS = ['A_FAIRE', 'EN_COURS', 'REALISE']
const TYPES = ['PREVENTIF', 'CURATIF']

const STATUT_LABELS = {
  A_FAIRE: 'À faire',
  EN_COURS: 'En cours',
  REALISE: 'Réalisé',
}

const TYPE_LABELS = {
  PREVENTIF: 'Préventif',
  CURATIF: 'Curatif',
}

function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function InterventionsList({ filters }) {
  const { data: interventions = [], isLoading, isFetching } = useInterventions()
  const assignerTechnicien = useAssignerTechnicien()
  const updateStatut = useUpdateStatut()
  const deleteIntervention = useDeleteIntervention()

  const [searchTerm, setSearchTerm] = useState('')
  const [sortConfig, setSortConfig] = useState({ key: 'dateDebut', direction: 'desc' })
  const [selectedIds, setSelectedIds] = useState(new Set())
  const [bulkTechnicienId, setBulkTechnicienId] = useState('')

  // Liste des techniciens connus, dérivée des interventions déjà chargées.
  // À terme, remplacer par un vrai GET /api/utilisateurs?role=TECHNICIEN.
  const techniciensConnus = useMemo(() => {
    const map = new Map()
    interventions.forEach((i) => {
      if (i.technicienId != null) {
        map.set(i.technicienId, i.technicienNom ?? `#${i.technicienId}`)
      }
    })
    return Array.from(map.entries()).map(([id, nom]) => ({ id, nom }))
  }, [interventions])

  // ── Filtrage (recherche + filtres de la sidebar) ─────────────────────────
  const filtered = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()

    return interventions.filter((interv) => {
      if (filters?.statuts?.length && !filters.statuts.includes(interv.statut)) {
        return false
      }
      if (filters?.types?.length && !filters.types.includes(interv.type)) {
        return false
      }
      if (
        filters?.techniciens?.length &&
        !filters.techniciens.includes(interv.technicienId)
      ) {
        return false
      }
      if (!term) return true

      const haystack = [
        interv.titre,
        interv.equipementNom,
        interv.siteNom,
        interv.clientNom,
        interv.technicienNom,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()

      return haystack.includes(term)
    })
  }, [interventions, filters, searchTerm])

  // ── Tri ───────────────────────────────────────────────────────────────
  const sorted = useMemo(() => {
    const { key, direction } = sortConfig
    const factor = direction === 'asc' ? 1 : -1

    return [...filtered].sort((a, b) => {
      let va = a[key]
      let vb = b[key]

      if (key === 'dateDebut') {
        va = va ? new Date(va).getTime() : 0
        vb = vb ? new Date(vb).getTime() : 0
      } else if (typeof va === 'string' || typeof vb === 'string') {
        va = (va ?? '').toString().toLowerCase()
        vb = (vb ?? '').toString().toLowerCase()
      } else {
        va = va ?? 0
        vb = vb ?? 0
      }

      if (va < vb) return -1 * factor
      if (va > vb) return 1 * factor
      return 0
    })
  }, [filtered, sortConfig])

  const handleSort = (key) => {
    setSortConfig((prev) =>
      prev.key === key
        ? { key, direction: prev.direction === 'asc' ? 'desc' : 'asc' }
        : { key, direction: 'asc' }
    )
  }

  const sortIndicator = (key) => {
    if (sortConfig.key !== key) return null
    return <span className="sort-arrow">{sortConfig.direction === 'asc' ? '▲' : '▼'}</span>
  }

  // ── Sélection multiple ───────────────────────────────────────────────
  const allVisibleSelected =
    sorted.length > 0 && sorted.every((i) => selectedIds.has(i.id))

  const toggleSelectAll = () => {
    setSelectedIds((prev) => {
      if (allVisibleSelected) return new Set()
      return new Set(sorted.map((i) => i.id))
    })
  }

  const toggleSelectOne = (id) => {
    setSelectedIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  const handleBulkAssign = () => {
    if (!bulkTechnicienId || selectedIds.size === 0) return
    selectedIds.forEach((id) => {
      assignerTechnicien.mutate({ id, technicienId: Number(bulkTechnicienId) })
    })
    setSelectedIds(new Set())
    setBulkTechnicienId('')
  }

  const handleDelete = (interv) => {
    if (window.confirm(`Supprimer l'intervention « ${interv.titre} » ?`)) {
      deleteIntervention.mutate(interv.id)
    }
  }

  // ── Rendu ────────────────────────────────────────────────────────────

  if (isLoading) {
    return (
      <div className="table-wrapper glass anim-fadein">
        <div className="skeleton-toolbar" />
        {Array.from({ length: 6 }).map((_, i) => (
          <div className="skeleton-row" key={i} />
        ))}
      </div>
    )
  }

  return (
    <div className="table-wrapper glass anim-fadein">
      {/* ── Barre d'outils ────────────────────────────────────────────── */}
      <div className="list-toolbar">
        <input
          type="text"
          className="search-input"
          placeholder="🔍 Rechercher (titre, équipement, site, technicien)…"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />

        <div className="toolbar-right">
          {isFetching && <span className="refresh-indicator"><span className="refresh-dot" /> Actualisation…</span>}
          <span className="results-count">
            {sorted.length} intervention{sorted.length > 1 ? 's' : ''}
          </span>
        </div>
      </div>

      {/* ── Barre d'actions groupées (visible si sélection) ─────────────── */}
      {selectedIds.size > 0 && (
        <div className="bulk-bar anim-fadein">
          <span>{selectedIds.size} sélectionnée(s)</span>
          <select
            value={bulkTechnicienId}
            onChange={(e) => setBulkTechnicienId(e.target.value)}
            className="bulk-select"
          >
            <option value="">Assigner à…</option>
            {techniciensConnus.map((t) => (
              <option key={t.id} value={t.id}>{t.nom}</option>
            ))}
          </select>
          <button
            className="btn-bulk-apply"
            disabled={!bulkTechnicienId}
            onClick={handleBulkAssign}
          >
            Appliquer
          </button>
          <button className="btn-bulk-cancel" onClick={() => setSelectedIds(new Set())}>
            Annuler
          </button>
        </div>
      )}

      {sorted.length === 0 ? (
        <div className="empty-state">
          <p>Aucune intervention ne correspond à ces critères.</p>
        </div>
      ) : (
        <table className="gmao-table">
          <thead>
            <tr>
              <th className="col-checkbox">
                <input
                  type="checkbox"
                  checked={allVisibleSelected}
                  onChange={toggleSelectAll}
                />
              </th>
              <th onClick={() => handleSort('id')} className="sortable">ID {sortIndicator('id')}</th>
              <th onClick={() => handleSort('titre')} className="sortable">Titre {sortIndicator('titre')}</th>
              <th>Équipement / Site</th>
              <th onClick={() => handleSort('type')} className="sortable">Type {sortIndicator('type')}</th>
              <th onClick={() => handleSort('statut')} className="sortable">Statut {sortIndicator('statut')}</th>
              <th onClick={() => handleSort('dateDebut')} className="sortable">Date {sortIndicator('dateDebut')}</th>
              <th onClick={() => handleSort('dureeEstimee')} className="sortable">Durée {sortIndicator('dureeEstimee')}</th>
              <th>Technicien</th>
              <th className="col-actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            {sorted.map((interv) => (
              <tr key={interv.id} className={selectedIds.has(interv.id) ? 'row-selected' : ''}>
                <td>
                  <input
                    type="checkbox"
                    checked={selectedIds.has(interv.id)}
                    onChange={() => toggleSelectOne(interv.id)}
                  />
                </td>
                <td><strong>#{interv.id}</strong></td>
                <td className="cell-titre">{interv.titre}</td>
                <td className="cell-secondary">
                  <div>{interv.equipementNom ?? '—'}</div>
                  <div className="cell-muted">{interv.siteNom ?? ''}</div>
                </td>
                <td>
                  <span className={`badge-type badge-type-${interv.type?.toLowerCase()}`}>
                    {TYPE_LABELS[interv.type] ?? interv.type}
                  </span>
                </td>
                <td>
                  <select
                    className={`badge badge-select badge-${interv.statut?.toLowerCase().replace('_', '-')}`}
                    value={interv.statut}
                    onChange={(e) =>
                      updateStatut.mutate({ id: interv.id, statut: e.target.value })
                    }
                  >
                    {STATUTS.map((s) => (
                      <option key={s} value={s}>{STATUT_LABELS[s]}</option>
                    ))}
                  </select>
                </td>
                <td className="cell-muted">{formatDate(interv.dateDebut)}</td>
                <td>{interv.dureeEstimee ?? '—'} min</td>
                <td>
                  <select
                    className="technicien-select"
                    value={interv.technicienId ?? ''}
                    onChange={(e) =>
                      assignerTechnicien.mutate({
                        id: interv.id,
                        technicienId: e.target.value ? Number(e.target.value) : null,
                      })
                    }
                  >
                    <option value="">Non assigné</option>
                    {techniciensConnus.map((t) => (
                      <option key={t.id} value={t.id}>{t.nom}</option>
                    ))}
                  </select>
                </td>
                <td className="col-actions">
                  <button
                    className="btn-icon btn-delete"
                    title="Supprimer"
                    onClick={() => handleDelete(interv)}
                  >
                    🗑️
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
