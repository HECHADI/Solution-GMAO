import apiClient from './client'

/** Récupère tous les utilisateurs de rôle TECHNICIEN actifs. */
export const fetchTechniciens = async () => {
  const { data } = await apiClient.get('/utilisateurs', {
    params: { role: 'TECHNICIEN', actif: true },
  })
  return data
}

/** Récupère un technicien par son id. */
export const fetchTechnicienById = async (id) => {
  const { data } = await apiClient.get(`/utilisateurs/${id}`)
  return data
}
