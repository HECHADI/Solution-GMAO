import React, { useMemo, useState } from 'react'
import {
  useEquipements,
  useEquipementDetail,
  useSites,
  useCreerEquipement,
  useModifierEquipement,
  useChangerStatutEquipement,
  useSupprimerEquipement,
} from '../hooks/useEquipements'
import Modal from './ui/Modal'
import './EquipementManager.css'

const STATUT_LABELS = {
  EN_SERVICE: 'En service',
  EN_PANNE: 'En panne',
  EN_MAINTENANCE: 'En maintenance',
  HORS_SERVICE: 'Hors service',
  RETIRE: 'Retiré',
}

const FORM_VIDE = {
  nom: '', categorie: '', marque: '', modele: '', numeroSerie: '',
  dateInstallation: '', statut: 'EN_SERVICE', siteId: '',
}

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function formatDateHeure(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

export default function EquipementManager() {
  const { data: equipements = [], isLoading } = useEquipements()
  const { data: sites = [] } = useSites()

  const creerEquipement = useCreerEquipement()
  const modifierEquipement = useModifierEquipement()
  const changerStatut = useChangerStatutEquipement()
  const supprimerEquipement = useSupprimerEquipement()

  const [searchTerm, setSearchTerm] = useState('')
  const [filtreStatut, setFiltreStatut] = useState('')
  const [formModal, setFormModal] = useState(null) // { mode, equipement?, valeurs } | null
  const [equipementASupprimer, setEquipementASupprimer] = useState(null)
  const [detailId, setDetailId] = useState(null)

  const filtres = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    return equipements.filter((e) => {
      if (filtreStatut && e.statut !== filtreStatut) return false
      if (!term) return true
      return [e.nom, e.marque, e.modele, e.numeroSerie, e.siteNom, e.clientNom]
        .filter(Boolean).join(' ').toLowerCase().includes(term)
    })
  }, [equipements, searchTerm, filtreStatut])

  const ouvrirCreation = () => setFormModal({ mode: 'creer', valeurs: FORM_VIDE })

  const ouvrirEdition = (e) => setFormModal({
    mode: 'modifier',
    equipement: e,
    valeurs: {
      nom: e.nom, categorie: e.categorie ?? '', marque: e.marque ?? '', modele: e.modele ?? '',
      numeroSerie: e.numeroSerie ?? '', dateInstallation: e.dateInstallation ?? '',
      statut: e.statut, siteId: e.siteId ?? '',
    },
  })

  const handleChampChange = (champ, valeur) => {
    setFormModal((m) => ({ ...m, valeurs: { ...m.valeurs, [champ]: valeur } }))
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    const payload = { ...formModal.valeurs, siteId: Number(formModal.valeurs.siteId) }
    if (!payload.nom.trim() || !payload.siteId) return

    if (formModal.mode === 'creer') {
      creerEquipement.mutate(payload)
    } else {
      modifierEquipement.mutate({ id: formModal.equipement.id, payload })
    }
    setFormModal(null)
  }

  const confirmerSuppression = () => {
    if (!equipementASupprimer) return
    supprimerEquipement.mutate(equipementASupprimer.id)
    setEquipementASupprimer(null)
  }

  if (isLoading) {
    return (
      <div className="equip-container anim-fadein">
        <div className="equip-card glass skeleton-card" />
      </div>
    )
  }

  return (
    <div className="equip-container anim-fadein">
      <div className="equip-toolbar">
        <input
          type="text"
          className="search-input"
          placeholder="🔍 Rechercher (nom, marque, n° série, site)…"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <select value={filtreStatut} onChange={(e) => setFiltreStatut(e.target.value)} className="filtre-select">
          <option value="">Tous les statuts</option>
          {Object.entries(STATUT_LABELS).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
        </select>
        <button className="btn btn-primary" onClick={ouvrirCreation}>➕ Nouvel équipement</button>
      </div>

      <div className="equip-card glass">
        {filtres.length === 0 ? (
          <div className="empty-state">
            {equipements.length === 0 ? 'Aucun équipement enregistré.' : 'Aucun équipement ne correspond à ces critères.'}
          </div>
        ) : (
          <table className="gmao-table">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Marque / Modèle</th>
                <th>N° Série</th>
                <th>Site</th>
                <th>Statut</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtres.map((e) => (
                <tr key={e.id}>
                  <td>
                    <button className="link-nom" onClick={() => setDetailId(e.id)}>{e.nom}</button>
                    {e.categorie && <div className="cell-muted">{e.categorie}</div>}
                  </td>
                  <td className="cell-muted">{[e.marque, e.modele].filter(Boolean).join(' · ') || '—'}</td>
                  <td><code>{e.numeroSerie || '—'}</code></td>
                  <td>
                    {e.siteNom || '—'}
                    {e.clientNom && <div className="cell-muted">{e.clientNom}</div>}
                  </td>
                  <td>
                    <select
                      className={`badge-select statut-${e.statut.toLowerCase().replace('_', '-')}`}
                      value={e.statut}
                      onChange={(ev) => changerStatut.mutate({ id: e.id, statut: ev.target.value })}
                    >
                      {Object.entries(STATUT_LABELS).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
                    </select>
                  </td>
                  <td className="col-actions">
                    <button className="btn-icon" title="Modifier" onClick={() => ouvrirEdition(e)}>✏️</button>
                    <button className="btn-icon btn-icon-danger" title="Supprimer" onClick={() => setEquipementASupprimer(e)}>🗑️</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* ── Modale création/édition ─────────────────────────────────── */}
      {formModal && (
        <Modal
          title={formModal.mode === 'creer' ? 'Nouvel équipement' : 'Modifier l\'équipement'}
          icon="🔧"
          onClose={() => setFormModal(null)}
        >
          <form onSubmit={handleSubmit} className="equip-form">
            <label className="form-label">
              Nom <span className="required">*</span>
              <input type="text" required autoFocus className="modal-input"
                value={formModal.valeurs.nom}
                onChange={(e) => handleChampChange('nom', e.target.value)} />
            </label>
            <div className="form-row">
              <label className="form-label">
                Catégorie
                <input type="text" className="modal-input"
                  value={formModal.valeurs.categorie}
                  onChange={(e) => handleChampChange('categorie', e.target.value)} />
              </label>
              <label className="form-label">
                N° Série
                <input type="text" className="modal-input"
                  value={formModal.valeurs.numeroSerie}
                  onChange={(e) => handleChampChange('numeroSerie', e.target.value)} />
              </label>
            </div>
            <div className="form-row">
              <label className="form-label">
                Marque
                <input type="text" className="modal-input"
                  value={formModal.valeurs.marque}
                  onChange={(e) => handleChampChange('marque', e.target.value)} />
              </label>
              <label className="form-label">
                Modèle
                <input type="text" className="modal-input"
                  value={formModal.valeurs.modele}
                  onChange={(e) => handleChampChange('modele', e.target.value)} />
              </label>
            </div>
            <div className="form-row">
              <label className="form-label">
                Date d'installation
                <input type="date" className="modal-input"
                  value={formModal.valeurs.dateInstallation}
                  onChange={(e) => handleChampChange('dateInstallation', e.target.value)} />
              </label>
              <label className="form-label">
                Statut
                <select className="modal-input" value={formModal.valeurs.statut}
                  onChange={(e) => handleChampChange('statut', e.target.value)}>
                  {Object.entries(STATUT_LABELS).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
                </select>
              </label>
            </div>
            <label className="form-label">
              Site <span className="required">*</span>
              <select required className="modal-input" value={formModal.valeurs.siteId}
                onChange={(e) => handleChampChange('siteId', e.target.value)}>
                <option value="">Sélectionner un site…</option>
                {sites.map((s) => (
                  <option key={s.id} value={s.id}>{s.nom} — {s.clientNom}</option>
                ))}
              </select>
            </label>
            <div className="modal-buttons">
              <button type="button" className="btn btn-cancel" onClick={() => setFormModal(null)}>Annuler</button>
              <button type="submit" className="btn btn-primary">
                {formModal.mode === 'creer' ? 'Créer' : 'Enregistrer'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* ── Modale suppression ──────────────────────────────────────── */}
      {equipementASupprimer && (
        <Modal title="Supprimer l'équipement" icon="⚠️" onClose={() => setEquipementASupprimer(null)}>
          <p className="modal-context">
            Supprimer définitivement <strong>{equipementASupprimer.nom}</strong> ? Cette action est irréversible.
            {' '}Si des interventions y sont rattachées, la suppression sera refusée.
          </p>
          <div className="modal-buttons">
            <button className="btn btn-cancel" onClick={() => setEquipementASupprimer(null)}>Annuler</button>
            <button className="btn btn-danger" onClick={confirmerSuppression}>Supprimer</button>
          </div>
        </Modal>
      )}

      {/* ── Modale fiche détail ─────────────────────────────────────── */}
      {detailId && (
        <FicheDetailEquipement id={detailId} onClose={() => setDetailId(null)} />
      )}
    </div>
  )
}

function FicheDetailEquipement({ id, onClose }) {
  const { data, isLoading } = useEquipementDetail(id)

  return (
    <Modal title={isLoading ? 'Chargement…' : data.equipement.nom} icon="🔧" onClose={onClose}>
      {isLoading ? (
        <p className="cell-muted">Chargement…</p>
      ) : (
        <div className="fiche-detail">
          <div className="fiche-meta">
            <span><strong>Site :</strong> {data.equipement.siteNom} ({data.equipement.clientNom})</span>
            <span><strong>Marque / Modèle :</strong> {[data.equipement.marque, data.equipement.modele].filter(Boolean).join(' · ') || '—'}</span>
            <span><strong>N° Série :</strong> {data.equipement.numeroSerie || '—'}</span>
            <span><strong>Installé le :</strong> {formatDate(data.equipement.dateInstallation)}</span>
            <span><strong>Statut :</strong> {STATUT_LABELS[data.equipement.statut]}</span>
          </div>

          <div className="fiche-section">
            <h5>🛠️ Historique des interventions ({data.historiqueInterventions.length})</h5>
            {data.historiqueInterventions.length === 0 ? (
              <p className="cell-muted">Aucune intervention enregistrée.</p>
            ) : (
              <ul className="fiche-liste">
                {data.historiqueInterventions.map((i) => (
                  <li key={i.id}>
                    <span>{i.titre}</span>
                    <span className="cell-muted">{formatDateHeure(i.dateDebut)} · {i.technicienNom ?? 'Non assigné'}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="fiche-section">
            <h5>📅 Plans de maintenance liés ({data.plansMaintenance.length})</h5>
            {data.plansMaintenance.length === 0 ? (
              <p className="cell-muted">Aucun plan de maintenance récurrent.</p>
            ) : (
              <ul className="fiche-liste">
                {data.plansMaintenance.map((p) => (
                  <li key={p.id}>
                    <span>{p.nom}</span>
                    <span className="cell-muted">
                      Tous les {p.frequenceValeur} {p.frequenceUnite.toLowerCase()} · prochaine échéance {formatDate(p.prochaineEcheance)}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}
    </Modal>
  )
}
