import apiClient from './client'

const BASE_URL = '/facturation'

export const factureApi = {
  listerInterventionsAFacturer: () =>
    apiClient.get(`${BASE_URL}/a-facturer`).then((res) => res.data),

  listerInterventionsAFacturerParClient: (clientId) =>
    apiClient.get(`${BASE_URL}/a-facturer/client/${clientId}`).then((res) => res.data),

  genererFacture: (payload) =>
    apiClient.post(`${BASE_URL}/factures`, payload).then((res) => res.data),

  listerFactures: () =>
    apiClient.get(`${BASE_URL}/factures`).then((res) => res.data),

  trouverFacture: (id) =>
    apiClient.get(`${BASE_URL}/factures/${id}`).then((res) => res.data),

  listerFacturesParStatut: (statut) =>
    apiClient.get(`${BASE_URL}/factures/statut/${statut}`).then((res) => res.data),

  marquerEnvoyee: (id) =>
    apiClient.patch(`${BASE_URL}/factures/${id}/envoyer`).then((res) => res.data),

  marquerPayee: (id, datePaiement) =>
    apiClient
      .patch(`${BASE_URL}/factures/${id}/payer`, datePaiement ? { datePaiement } : {})
      .then((res) => res.data),

  annulerFacture: (id) =>
    apiClient.patch(`${BASE_URL}/factures/${id}/annuler`).then((res) => res.data),

  telechargerPdf: (id) =>
    apiClient
      .get(`${BASE_URL}/factures/${id}/pdf`, { responseType: 'blob' })
      .then((res) => res.data),
}

/**
 * Déclenche le téléchargement du PDF dans le navigateur (crée une URL blob
 * temporaire, simule un clic sur un lien, puis la révoque).
 */
export async function telechargerFacturePdf(id, numero) {
  const blob = await factureApi.telechargerPdf(id)
  const url = window.URL.createObjectURL(blob)
  const lien = document.createElement('a')
  lien.href = url
  lien.download = `${numero ?? 'facture-' + id}.pdf`
  document.body.appendChild(lien)
  lien.click()
  lien.remove()
  window.URL.revokeObjectURL(url)
}
