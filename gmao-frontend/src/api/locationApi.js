import apiClient from './client'

/**
 * Récupère les positions GPS simulées des techniciens en temps réel.
 */
export const fetchTechnicienLocations = async () => {
  const { data } = await apiClient.get('/locations/techniciens')
  return data
}
