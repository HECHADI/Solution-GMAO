import axios from 'axios'

// ⚠️ Même remarque que pour stockApi.js : ajuste BASE_URL si besoin.
const BASE_URL = '/api/chantiers'

export const fetchChantiers = () =>
  axios.get(BASE_URL).then((res) => res.data)

export const fetchChantierById = (id) =>
  axios.get(`${BASE_URL}/${id}`).then((res) => res.data)

export const creerChantier = (payload) =>
  axios.post(BASE_URL, payload).then((res) => res.data)

export const modifierChantier = (id, payload) =>
  axios.put(`${BASE_URL}/${id}`, payload).then((res) => res.data)

export const supprimerChantier = (id) =>
  axios.delete(`${BASE_URL}/${id}`)

export const rattacherIntervention = (interventionId, chantierId) =>
  axios.patch(`${BASE_URL}/interventions/${interventionId}/rattacher`, { chantierId })
