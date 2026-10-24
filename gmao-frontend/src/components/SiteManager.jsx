import React, { useMemo, useState } from 'react'
import { useSites, useCreerSite, useModifierSite, useSupprimerSite } from '../hooks/useSites'
import { useClients } from '../hooks/useClients'
import Modal from './ui/Modal'
import './SiteManager.css'

const FORM_VIDE = { nom: '', adresse: '', coordonneesGps: '', clientId: '' }

export default function SiteManager() {
  const { data: sites = [], isLoading } = useSites()
  const { data: clients = [] } = useClients()

  const creerSite = useCreerSite()
  const modifierSite = useModifierSite()
  const supprimerSite = useSupprimerSite()

  const [searchTerm, setSearchTerm] = useState('')
  const [filtreClient, setFiltreClient] = useState('')
  const [formModal, setFormModal] = useState(null) // { mode, site?, valeurs } | null
  const [siteASupprimer, setSiteASupprimer] = useState(null)

  const filtres = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    return sites.filter((s) => {
      if (filtreClient && String(s.clientId) !== filtreClient) return false
      if (!term) return true
      return [s.nom, s.adresse, s.clientNom].filter(Boolean).join(' ').toLowerCase().includes(term)
    })
  }, [sites, searchTerm, filtreClient])

  const ouvrirCreation = () => setFormModal({ mode: 'creer', valeurs: FORM_VIDE })

  const ouvrirEdition = (site) => setFormModal({
    mode: 'modifier',
    site,
    valeurs: {
      nom: site.nom, adresse: site.adresse ?? '',
      coordonneesGps: site.coordonneesGps ?? '', clientId: site.clientId ?? '',
    },
  })

  const handleChampChange = (champ, valeur) => {
    setFormModal((m) => ({ ...m, valeurs: { ...m.valeurs, [champ]: valeur } }))
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    const payload = { ...formModal.valeurs, clientId: Number(formModal.valeurs.clientId) }
    if (!payload.nom.trim() || !payload.clientId) return

    if (formModal.mode === 'creer') {
      creerSite.mutate(payload)
    } else {
      modifierSite.mutate({ id: formModal.site.id, payload })
    }
    setFormModal(null)
  }

  const confirmerSuppression = () => {
    if (!siteASupprimer) return
    supprimerSite.mutate(siteASupprimer.id)
    setSiteASupprimer(null)
  }

  if (isLoading) {
    return (
      <div className="site-container anim-fadein">
        <div className="site-card glass skeleton-card" />
      </div>
    )
  }

  return (
    <div className="site-container anim-fadein">
      <div className="site-toolbar">
        <input
          type="text"
          className="search-input"
          placeholder="🔍 Rechercher (nom, adresse, client)…"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <select value={filtreClient} onChange={(e) => setFiltreClient(e.target.value)} className="filtre-select">
          <option value="">Tous les clients</option>
          {clients.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
        </select>
        <button className="btn btn-primary" onClick={ouvrirCreation}>➕ Nouveau site</button>
      </div>

      <div className="site-card glass">
        {filtres.length === 0 ? (
          <div className="empty-state">
            {sites.length === 0 ? 'Aucun site enregistré.' : 'Aucun site ne correspond à ces critères.'}
          </div>
        ) : (
          <table className="gmao-table">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Client</th>
                <th>Adresse</th>
                <th>Coordonnées GPS</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtres.map((s) => (
                <tr key={s.id}>
                  <td><strong>{s.nom}</strong></td>
                  <td>{s.clientNom ?? '—'}</td>
                  <td className="cell-muted">{s.adresse || '—'}</td>
                  <td className="cell-muted">{s.coordonneesGps || '—'}</td>
                  <td className="col-actions">
                    <button className="btn-icon" title="Modifier" onClick={() => ouvrirEdition(s)}>✏️</button>
                    <button className="btn-icon btn-icon-danger" title="Supprimer" onClick={() => setSiteASupprimer(s)}>🗑️</button>
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
          title={formModal.mode === 'creer' ? 'Nouveau site' : 'Modifier le site'}
          icon="📍"
          onClose={() => setFormModal(null)}
        >
          <form onSubmit={handleSubmit} className="site-form">
            <label className="form-label">
              Nom <span className="required">*</span>
              <input type="text" required autoFocus className="modal-input"
                value={formModal.valeurs.nom}
                onChange={(e) => handleChampChange('nom', e.target.value)} />
            </label>
            <label className="form-label">
              Client <span className="required">*</span>
              <select required className="modal-input" value={formModal.valeurs.clientId}
                onChange={(e) => handleChampChange('clientId', e.target.value)}>
                <option value="">Sélectionner un client…</option>
                {clients.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
              </select>
            </label>
            <label className="form-label">
              Adresse
              <textarea className="modal-input" rows={2}
                value={formModal.valeurs.adresse}
                onChange={(e) => handleChampChange('adresse', e.target.value)} />
            </label>
            <label className="form-label">
              Coordonnées GPS
              <input type="text" className="modal-input" placeholder="ex: 48.8566,2.3522"
                value={formModal.valeurs.coordonneesGps}
                onChange={(e) => handleChampChange('coordonneesGps', e.target.value)} />
              <span className="form-hint">Format : latitude,longitude — utilisé par la carte de supervision</span>
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
      {siteASupprimer && (
        <Modal title="Supprimer le site" icon="⚠️" onClose={() => setSiteASupprimer(null)}>
          <p className="modal-context">
            Supprimer définitivement <strong>{siteASupprimer.nom}</strong> ? Cette action est irréversible.
            {' '}Si des équipements ou chantiers y sont encore rattachés, la suppression sera refusée.
          </p>
          <div className="modal-buttons">
            <button className="btn btn-cancel" onClick={() => setSiteASupprimer(null)}>Annuler</button>
            <button className="btn btn-danger" onClick={confirmerSuppression}>Supprimer</button>
          </div>
        </Modal>
      )}
    </div>
  )
}
