import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { fetchSites, fetchSitesParClient, creerSite, modifierSite, supprimerSite } from '../api/siteApi'

export const SITES_KEY = ['sites']

export const useSites = () =>
  useQuery({ queryKey: SITES_KEY, queryFn: fetchSites, staleTime: 1000 * 60 * 5 })

export const useSitesParClient = (clientId) =>
  useQuery({
    queryKey: [...SITES_KEY, 'client', clientId],
    queryFn: () => fetchSitesParClient(clientId),
    enabled: clientId != null,
  })

export const useCreerSite = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: creerSite,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: SITES_KEY })
      toast.success('📍 Site créé')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}

export const useModifierSite = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => modifierSite(id, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: SITES_KEY })
      toast.success('Site mis à jour')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}

export const useSupprimerSite = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: supprimerSite,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: SITES_KEY })
      toast.success('Site supprimé')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}