import axios from 'axios'

const BASE_URL = '/api/notifications'

export const fetchNotifications = () =>
  axios.get(BASE_URL).then((res) => res.data)

export const fetchNotificationsCompteur = () =>
  axios.get(`${BASE_URL}/compteur`).then((res) => res.data)