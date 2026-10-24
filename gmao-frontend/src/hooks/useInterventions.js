import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import {
  fetchInterventions,
  updateDates,
  assignerTechnicien,
  updateStatut,
  deleteIntervention,
} from '../api/interventionApi'

export const INTERVENTIONS_KEY = ['interventions']

/** Charge toutes les interventions. */
export const useInterventions = () =>
  useQuery({
    queryKey: INTERVENTIONS_KEY,
    queryFn:  fetchInterventions,
    refetchInterval: 60_000, // refresh auto toutes les 60s
  })

/** Mutation : drag-and-drop → mise à jour des dates. */
export const useUpdateDates = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => updateDates(id, payload),
    onSuccess: (updated) => {
      // Mise à jour optimiste dans le cache
      qc.setQueryData(INTERVENTIONS_KEY, (old) =>
        old?.map((i) => (i.id === updated.id ? updated : i))
      )
      toast.success(`📅 Dates mises à jour pour « ${updated.titre} »`)
    },
    onError: (err) => {
      toast.error(`Erreur : ${err.message}`)
      qc.invalidateQueries({ queryKey: INTERVENTIONS_KEY })
    },
  })
}

/** Mutation : assignation d'un technicien. */
export const useAssignerTechnicien = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, technicienId }) => assignerTechnicien(id, technicienId),
    onSuccess: (updated) => {
      qc.setQueryData(INTERVENTIONS_KEY, (old) =>
        old?.map((i) => (i.id === updated.id ? updated : i))
      )
      toast.success(`👤 Technicien assigné à « ${updated.titre} »`)
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}

/** Mutation : changement de statut. */
export const useUpdateStatut = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, statut }) => updateStatut(id, statut),
    onSuccess: (updated) => {
      qc.setQueryData(INTERVENTIONS_KEY, (old) =>
        old?.map((i) => (i.id === updated.id ? updated : i))
      )
      toast.success('Statut mis à jour')
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}

/** Mutation : suppression. */
export const useDeleteIntervention = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id) => deleteIntervention(id),
    onSuccess: (_, id) => {
      qc.setQueryData(INTERVENTIONS_KEY, (old) => old?.filter((i) => i.id !== id))
      toast.success('Intervention supprimée')
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}
