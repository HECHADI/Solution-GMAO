import axios from 'axios'

const BASE_URL = '/api/sites'

export const fetchSites = () =>
  axios.get(BASE_URL).then((res) => res.data)

export const fetchSitesParClient = (clientId) =>
  axios.get(`${BASE_URL}/client/${clientId}`).then((res) => res.data)

export const creerSite = (payload) =>
  axios.post(BASE_URL, payload).then((res) => res.data)

export const modifierSite = (id, payload) =>
  axios.put(`${BASE_URL}/${id}`, payload).then((res) => res.data)

export const supprimerSite = (id) =>
  axios.delete(`${BASE_URL}/${id}`)