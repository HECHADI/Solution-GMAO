import React, { useMemo, useState } from 'react'
import {
  useInterventionsAFacturer,
  useFactures,
  useGenererFacture,
  useMarquerEnvoyee,
  useMarquerPayee,
  useAnnulerFacture,
} from '../hooks/useFacturation'
import { telechargerFacturePdf } from '../api/factureApi'
import Modal from './ui/Modal'
import './BillingManager.css'

const STATUT_LABELS = {
  BROUILLON: { label: '📝 Brouillon', className: 'statut-brouillon' },
  ENVOYEE: { label: '📤 Envoyée', className: 'statut-envoyee' },
  PAYEE: { label: '✅ Payée', className: 'statut-payee' },
  ANNULEE: { label: '❌ Annulée', className: 'statut-annulee' },
}

function formatMontant(valeur) {
  const nombre = Number(valeur ?? 0)
  return `${nombre.toFixed(2)} €`
}

function formatDate(date) {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('fr-FR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
  })
}

export default function BillingManager() {
  const { data: interventionsAFacturer = [], isLoading: loadingAFacturer } = useInterventionsAFacturer()
  const { data: factures = [], isLoading: loadingFactures } = useFactures()

  const genererFacture = useGenererFacture()
  const marquerEnvoyee = useMarquerEnvoyee()
  const marquerPayee = useMarquerPayee()
  const annulerFacture = useAnnulerFacture()

  const [ongletActif, setOngletActif] = useState('a-facturer') // 'a-facturer' | 'factures'
  const [searchTerm, setSearchTerm] = useState('')
  const [selectionInterventions, setSelectionInterventions] = useState([]) // ids cochés
  const [genererModalOuverte, setGenererModalOuverte] = useState(false)
  const [factureDetailOuverte, setFactureDetailOuverte] = useState(null) // facture | null
  const [confirmAnnulation, setConfirmAnnulation] = useState(null) // facture | null
  const [telechargementEnCours, setTelechargementEnCours] = useState(null) // id de la facture | null

  const handleTelechargerPdf = async (facture) => {
    setTelechargementEnCours(facture.id)
    try {
      await telechargerFacturePdf(facture.id, facture.numero)
    } catch (err) {
      console.error('Échec du téléchargement du PDF', err)
    } finally {
      setTelechargementEnCours(null)
    }
  }

  // ── Filtrage ────────────────────────────────────────────────────────
  const interventionsFiltrees = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    if (!term) return interventionsAFacturer
    return interventionsAFacturer.filter((i) =>
      [i.titre, i.clientNom, i.chantierNom, i.equipementNom]
        .filter(Boolean).join(' ').toLowerCase().includes(term)
    )
  }, [interventionsAFacturer, searchTerm])

  const facturesFiltrees = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    if (!term) return factures
    return factures.filter((f) =>
      [f.numero, f.clientNom, f.chantierNom]
        .filter(Boolean).join(' ').toLowerCase().includes(term)
    )
  }, [factures, searchTerm])

  // ── KPIs ────────────────────────────────────────────────────────────
  const kpis = useMemo(() => {
    const totalAFacturer = interventionsAFacturer.reduce(
      (acc, i) => acc + Number(i.coutTotalEstime ?? 0), 0)
    const totalEnAttentePaiement = factures
      .filter((f) => f.statut === 'ENVOYEE')
      .reduce((acc, f) => acc + Number(f.montantTotal ?? 0), 0)
    const totalPaye = factures
      .filter((f) => f.statut === 'PAYEE')
      .reduce((acc, f) => acc + Number(f.montantTotal ?? 0), 0)
    return { totalAFacturer, totalEnAttentePaiement, totalPaye }
  }, [interventionsAFacturer, factures])

  // ── Sélection multi pour génération de facture ────────────────────
  const toggleSelection = (id) => {
    setSelectionInterventions((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id])
  }

  const interventionsSelectionnees = useMemo(
    () => interventionsAFacturer.filter((i) => selectionInterventions.includes(i.id)),
    [interventionsAFacturer, selectionInterventions]
  )

  const clientUnique = useMemo(() => {
    const clientIds = new Set(interventionsSelectionnees.map((i) => i.clientId))
    return clientIds.size === 1 ? interventionsSelectionnees[0] : null
  }, [interventionsSelectionnees])

  const totalSelection = useMemo(
    () => interventionsSelectionnees.reduce((acc, i) => acc + Number(i.coutTotalEstime ?? 0), 0),
    [interventionsSelectionnees]
  )

  const ouvrirGenerationFacture = () => {
    if (selectionInterventions.length === 0) return
    setGenererModalOuverte(true)
  }

  const confirmerGenerationFacture = () => {
    if (!clientUnique) return
    genererFacture.mutate({
      clientId: clientUnique.clientId,
      chantierId: clientUnique.chantierId ?? null,
      interventionIds: selectionInterventions,
    })
    setSelectionInterventions([])
    setGenererModalOuverte(false)
  }

  const isLoading = ongletActif === 'a-facturer' ? loadingAFacturer : loadingFactures

  return (
    <div className="billing-container anim-fadein">
      {/* ── KPIs ─────────────────────────────────────────────────────── */}
      <div className="billing-kpis">
        <div className="kpi-chip kpi-alert">
          🧾 À facturer : <strong>{formatMontant(kpis.totalAFacturer)}</strong>
        </div>
        <div className="kpi-chip">
          📤 En attente de paiement : <strong>{formatMontant(kpis.totalEnAttentePaiement)}</strong>
        </div>
        <div className="kpi-chip kpi-success">
          ✅ Encaissé : <strong>{formatMontant(kpis.totalPaye)}</strong>
        </div>
      </div>

      {/* ── Onglets + recherche ──────────────────────────────────────── */}
      <div className="billing-toolbar">
        <div className="billing-tabs">
          <button
            className={ongletActif === 'a-facturer' ? 'tab-active' : ''}
            onClick={() => setOngletActif('a-facturer')}
          >
            🧾 À facturer ({interventionsAFacturer.length})
          </button>
          <button
            className={ongletActif === 'factures' ? 'tab-active' : ''}
            onClick={() => setOngletActif('factures')}
          >
            📄 Factures émises ({factures.length})
          </button>
        </div>
        <input
          type="text"
          className="search-input"
          placeholder="🔍 Rechercher (client, chantier, équipement)…"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {isLoading ? (
        <div className="billing-card glass skeleton-card" />
      ) : (
        <>
          {/* ── Onglet À facturer ─────────────────────────────────────── */}
          {ongletActif === 'a-facturer' && (
            <>
              {selectionInterventions.length > 0 && (
                <div className="selection-bar glass">
                  <span>
                    {selectionInterventions.length} intervention(s) sélectionnée(s) —{' '}
                    <strong>{formatMontant(totalSelection)}</strong>
                    {!clientUnique && interventionsSelectionnees.length > 1 && (
                      <span className="selection-warning"> (clients différents — sélectionne un seul client)</span>
                    )}
                  </span>
                  <div className="selection-actions">
                    <button className="btn btn-cancel" onClick={() => setSelectionInterventions([])}>
                      Annuler la sélection
                    </button>
                    <button
                      className="btn btn-primary"
                      disabled={!clientUnique}
                      onClick={ouvrirGenerationFacture}
                    >
                      🧾 Générer la facture
                    </button>
                  </div>
                </div>
              )}

              {interventionsFiltrees.length === 0 ? (
                <div className="empty-state">
                  {searchTerm
                    ? 'Aucune intervention à facturer ne correspond à cette recherche.'
                    : 'Aucune intervention clôturée en attente de facturation.'}
                </div>
              ) : (
                <div className="billing-card glass">
                  <table className="gmao-table">
                    <thead>
                      <tr>
                        <th></th>
                        <th>Intervention</th>
                        <th>Client</th>
                        <th>Chantier</th>
                        <th>Réalisée le</th>
                        <th>Pièces</th>
                        <th>Main d'œuvre</th>
                        <th>Total estimé</th>
                      </tr>
                    </thead>
                    <tbody>
                      {interventionsFiltrees.map((interv) => (
                        <tr key={interv.id}>
                          <td>
                            <input
                              type="checkbox"
                              checked={selectionInterventions.includes(interv.id)}
                              onChange={() => toggleSelection(interv.id)}
                            />
                          </td>
                          <td>{interv.titre}</td>
                          <td>{interv.clientNom ?? '—'}</td>
                          <td>{interv.chantierNom ?? '—'}</td>
                          <td className="cell-muted">{formatDate(interv.dateRealisation)}</td>
                          <td>{formatMontant(interv.coutPiecesEstime)}</td>
                          <td>{formatMontant(interv.coutMainOeuvreEstime)}</td>
                          <td><strong>{formatMontant(interv.coutTotalEstime)}</strong></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )}

          {/* ── Onglet Factures émises ───────────────────────────────── */}
          {ongletActif === 'factures' && (
            facturesFiltrees.length === 0 ? (
              <div className="empty-state">
                {searchTerm ? 'Aucune facture ne correspond à cette recherche.' : 'Aucune facture émise pour le moment.'}
              </div>
            ) : (
              <div className="billing-card glass">
                <table className="gmao-table">
                  <thead>
                    <tr>
                      <th>N° Facture</th>
                      <th>Client</th>
                      <th>Chantier</th>
                      <th>Émise le</th>
                      <th>Échéance</th>
                      <th>Montant</th>
                      <th>Statut</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {facturesFiltrees.map((facture) => {
                      const statut = STATUT_LABELS[facture.statut] ?? { label: facture.statut, className: '' }
                      return (
                        <tr key={facture.id}>
                          <td>
                            <button className="link-button" onClick={() => setFactureDetailOuverte(facture)}>
                              <code>{facture.numero}</code>
                            </button>
                          </td>
                          <td>{facture.clientNom}</td>
                          <td>{facture.chantierNom ?? '—'}</td>
                          <td className="cell-muted">{formatDate(facture.dateEmission)}</td>
                          <td className="cell-muted">{formatDate(facture.dateEcheance)}</td>
                          <td><strong>{formatMontant(facture.montantTotal)}</strong></td>
                          <td><span className={`period-badge ${statut.className}`}>{statut.label}</span></td>
                          <td className="table-actions">
                            <button
                              className="btn-icon"
                              title="Télécharger le PDF"
                              disabled={telechargementEnCours === facture.id}
                              onClick={() => handleTelechargerPdf(facture)}
                            >
                              {telechargementEnCours === facture.id ? '⏳' : '⬇️'}
                            </button>
                            {facture.statut === 'BROUILLON' && (
                              <button className="btn-icon" title="Envoyer au client"
                                onClick={() => marquerEnvoyee.mutate(facture.id)}>
                                📤
                              </button>
                            )}
                            {facture.statut === 'ENVOYEE' && (
                              <button className="btn-icon" title="Marquer comme payée"
                                onClick={() => marquerPayee.mutate({ id: facture.id })}>
                                ✅
                              </button>
                            )}
                            {facture.statut !== 'PAYEE' && facture.statut !== 'ANNULEE' && (
                              <button className="btn-icon" title="Annuler la facture"
                                onClick={() => setConfirmAnnulation(facture)}>
                                🗑️
                              </button>
                            )}
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            )
          )}
        </>
      )}

      {/* ── Modale : confirmer la génération de facture ───────────────── */}
      {genererModalOuverte && clientUnique && (
        <Modal title="Générer la facture" icon="🧾" onClose={() => setGenererModalOuverte(false)}>
          <p className="modal-context">
            Générer une facture pour <strong>{clientUnique.clientNom}</strong> regroupant{' '}
            <strong>{selectionInterventions.length}</strong> intervention(s) ?
          </p>
          <div className="modal-diff">
            <span>Total</span>
            <span className="val-apres">{formatMontant(totalSelection)}</span>
          </div>
          <div className="modal-buttons">
            <button className="btn btn-cancel" onClick={() => setGenererModalOuverte(false)}>Annuler</button>
            <button className="btn btn-primary" onClick={confirmerGenerationFacture}>Générer</button>
          </div>
        </Modal>
      )}

      {/* ── Modale : détail d'une facture ─────────────────────────────── */}
      {factureDetailOuverte && (
        <Modal
          title={`Facture ${factureDetailOuverte.numero}`}
          icon="📄"
          onClose={() => setFactureDetailOuverte(null)}
        >
          <p className="modal-context">
            <strong>{factureDetailOuverte.clientNom}</strong>
            {factureDetailOuverte.chantierNom && <> — {factureDetailOuverte.chantierNom}</>}
          </p>
          <table className="gmao-table">
            <thead>
              <tr><th>Intervention</th><th>Pièces</th><th>Main d'œuvre</th><th>Total</th></tr>
            </thead>
            <tbody>
              {(factureDetailOuverte.lignes ?? []).map((ligne) => (
                <tr key={ligne.id}>
                  <td>{ligne.interventionTitre}</td>
                  <td>{formatMontant(ligne.coutPieces)}</td>
                  <td>{formatMontant(ligne.coutMainOeuvre)}</td>
                  <td><strong>{formatMontant(ligne.coutTotal)}</strong></td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="modal-diff">
            <span>Total facture</span>
            <span className="val-apres">{formatMontant(factureDetailOuverte.montantTotal)}</span>
          </div>
          <div className="modal-buttons">
            <button className="btn btn-cancel" onClick={() => setFactureDetailOuverte(null)}>Fermer</button>
            <button
              className="btn btn-primary"
              disabled={telechargementEnCours === factureDetailOuverte.id}
              onClick={() => handleTelechargerPdf(factureDetailOuverte)}
            >
              {telechargementEnCours === factureDetailOuverte.id ? '⏳ Génération…' : '⬇️ Télécharger le PDF'}
            </button>
          </div>
        </Modal>
      )}

      {/* ── Modale : confirmation d'annulation ────────────────────────── */}
      {confirmAnnulation && (
        <Modal title="Annuler la facture" icon="⚠️" onClose={() => setConfirmAnnulation(null)}>
          <p className="modal-context">
            Annuler la facture <strong>{confirmAnnulation.numero}</strong> ? Les interventions liées
            redeviendront disponibles pour une nouvelle facturation.
          </p>
          <div className="modal-buttons">
            <button className="btn btn-cancel" onClick={() => setConfirmAnnulation(null)}>Retour</button>
            <button
              className="btn btn-danger"
              onClick={() => {
                annulerFacture.mutate(confirmAnnulation.id)
                setConfirmAnnulation(null)
              }}
            >
              Confirmer l'annulation
            </button>
          </div>
        </Modal>
      )}
    </div>
  )
}
