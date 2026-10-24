import axios from 'axios'

const BASE_URL = '/api/utilisateurs'

export const fetchUtilisateurs = (role) =>
  axios.get(BASE_URL, { params: role ? { role } : {} }).then((res) => res.data)

export const creerUtilisateur = (payload) =>
  axios.post(BASE_URL, payload).then((res) => res.data)

export const modifierUtilisateur = (id, payload) =>
  axios.put(`${BASE_URL}/${id}`, payload).then((res) => res.data)

export const changerActifUtilisateur = (id, actif) =>
  axios.patch(`${BASE_URL}/${id}/actif`, { actif }).then((res) => res.data)

export const changerMotDePasse = (id, nouveauMotDePasse) =>
  axios.patch(`${BASE_URL}/${id}/mot-de-passe`, { nouveauMotDePasse })