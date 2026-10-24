import { useQuery } from '@tanstack/react-query'
import { fetchTechnicienLocations } from '../api/locationApi'

/**
 * Hook pour récupérer la position des techniciens en temps réel.
 * Rafraîchit les données toutes les 5 secondes.
 */
export const useLocations = () =>
  useQuery({
    queryKey: ['locations'],
    queryFn: fetchTechnicienLocations,
    refetchInterval: 5000, // Rafraîchissement automatique toutes les 5s
  })
