import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import {
  fetchUtilisateurs,
  creerUtilisateur,
  modifierUtilisateur,
  changerActifUtilisateur,
  changerMotDePasse,
} from '../api/utilisateurApi'

export const UTILISATEURS_KEY = ['utilisateurs']

export const useUtilisateurs = (role) =>
  useQuery({ queryKey: [...UTILISATEURS_KEY, role ?? 'all'], queryFn: () => fetchUtilisateurs(role) })

export const useCreerUtilisateur = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: creerUtilisateur,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: UTILISATEURS_KEY })
      toast.success('Utilisateur créé')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}

export const useModifierUtilisateur = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => modifierUtilisateur(id, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: UTILISATEURS_KEY })
      toast.success('Utilisateur mis à jour')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}

export const useChangerActifUtilisateur = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, actif }) => changerActifUtilisateur(id, actif),
    onSuccess: (updated) => {
      qc.invalidateQueries({ queryKey: UTILISATEURS_KEY })
      toast.success(updated.actif ? '✅ Compte activé' : '⛔ Compte désactivé')
    },
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}

export const useChangerMotDePasse = () => {
  return useMutation({
    mutationFn: ({ id, nouveauMotDePasse }) => changerMotDePasse(id, nouveauMotDePasse),
    onSuccess: () => toast.success('🔑 Mot de passe changé'),
    onError: (err) => toast.error(`Erreur : ${err.response?.data?.message ?? err.message}`),
  })
}