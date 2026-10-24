import { useQuery } from '@tanstack/react-query'
import { fetchNotifications, fetchNotificationsCompteur } from '../api/notificationApi'

export const NOTIFICATIONS_KEY = ['notifications']
export const NOTIFICATIONS_COMPTEUR_KEY = ['notifications', 'compteur']

/** Liste complète — utilisée sur la page dédiée. Rafraîchie toutes les 60s. */
export const useNotifications = () =>
  useQuery({
    queryKey: NOTIFICATIONS_KEY,
    queryFn: fetchNotifications,
    refetchInterval: 60_000,
  })

/** Compteur léger — utilisé pour le badge dans le sidebar. Rafraîchi toutes les 60s. */
export const useNotificationsCompteur = () =>
  useQuery({
    queryKey: NOTIFICATIONS_COMPTEUR_KEY,
    queryFn: fetchNotificationsCompteur,
    refetchInterval: 60_000,
  })