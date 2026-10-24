import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { fetchParametres, modifierParametre } from '../api/parametreApi'

export const PARAMETRES_KEY = ['parametres']

export const useParametres = () =>
  useQuery({ queryKey: PARAMETRES_KEY, queryFn: fetchParametres })

export const useModifierParametre = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ cle, valeur }) => modifierParametre(cle, valeur),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: PARAMETRES_KEY })
      toast.success('Paramètre mis à jour')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}