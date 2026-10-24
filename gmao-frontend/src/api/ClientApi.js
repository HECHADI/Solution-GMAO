import axios from 'axios'

const BASE_URL = '/api/clients'

export const fetchClients = () =>
  axios.get(BASE_URL).then((res) => res.data)

export const creerClient = (payload) =>
  axios.post(BASE_URL, payload).then((res) => res.data)

export const modifierClient = (id, payload) =>
  axios.put(`${BASE_URL}/${id}`, payload).then((res) => res.data)

export const supprimerClient = (id) =>
  axios.delete(`${BASE_URL}/${id}`)