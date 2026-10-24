import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { factureApi } from '../api/factureApi'

const QK_A_FACTURER = ['facturation', 'a-facturer']
const QK_FACTURES = ['facturation', 'factures']

export function useInterventionsAFacturer() {
  return useQuery({
    queryKey: QK_A_FACTURER,
    queryFn: factureApi.listerInterventionsAFacturer,
  })
}

export function useFactures() {
  return useQuery({
    queryKey: QK_FACTURES,
    queryFn: factureApi.listerFactures,
  })
}

export function useFacture(id) {
  return useQuery({
    queryKey: [...QK_FACTURES, id],
    queryFn: () => factureApi.trouverFacture(id),
    enabled: !!id,
  })
}

function invalidateFacturation(queryClient) {
  queryClient.invalidateQueries({ queryKey: QK_A_FACTURER })
  queryClient.invalidateQueries({ queryKey: QK_FACTURES })
}

export function useGenererFacture() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: factureApi.genererFacture,
    onSuccess: () => invalidateFacturation(queryClient),
  })
}

export function useMarquerEnvoyee() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: factureApi.marquerEnvoyee,
    onSuccess: () => invalidateFacturation(queryClient),
  })
}

export function useMarquerPayee() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, datePaiement }) => factureApi.marquerPayee(id, datePaiement),
    onSuccess: () => invalidateFacturation(queryClient),
  })
}

export function useAnnulerFacture() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: factureApi.annulerFacture,
    onSuccess: () => invalidateFacturation(queryClient),
  })
}
