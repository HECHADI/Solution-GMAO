import axios from 'axios'

const BASE_URL = '/api/auth'

export const login = (email, motDePasse) =>
  axios.post(`${BASE_URL}/login`, { email, motDePasse }).then((res) => res.data)

export const fetchUtilisateurCourant = () =>
  axios.get(`${BASE_URL}/me`).then((res) => res.data)

export const demanderReinitialisation = (email) =>
  axios.post(`${BASE_URL}/mot-de-passe-oublie`, { email }).then((res) => res.data)

export const reinitialiserMotDePasse = (token, nouveauMotDePasse) =>
  axios.post(`${BASE_URL}/reinitialiser-mot-de-passe`, { token, nouveauMotDePasse }).then((res) => res.data)