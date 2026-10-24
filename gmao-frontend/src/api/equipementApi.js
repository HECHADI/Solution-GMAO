import axios from 'axios'

const BASE_URL = '/api/equipements'

export const fetchEquipements = () =>
  axios.get(BASE_URL).then((res) => res.data)

export const fetchEquipementDetail = (id) =>
  axios.get(`${BASE_URL}/${id}`).then((res) => res.data)

export const creerEquipement = (payload) =>
  axios.post(BASE_URL, payload).then((res) => res.data)

export const modifierEquipement = (id, payload) =>
  axios.put(`${BASE_URL}/${id}`, payload).then((res) => res.data)

export const changerStatutEquipement = (id, statut) =>
  axios.patch(`${BASE_URL}/${id}/statut`, { statut }).then((res) => res.data)


export const supprimerEquipement = (id) =>
  axios.delete(`${BASE_URL}/${id}`)