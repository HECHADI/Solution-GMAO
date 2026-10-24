import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import {
  fetchCatalogue,
  modifierSeuilAlerte,
  fetchStockVehicules,
  approvisionner,
  transferer,
  ajusterStock,
  fetchHistoriqueComplet,
} from '../api/stockApi'

export const CATALOGUE_KEY = ['stock', 'catalogue']
export const STOCK_VEHICULES_KEY = ['stock', 'vehicules']
export const HISTORIQUE_KEY = ['stock', 'mouvements']

export const useCatalogue = () =>
  useQuery({ queryKey: CATALOGUE_KEY, queryFn: fetchCatalogue })

export const useStockVehicules = () =>
  useQuery({
    queryKey: STOCK_VEHICULES_KEY,
    queryFn: fetchStockVehicules,
    refetchInterval: 60_000,
  })

export const useHistoriqueMouvements = () =>
  useQuery({ queryKey: HISTORIQUE_KEY, queryFn: fetchHistoriqueComplet })

export const useModifierSeuilAlerte = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ pieceId, seuilAlerte }) => modifierSeuilAlerte(pieceId, seuilAlerte),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CATALOGUE_KEY })
      qc.invalidateQueries({ queryKey: STOCK_VEHICULES_KEY })
      toast.success('Seuil d\'alerte mis à jour')
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}

export const useApprovisionner = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: approvisionner,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: STOCK_VEHICULES_KEY })
      qc.invalidateQueries({ queryKey: HISTORIQUE_KEY })
      toast.success('📦 Stock approvisionné')
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}

export const useTransferer = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: transferer,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: STOCK_VEHICULES_KEY })
      qc.invalidateQueries({ queryKey: HISTORIQUE_KEY })
      toast.success('🔄 Transfert effectué')
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}

export const useAjusterStock = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ajusterStock,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: STOCK_VEHICULES_KEY })
      qc.invalidateQueries({ queryKey: HISTORIQUE_KEY })
      toast.success('✏️ Stock ajusté')
    },
    onError: (err) => toast.error(`Erreur : ${err.message}`),
  })
}
