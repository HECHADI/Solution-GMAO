import apiClient from './client'

/** Récupère toutes les interventions. */
export const fetchInterventions = async () => {
  const { data } = await apiClient.get('/interventions')
  return data
}

/** Récupère les interventions d'un technicien. */
export const fetchInterventionsByTechnicien = async (technicienId) => {
  const { data } = await apiClient.get(`/interventions/technicien/${technicienId}`)
  return data
}

/** Crée une intervention. */
export const createIntervention = async (payload) => {
  const { data } = await apiClient.post('/interventions', payload)
  return data
}

/**
 * Met à jour les dates d'une intervention (drag-and-drop).
 * @param {number}  id
 * @param {{ dateDebut?: string, dateFin?: string, dureeEstimee?: number }} payload
 */
export const updateDates = async (id, payload) => {
  const { data } = await apiClient.patch(`/interventions/${id}/dates`, payload)
  return data
}

/**
 * Assigne un technicien à une intervention.
 * @param {number} id
 * @param {number} technicienId
 */
export const assignerTechnicien = async (id, technicienId) => {
  const { data } = await apiClient.patch(`/interventions/${id}/assigner`, { technicienId })
  return data
}

/**
 * Met à jour le statut d'une intervention.
 * @param {number} id
 * @param {'A_FAIRE'|'EN_COURS'|'REALISE'} statut
 */
export const updateStatut = async (id, statut) => {
  const { data } = await apiClient.patch(`/interventions/${id}/statut`, null, {
    params: { statut },
  })
  return data
}

/** Supprime une intervention. */
export const deleteIntervention = async (id) => {
  await apiClient.delete(`/interventions/${id}`)
}
