import React, { useMemo, useState } from 'react'
import {
  useUtilisateurs,
  useCreerUtilisateur,
  useModifierUtilisateur,
  useChangerActifUtilisateur,
  useChangerMotDePasse,
} from '../hooks/useUtilisateurs'
import Modal from './ui/Modal'
import './UtilisateurManager.css'

const FORM_CREATION_VIDE = { nom: '', email: '', motDePasse: '', role: 'TECHNICIEN' }
const FORM_EDITION_VIDE = { nom: '', email: '', role: 'TECHNICIEN' }

const ROLE_LABELS = { MANAGER: 'Manager', TECHNICIEN: 'Technicien' }

export default function UtilisateurManager() {
  const [filtreRole, setFiltreRole] = useState('')
  const { data: utilisateurs = [], isLoading } = useUtilisateurs(filtreRole || undefined)

  const creerUtilisateur = useCreerUtilisateur()
  const modifierUtilisateur = useModifierUtilisateur()
  const changerActif = useChangerActifUtilisateur()
  const changerMotDePasse = useChangerMotDePasse()

  const [searchTerm, setSearchTerm] = useState('')
  const [formCreation, setFormCreation] = useState(null) // valeurs | null
  const [formEdition, setFormEdition] = useState(null) // { utilisateur, valeurs } | null
  const [modalMotDePasse, setModalMotDePasse] = useState(null) // { utilisateur, valeur } | null

  const filtres = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    if (!term) return utilisateurs
    return utilisateurs.filter((u) => [u.nom, u.email].join(' ').toLowerCase().includes(term))
  }, [utilisateurs, searchTerm])

  // ── Création ─────────────────────────────────────────────────────────
  const handleSubmitCreation = (e) => {
    e.preventDefault()
    if (!formCreation.nom.trim() || !formCreation.email.trim() || formCreation.motDePasse.length < 8) return
    creerUtilisateur.mutate(formCreation)
    setFormCreation(null)
  }

  // ── Édition ──────────────────────────────────────────────────────────
  const ouvrirEdition = (u) => setFormEdition({ utilisateur: u, valeurs: { nom: u.nom, email: u.email, role: u.role } })

  const handleSubmitEdition = (e) => {
    e.preventDefault()
    modifierUtilisateur.mutate({ id: formEdition.utilisateur.id, payload: formEdition.valeurs })
    setFormEdition(null)
  }

  // ── Activation / désactivation ───────────────────────────────────────
  const toggleActif = (u) => changerActif.mutate({ id: u.id, actif: !u.actif })

  // ── Mot de passe ─────────────────────────────────────────────────────
  const handleSubmitMotDePasse = (e) => {
    e.preventDefault()
    if (modalMotDePasse.valeur.length < 8) return
    changerMotDePasse.mutate({ id: modalMotDePasse.utilisateur.id, nouveauMotDePasse: modalMotDePasse.valeur })
    setModalMotDePasse(null)
  }

  if (isLoading) {
    return (
      <div className="user-container anim-fadein">
        <div className="user-card glass skeleton-card" />
      </div>
    )
  }

  return (
    <div className="user-container anim-fadein">
      <div className="user-toolbar">
        <input
          type="text"
          className="search-input"
          placeholder="🔍 Rechercher (nom, email)…"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <select value={filtreRole} onChange={(e) => setFiltreRole(e.target.value)} className="filtre-select">
          <option value="">Tous les rôles</option>
          <option value="MANAGER">Manager</option>
          <option value="TECHNICIEN">Technicien</option>
        </select>
        <button className="btn btn-primary" onClick={() => setFormCreation(FORM_CREATION_VIDE)}>
          ➕ Nouvel utilisateur
        </button>
      </div>

      <div className="user-card glass">
        {filtres.length === 0 ? (
          <div className="empty-state">
            {utilisateurs.length === 0 ? 'Aucun utilisateur enregistré.' : 'Aucun utilisateur ne correspond à cette recherche.'}
          </div>
        ) : (
          <table className="gmao-table">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Email</th>
                <th>Rôle</th>
                <th>Statut</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtres.map((u) => (
                <tr key={u.id} className={!u.actif ? 'row-inactive' : ''}>
                  <td><strong>{u.nom}</strong></td>
                  <td className="cell-muted">{u.email}</td>
                  <td>
                    <span className={`badge-role badge-role-${u.role.toLowerCase()}`}>{ROLE_LABELS[u.role]}</span>
                  </td>
                  <td>
                    <button
                      className={`badge-actif ${u.actif ? 'badge-actif-oui' : 'badge-actif-non'}`}
                      onClick={() => toggleActif(u)}
                      title={u.actif ? 'Cliquer pour désactiver' : 'Cliquer pour activer'}
                    >
                      {u.actif ? '🟢 Actif' : '⛔ Désactivé'}
                    </button>
                  </td>
                  <td className="col-actions">
                    <button className="btn-icon" title="Modifier" onClick={() => ouvrirEdition(u)}>✏️</button>
                    <button
                      className="btn-icon"
                      title="Changer le mot de passe"
                      onClick={() => setModalMotDePasse({ utilisateur: u, valeur: '' })}
                    >
                      🔑
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* ── Modale création ─────────────────────────────────────────── */}
      {formCreation && (
        <Modal title="Nouvel utilisateur" icon="👤" onClose={() => setFormCreation(null)}>
          <form onSubmit={handleSubmitCreation} className="user-form">
            <label className="form-label">
              Nom <span className="required">*</span>
              <input
                type="text" required autoFocus className="modal-input"
                value={formCreation.nom}
                onChange={(e) => setFormCreation((f) => ({ ...f, nom: e.target.value }))}
              />
            </label>
            <label className="form-label">
              Email <span className="required">*</span>
              <input
                type="email" required className="modal-input"
                value={formCreation.email}
                onChange={(e) => setFormCreation((f) => ({ ...f, email: e.target.value }))}
              />
            </label>
            <label className="form-label">
              Mot de passe <span className="required">*</span>
              <input
                type="password" required minLength={8} className="modal-input"
                value={formCreation.motDePasse}
                onChange={(e) => setFormCreation((f) => ({ ...f, motDePasse: e.target.value }))}
              />
              <span className="form-hint">8 caractères minimum</span>
            </label>
            <label className="form-label">
              Rôle
              <select
                className="modal-input"
                value={formCreation.role}
                onChange={(e) => setFormCreation((f) => ({ ...f, role: e.target.value }))}
              >
                <option value="TECHNICIEN">Technicien</option>
                <option value="MANAGER">Manager</option>
              </select>
            </label>
            <div className="modal-buttons">
              <button type="button" className="btn btn-cancel" onClick={() => setFormCreation(null)}>Annuler</button>
              <button type="submit" className="btn btn-primary">Créer</button>
            </div>
          </form>
        </Modal>
      )}

      {/* ── Modale édition ──────────────────────────────────────────── */}
      {formEdition && (
        <Modal title="Modifier l'utilisateur" icon="✏️" onClose={() => setFormEdition(null)}>
          <form onSubmit={handleSubmitEdition} className="user-form">
            <label className="form-label">
              Nom <span className="required">*</span>
              <input
                type="text" required autoFocus className="modal-input"
                value={formEdition.valeurs.nom}
                onChange={(e) => setFormEdition((f) => ({ ...f, valeurs: { ...f.valeurs, nom: e.target.value } }))}
              />
            </label>
            <label className="form-label">
              Email <span className="required">*</span>
              <input
                type="email" required className="modal-input"
                value={formEdition.valeurs.email}
                onChange={(e) => setFormEdition((f) => ({ ...f, valeurs: { ...f.valeurs, email: e.target.value } }))}
              />
            </label>
            <label className="form-label">
              Rôle
              <select
                className="modal-input"
                value={formEdition.valeurs.role}
                onChange={(e) => setFormEdition((f) => ({ ...f, valeurs: { ...f.valeurs, role: e.target.value } }))}
              >
                <option value="TECHNICIEN">Technicien</option>
                <option value="MANAGER">Manager</option>
              </select>
            </label>
            <div className="modal-buttons">
              <button type="button" className="btn btn-cancel" onClick={() => setFormEdition(null)}>Annuler</button>
              <button type="submit" className="btn btn-primary">Enregistrer</button>
            </div>
          </form>
        </Modal>
      )}

      {/* ── Modale changement de mot de passe ───────────────────────── */}
      {modalMotDePasse && (
        <Modal title="Changer le mot de passe" icon="🔑" onClose={() => setModalMotDePasse(null)}>
          <form onSubmit={handleSubmitMotDePasse} className="user-form">
            <p className="modal-context">
              Nouveau mot de passe pour <strong>{modalMotDePasse.utilisateur.nom}</strong>
            </p>
            <label className="form-label">
              Nouveau mot de passe <span className="required">*</span>
              <input
                type="password" required minLength={8} autoFocus className="modal-input"
                value={modalMotDePasse.valeur}
                onChange={(e) => setModalMotDePasse((m) => ({ ...m, valeur: e.target.value }))}
              />
              <span className="form-hint">8 caractères minimum</span>
            </label>
            <div className="modal-buttons">
              <button type="button" className="btn btn-cancel" onClick={() => setModalMotDePasse(null)}>Annuler</button>
              <button type="submit" className="btn btn-primary">Valider</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
