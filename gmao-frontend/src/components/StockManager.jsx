import React, { useMemo, useState } from 'react'
import {
  useCatalogue,
  useStockVehicules,
  useHistoriqueMouvements,
  useModifierSeuilAlerte,
  useApprovisionner,
  useTransferer,
  useAjusterStock,
} from '../hooks/useStock'
import Modal from './ui/Modal' // ⚠️ Ajuste le chemin selon l'emplacement réel de Modal.jsx dans ton projet
import './StockManager.css'

const TYPE_LABELS = {
  APPROVISIONNEMENT: '📦 Approvisionnement',
  CONSOMMATION: '🔧 Consommation',
  TRANSFERT_ENTRANT: '⬅️ Transfert reçu',
  TRANSFERT_SORTANT: '➡️ Transfert envoyé',
  AJUSTEMENT: '✏️ Ajustement',
}

function formatDateHeure(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('fr-FR', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

export default function StockManager() {
  const { data: catalogue = [], isLoading: loadingCatalogue } = useCatalogue()
  const { data: stockVehicules = [], isLoading: loadingStock } = useStockVehicules()
  const { data: historique = [], isLoading: loadingHistorique } = useHistoriqueMouvements()

  const modifierSeuil = useModifierSeuilAlerte()
  const approvisionner = useApprovisionner()
  const transferer = useTransferer()
  const ajuster = useAjusterStock()

  const [ongletActif, setOngletActif] = useState('vehicules') // 'catalogue' | 'vehicules' | 'historique'
  const [searchTerm, setSearchTerm] = useState('')
  const [seuilsEnEdition, setSeuilsEnEdition] = useState({}) // { [pieceId]: valeur en cours de saisie }

  const [formApprovisionnement, setFormApprovisionnement] = useState({
    technicienId: '', pieceId: '', quantite: '', commentaire: '',
  })
  const [formTransfert, setFormTransfert] = useState({
    technicienSourceId: '', technicienDestinationId: '', pieceId: '', quantite: '', commentaire: '',
  })
  const [formulaireOuvert, setFormulaireOuvert] = useState(null) // 'approvisionnement' | 'transfert' | null

  // Modale d'ajustement de quantité (remplace window.prompt)
  const [ajustementModal, setAjustementModal] = useState(null) // { ligne, valeur } | null

  // Modale de confirmation de changement de seuil (remplace l'enregistrement silencieux)
  const [seuilConfirmModal, setSeuilConfirmModal] = useState(null) // { piece, ancienneValeur, nouvelleValeur } | null

  // Techniciens connus, dérivés du stock existant (à remplacer par un vrai
  // GET /api/utilisateurs?role=TECHNICIEN si disponible côté back).
  const techniciensConnus = useMemo(() => {
    const map = new Map()
    stockVehicules.forEach((s) => map.set(s.technicienId, s.technicienNom))
    return Array.from(map.entries()).map(([id, nom]) => ({ id, nom }))
  }, [stockVehicules])

  // ── Groupement du stock par technicien ───────────────────────────────
  const stockGroupe = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    const filtered = stockVehicules.filter((s) => {
      if (!term) return true
      return [s.technicienNom, s.pieceNom, s.pieceReference]
        .filter(Boolean).join(' ').toLowerCase().includes(term)
    })

    const groupes = new Map()
    filtered.forEach((s) => {
      if (!groupes.has(s.technicienId)) {
        groupes.set(s.technicienId, { technicienId: s.technicienId, technicienNom: s.technicienNom, lignes: [] })
      }
      groupes.get(s.technicienId).lignes.push(s)
    })
    return Array.from(groupes.values())
  }, [stockVehicules, searchTerm])

  const catalogueFiltre = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    if (!term) return catalogue
    return catalogue.filter((p) =>
      [p.reference, p.nom].filter(Boolean).join(' ').toLowerCase().includes(term)
    )
  }, [catalogue, searchTerm])

  const kpis = useMemo(() => {
    const valeurTotale = stockVehicules.reduce((acc, s) => acc + (s.valeurStock ?? 0), 0)
    const nbCritiques = stockVehicules.filter((s) => s.critique).length
    return { valeurTotale, nbCritiques }
  }, [stockVehicules])

  // ── Handlers ─────────────────────────────────────────────────────────

  const handleSeuilChange = (pieceId, value) => {
    setSeuilsEnEdition((prev) => ({ ...prev, [pieceId]: value }))
  }

  /** Ouvre la modale de confirmation si la valeur a réellement changé. */
  const handleSeuilValider = (piece) => {
    const brut = seuilsEnEdition[piece.id]
    if (brut === undefined) return // rien saisi, ne rien faire

    const nouvelleValeur = Number(brut)
    setSeuilsEnEdition((prev) => {
      const next = { ...prev }
      delete next[piece.id]
      return next
    })

    if (Number.isNaN(nouvelleValeur) || nouvelleValeur < 0) return
    if (nouvelleValeur === piece.seuilAlerte) return // pas de changement réel

    setSeuilConfirmModal({ piece, ancienneValeur: piece.seuilAlerte, nouvelleValeur })
  }

  const confirmerChangementSeuil = () => {
    if (!seuilConfirmModal) return
    modifierSeuil.mutate({
      pieceId: seuilConfirmModal.piece.id,
      seuilAlerte: seuilConfirmModal.nouvelleValeur,
    })
    setSeuilConfirmModal(null)
  }

  const handleSubmitApprovisionnement = (e) => {
    e.preventDefault()
    approvisionner.mutate({
      technicienId: Number(formApprovisionnement.technicienId),
      pieceId: Number(formApprovisionnement.pieceId),
      quantite: Number(formApprovisionnement.quantite),
      commentaire: formApprovisionnement.commentaire || undefined,
    })
    setFormApprovisionnement({ technicienId: '', pieceId: '', quantite: '', commentaire: '' })
    setFormulaireOuvert(null)
  }

  const handleSubmitTransfert = (e) => {
    e.preventDefault()
    transferer.mutate({
      technicienSourceId: Number(formTransfert.technicienSourceId),
      technicienDestinationId: Number(formTransfert.technicienDestinationId),
      pieceId: Number(formTransfert.pieceId),
      quantite: Number(formTransfert.quantite),
      commentaire: formTransfert.commentaire || undefined,
    })
    setFormTransfert({ technicienSourceId: '', technicienDestinationId: '', pieceId: '', quantite: '', commentaire: '' })
    setFormulaireOuvert(null)
  }

  const handleAjustementRapide = (ligne) => {
    setAjustementModal({ ligne, valeur: String(ligne.quantite) })
  }

  const confirmerAjustement = () => {
    if (!ajustementModal) return
    const nouvelleQuantite = Number(ajustementModal.valeur)
    if (Number.isNaN(nouvelleQuantite) || nouvelleQuantite < 0) return

    ajuster.mutate({
      technicienId: ajustementModal.ligne.technicienId,
      pieceId: ajustementModal.ligne.pieceId,
      nouvelleQuantite,
    })
    setAjustementModal(null)
  }

  const isLoading = loadingCatalogue || loadingStock

  if (isLoading) {
    return (
      <div className="stock-container anim-fadein">
        <div className="stock-card glass skeleton-card" />
        <div className="stock-card glass skeleton-card" />
      </div>
    )
  }

  return (
    <div className="stock-container anim-fadein">
      {/* ── KPIs ────────────────────────────────────────────────────── */}
      <div className="stock-kpis">
        <div className="kpi-chip">
          💰 Valeur stock embarqué : <strong>{kpis.valeurTotale.toFixed(2)} €</strong>
        </div>
        <div className={`kpi-chip ${kpis.nbCritiques > 0 ? 'kpi-alert' : ''}`}>
          ⚠️ {kpis.nbCritiques} référence(s) en stock critique
        </div>
      </div>

      {/* ── Onglets + recherche ───────────────────────────────────────── */}
      <div className="stock-toolbar">
        <div className="stock-tabs">
          <button className={ongletActif === 'catalogue' ? 'tab-active' : ''} onClick={() => setOngletActif('catalogue')}>
            🗂️ Catalogue
          </button>
          <button className={ongletActif === 'vehicules' ? 'tab-active' : ''} onClick={() => setOngletActif('vehicules')}>
            🚚 Stock véhicules
          </button>
          <button className={ongletActif === 'historique' ? 'tab-active' : ''} onClick={() => setOngletActif('historique')}>
            📜 Historique
          </button>
        </div>
        {ongletActif !== 'historique' && (
          <input
            type="text"
            className="search-input"
            placeholder="🔍 Rechercher…"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        )}
      </div>

      {/* ── Onglet Catalogue ─────────────────────────────────────────── */}
      {ongletActif === 'catalogue' && (
        <div className="stock-card glass">
          <h3 className="stock-title">🗂️ Catalogue Général des Pièces</h3>
          <table className="gmao-table">
            <thead>
              <tr>
                <th>Référence</th>
                <th>Nom de la pièce</th>
                <th>Prix Unitaire</th>
                <th>Seuil d'alerte</th>
              </tr>
            </thead>
            <tbody>
              {catalogueFiltre.map((piece) => (
                <tr key={piece.id}>
                  <td><code>{piece.reference}</code></td>
                  <td>{piece.nom}</td>
                  <td>{piece.prixUnitaire?.toFixed(2)} €</td>
                  <td>
                    <input
                      type="number"
                      min="0"
                      className="seuil-input"
                      value={seuilsEnEdition[piece.id] ?? piece.seuilAlerte}
                      onChange={(e) => handleSeuilChange(piece.id, e.target.value)}
                      onBlur={() => handleSeuilValider(piece)}
                      onKeyDown={(e) => e.key === 'Enter' && e.target.blur()}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ── Onglet Stock véhicules ───────────────────────────────────── */}
      {ongletActif === 'vehicules' && (
        <>
          <div className="stock-actions">
            <button className="btn btn-primary" onClick={() => setFormulaireOuvert('approvisionnement')}>
              📦 Approvisionner
            </button>
            <button className="btn btn-secondary" onClick={() => setFormulaireOuvert('transfert')}>
              🔄 Transférer
            </button>
          </div>

          {formulaireOuvert === 'approvisionnement' && (
            <form className="stock-card glass inline-form" onSubmit={handleSubmitApprovisionnement}>
              <h4>📦 Approvisionner un véhicule</h4>
              <div className="form-row">
                <select required value={formApprovisionnement.technicienId}
                  onChange={(e) => setFormApprovisionnement((f) => ({ ...f, technicienId: e.target.value }))}>
                  <option value="">Technicien…</option>
                  {techniciensConnus.map((t) => <option key={t.id} value={t.id}>{t.nom}</option>)}
                </select>
                <select required value={formApprovisionnement.pieceId}
                  onChange={(e) => setFormApprovisionnement((f) => ({ ...f, pieceId: e.target.value }))}>
                  <option value="">Pièce…</option>
                  {catalogue.map((p) => <option key={p.id} value={p.id}>{p.nom}</option>)}
                </select>
                <input type="number" min="1" required placeholder="Quantité"
                  value={formApprovisionnement.quantite}
                  onChange={(e) => setFormApprovisionnement((f) => ({ ...f, quantite: e.target.value }))} />
              </div>
              <input type="text" placeholder="Commentaire (optionnel)" className="form-comment"
                value={formApprovisionnement.commentaire}
                onChange={(e) => setFormApprovisionnement((f) => ({ ...f, commentaire: e.target.value }))} />
              <div className="form-buttons">
                <button type="submit" className="btn btn-primary">Valider</button>
                <button type="button" className="btn btn-cancel" onClick={() => setFormulaireOuvert(null)}>Annuler</button>
              </div>
            </form>
          )}

          {formulaireOuvert === 'transfert' && (
            <form className="stock-card glass inline-form" onSubmit={handleSubmitTransfert}>
              <h4>🔄 Transférer entre techniciens</h4>
              <div className="form-row">
                <select required value={formTransfert.technicienSourceId}
                  onChange={(e) => setFormTransfert((f) => ({ ...f, technicienSourceId: e.target.value }))}>
                  <option value="">Depuis…</option>
                  {techniciensConnus.map((t) => <option key={t.id} value={t.id}>{t.nom}</option>)}
                </select>
                <select required value={formTransfert.technicienDestinationId}
                  onChange={(e) => setFormTransfert((f) => ({ ...f, technicienDestinationId: e.target.value }))}>
                  <option value="">Vers…</option>
                  {techniciensConnus.map((t) => <option key={t.id} value={t.id}>{t.nom}</option>)}
                </select>
              </div>
              <div className="form-row">
                <select required value={formTransfert.pieceId}
                  onChange={(e) => setFormTransfert((f) => ({ ...f, pieceId: e.target.value }))}>
                  <option value="">Pièce…</option>
                  {catalogue.map((p) => <option key={p.id} value={p.id}>{p.nom}</option>)}
                </select>
                <input type="number" min="1" required placeholder="Quantité"
                  value={formTransfert.quantite}
                  onChange={(e) => setFormTransfert((f) => ({ ...f, quantite: e.target.value }))} />
              </div>
              <input type="text" placeholder="Commentaire (optionnel)" className="form-comment"
                value={formTransfert.commentaire}
                onChange={(e) => setFormTransfert((f) => ({ ...f, commentaire: e.target.value }))} />
              <div className="form-buttons">
                <button type="submit" className="btn btn-primary">Valider</button>
                <button type="button" className="btn btn-cancel" onClick={() => setFormulaireOuvert(null)}>Annuler</button>
              </div>
            </form>
          )}

          {stockGroupe.length === 0 ? (
            <div className="empty-state">Aucun stock ne correspond à cette recherche.</div>
          ) : (
            stockGroupe.map((groupe) => (
              <div key={groupe.technicienId} className="stock-card glass">
                <h3 className="stock-title">🧑‍🔧 {groupe.technicienNom}</h3>
                <table className="gmao-table">
                  <thead>
                    <tr>
                      <th>Pièce Détachée</th>
                      <th>Quantité</th>
                      <th>Valeur</th>
                      <th>Statut</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {groupe.lignes.map((ligne) => (
                      <tr key={ligne.id}>
                        <td>{ligne.pieceNom} <span className="cell-muted">({ligne.pieceReference})</span></td>
                        <td><strong>{ligne.quantite} u</strong></td>
                        <td>{ligne.valeurStock?.toFixed(2)} €</td>
                        <td>
                          {ligne.critique ? (
                            <span className="alert-badge">⚠️ Stock Critique</span>
                          ) : (
                            <span style={{ color: '#10b981', fontSize: '0.8rem' }}>🟢 OK</span>
                          )}
                        </td>
                        <td>
                          <button className="btn-icon" title="Ajuster (inventaire)" onClick={() => handleAjustementRapide(ligne)}>
                            ✏️
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ))
          )}
        </>
      )}

      {/* ── Onglet Historique ────────────────────────────────────────── */}
      {ongletActif === 'historique' && (
        <div className="stock-card glass">
          <h3 className="stock-title">📜 Historique des Mouvements</h3>
          {loadingHistorique ? (
            <p className="cell-muted">Chargement…</p>
          ) : historique.length === 0 ? (
            <div className="empty-state">Aucun mouvement enregistré.</div>
          ) : (
            <table className="gmao-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Pièce</th>
                  <th>Technicien</th>
                  <th>Quantité</th>
                  <th>Détail</th>
                </tr>
              </thead>
              <tbody>
                {historique.map((m) => (
                  <tr key={m.id}>
                    <td className="cell-muted">{formatDateHeure(m.createdAt)}</td>
                    <td>{TYPE_LABELS[m.typeMouvement] ?? m.typeMouvement}</td>
                    <td>{m.pieceNom} <span className="cell-muted">({m.pieceReference})</span></td>
                    <td>
                      {m.technicienNom}
                      {m.technicienSourceNom && <span className="cell-muted"> ← {m.technicienSourceNom}</span>}
                    </td>
                    <td>{m.quantite}</td>
                    <td className="cell-muted">
                      {m.interventionTitre && `Intervention : ${m.interventionTitre}`}
                      {m.commentaire && ` ${m.commentaire}`}
                      {m.utilisateurNom && ` — par ${m.utilisateurNom}`}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* ── Modale : ajustement de quantité ───────────────────────────── */}
      {ajustementModal && (
        <Modal
          title="Ajuster la quantité"
          icon="✏️"
          onClose={() => setAjustementModal(null)}
        >
          <p className="modal-context">
            <strong>{ajustementModal.ligne.pieceNom}</strong> — véhicule de{' '}
            <strong>{ajustementModal.ligne.technicienNom}</strong>
          </p>
          <input
            type="number"
            min="0"
            autoFocus
            className="modal-input"
            value={ajustementModal.valeur}
            onChange={(e) => setAjustementModal((m) => ({ ...m, valeur: e.target.value }))}
            onKeyDown={(e) => e.key === 'Enter' && confirmerAjustement()}
          />
          <div className="modal-buttons">
            <button className="btn btn-cancel" onClick={() => setAjustementModal(null)}>Annuler</button>
            <button className="btn btn-primary" onClick={confirmerAjustement}>Valider</button>
          </div>
        </Modal>
      )}

      {/* ── Modale : confirmation de changement de seuil ──────────────── */}
      {seuilConfirmModal && (
        <Modal
          title="Confirmer le nouveau seuil"
          icon="⚠️"
          onClose={() => setSeuilConfirmModal(null)}
        >
          <p className="modal-context">
            Modifier le seuil d'alerte de <strong>{seuilConfirmModal.piece.nom}</strong> ({seuilConfirmModal.piece.reference}) ?
          </p>
          <div className="modal-diff">
            <span className="val-avant">{seuilConfirmModal.ancienneValeur}</span>
            <span>→</span>
            <span className="val-apres">{seuilConfirmModal.nouvelleValeur}</span>
          </div>
          <div className="modal-buttons">
            <button className="btn btn-cancel" onClick={() => setSeuilConfirmModal(null)}>Annuler</button>
            <button className="btn btn-primary" onClick={confirmerChangementSeuil}>Confirmer</button>
          </div>
        </Modal>
      )}
    </div>
  )
}
