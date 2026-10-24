import React, { useMemo, useState } from 'react'
import { useClients, useCreerClient, useModifierClient, useSupprimerClient } from '../hooks/useClients'
import Modal from './ui/Modal' // ⚠️ Même hypothèse de chemin que pour StockManager
import './ClientManager.css'

const FORM_VIDE = { nom: '', adresse: '', telephone: '', email: '' }

export default function ClientManager() {
  const { data: clients = [], isLoading } = useClients()
  const creerClient = useCreerClient()
  const modifierClient = useModifierClient()
  const supprimerClient = useSupprimerClient()

  const [searchTerm, setSearchTerm] = useState('')
  const [formModal, setFormModal] = useState(null) // { mode: 'creer' | 'modifier', client?, valeurs } | null
  const [clientASupprimer, setClientASupprimer] = useState(null)

  const filtres = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    if (!term) return clients
    return clients.filter((c) =>
      [c.nom, c.email, c.telephone, c.adresse].filter(Boolean).join(' ').toLowerCase().includes(term)
    )
  }, [clients, searchTerm])

  const ouvrirCreation = () => setFormModal({ mode: 'creer', valeurs: FORM_VIDE })

  const ouvrirEdition = (client) => setFormModal({
    mode: 'modifier',
    client,
    valeurs: { nom: client.nom, adresse: client.adresse ?? '', telephone: client.telephone ?? '', email: client.email ?? '' },
  })

  const handleChampChange = (champ, valeur) => {
    setFormModal((m) => ({ ...m, valeurs: { ...m.valeurs, [champ]: valeur } }))
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!formModal.valeurs.nom.trim()) return

    if (formModal.mode === 'creer') {
      creerClient.mutate(formModal.valeurs)
    } else {
      modifierClient.mutate({ id: formModal.client.id, payload: formModal.valeurs })
    }
    setFormModal(null)
  }

  const confirmerSuppression = () => {
    if (!clientASupprimer) return
    supprimerClient.mutate(clientASupprimer.id)
    setClientASupprimer(null)
  }

  if (isLoading) {
    return (
      <div className="client-container anim-fadein">
        <div className="client-card glass skeleton-card" />
      </div>
    )
  }

  return (
    <div className="client-container anim-fadein">
      <div className="client-toolbar">
        <input
          type="text"
          className="search-input"
          placeholder="🔍 Rechercher (nom, email, téléphone)…"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <button className="btn btn-primary" onClick={ouvrirCreation}>➕ Nouveau client</button>
      </div>

      <div className="client-card glass">
        {filtres.length === 0 ? (
          <div className="empty-state">
            {clients.length === 0 ? 'Aucun client enregistré.' : 'Aucun client ne correspond à cette recherche.'}
          </div>
        ) : (
          <table className="gmao-table">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Adresse</th>
                <th>Téléphone</th>
                <th>Email</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtres.map((client) => (
                <tr key={client.id}>
                  <td><strong>{client.nom}</strong></td>
                  <td className="cell-muted">{client.adresse || '—'}</td>
                  <td>{client.telephone || '—'}</td>
                  <td>{client.email || '—'}</td>
                  <td className="col-actions">
                    <button className="btn-icon" title="Modifier" onClick={() => ouvrirEdition(client)}>✏️</button>
                    <button className="btn-icon btn-icon-danger" title="Supprimer" onClick={() => setClientASupprimer(client)}>🗑️</button>
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
          title={formModal.mode === 'creer' ? 'Nouveau client' : 'Modifier le client'}
          icon="💼"
          onClose={() => setFormModal(null)}
        >
          <form onSubmit={handleSubmit} className="client-form">
            <label className="form-label">
              Nom <span className="required">*</span>
              <input
                type="text"
                required
                className="modal-input"
                value={formModal.valeurs.nom}
                onChange={(e) => handleChampChange('nom', e.target.value)}
                autoFocus
              />
            </label>
            <label className="form-label">
              Adresse
              <textarea
                className="modal-input"
                rows={2}
                value={formModal.valeurs.adresse}
                onChange={(e) => handleChampChange('adresse', e.target.value)}
              />
            </label>
            <label className="form-label">
              Téléphone
              <input
                type="tel"
                className="modal-input"
                value={formModal.valeurs.telephone}
                onChange={(e) => handleChampChange('telephone', e.target.value)}
              />
            </label>
            <label className="form-label">
              Email
              <input
                type="email"
                className="modal-input"
                value={formModal.valeurs.email}
                onChange={(e) => handleChampChange('email', e.target.value)}
              />
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

      {/* ── Modale confirmation suppression ─────────────────────────── */}
      {clientASupprimer && (
        <Modal title="Supprimer le client" icon="⚠️" onClose={() => setClientASupprimer(null)}>
          <p className="modal-context">
            Supprimer définitivement <strong>{clientASupprimer.nom}</strong> ? Cette action est irréversible.
            {' '}Si des sites sont encore rattachés à ce client, la suppression sera refusée.
          </p>
          <div className="modal-buttons">
            <button className="btn btn-cancel" onClick={() => setClientASupprimer(null)}>Annuler</button>
            <button className="btn btn-danger" onClick={confirmerSuppression}>Supprimer</button>
          </div>
        </Modal>
      )}
    </div>
  )
}
