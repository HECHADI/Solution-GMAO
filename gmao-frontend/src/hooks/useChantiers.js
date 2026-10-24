import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import {
  fetchChantiers,
  creerChantier,
  modifierChantier,
  supprimerChantier,
  rattacherIntervention,
} from '../api/chantierApi'

export const CHANTIERS_KEY = ['chantiers']

export const useChantiers = () =>
  useQuery({ queryKey: CHANTIERS_KEY, queryFn: fetchChantiers })

export const useCreerChantier = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: creerChantier,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CHANTIERS_KEY })
      toast.success('🏗️ Chantier créé')
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}

export const useModifierChantier = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => modifierChantier(id, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CHANTIERS_KEY })
      toast.success('Chantier mis à jour')
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}

export const useSupprimerChantier = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: supprimerChantier,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CHANTIERS_KEY })
      toast.success('Chantier supprimé')
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}

export const useRattacherIntervention = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ interventionId, chantierId }) => rattacherIntervention(interventionId, chantierId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CHANTIERS_KEY })
      toast.success('Intervention rattachée')
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}
