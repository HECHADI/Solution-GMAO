import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { fetchClients, creerClient, modifierClient, supprimerClient } from '../api/clientApi'

export const CLIENTS_KEY = ['clients']

export const useClients = () =>
  useQuery({ queryKey: CLIENTS_KEY, queryFn: fetchClients })

export const useCreerClient = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: creerClient,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CLIENTS_KEY })
      toast.success('Client créé')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}

export const useModifierClient = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => modifierClient(id, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CLIENTS_KEY })
      toast.success('Client mis à jour')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}

export const useSupprimerClient = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: supprimerClient,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CLIENTS_KEY })
      toast.success('Client supprimé')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}