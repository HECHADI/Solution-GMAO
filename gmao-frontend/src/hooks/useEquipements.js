import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import {
  fetchEquipements,
  fetchEquipementDetail,
  creerEquipement,
  modifierEquipement,
  changerStatutEquipement,
  supprimerEquipement,
} from '../api/equipementApi'
export { useSites } from './useSites' // ré-exporté pour compatibilité avec l'existant

export const EQUIPEMENTS_KEY = ['equipements']

export const useEquipements = () =>
  useQuery({ queryKey: EQUIPEMENTS_KEY, queryFn: fetchEquipements })

export const useEquipementDetail = (id) =>
  useQuery({
    queryKey: [...EQUIPEMENTS_KEY, id],
    queryFn: () => fetchEquipementDetail(id),
    enabled: id != null,
  })


export const useCreerEquipement = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: creerEquipement,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: EQUIPEMENTS_KEY })
      toast.success('🔧 Équipement créé')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}

export const useModifierEquipement = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => modifierEquipement(id, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: EQUIPEMENTS_KEY })
      toast.success('Équipement mis à jour')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}

export const useChangerStatutEquipement = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, statut }) => changerStatutEquipement(id, statut),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: EQUIPEMENTS_KEY })
      toast.success('Statut mis à jour')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}

export const useSupprimerEquipement = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: supprimerEquipement,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: EQUIPEMENTS_KEY })
      toast.success('Équipement supprimé')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}