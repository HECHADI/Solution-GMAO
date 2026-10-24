import axios from 'axios'

// ⚠️ Ajuste BASE_URL si ton interventionApi.js utilise une constante/config
// différente (ex: import.meta.env.VITE_API_URL). Je pars sur la même
// convention que le reste du projet (proxy Vite vers /api).
const BASE_URL = '/api/stock'

export const fetchCatalogue = () =>
  axios.get(`${BASE_URL}/pieces`).then((res) => res.data)

export const modifierSeuilAlerte = (pieceId, seuilAlerte) =>
  axios.patch(`${BASE_URL}/pieces/${pieceId}/seuil`, { seuilAlerte }).then((res) => res.data)

export const fetchStockVehicules = () =>
  axios.get(`${BASE_URL}/vehicules`).then((res) => res.data)

export const fetchStockParTechnicien = (technicienId) =>
  axios.get(`${BASE_URL}/vehicules/technicien/${technicienId}`).then((res) => res.data)

export const approvisionner = (payload) =>
  axios.post(`${BASE_URL}/vehicules/approvisionner`, payload).then((res) => res.data)

export const transferer = (payload) =>
  axios.post(`${BASE_URL}/vehicules/transferer`, payload).then((res) => res.data)

export const ajusterStock = (payload) =>
  axios.patch(`${BASE_URL}/vehicules/ajuster`, payload).then((res) => res.data)

export const fetchHistoriqueComplet = () =>
  axios.get(`${BASE_URL}/mouvements`).then((res) => res.data)

export const fetchHistoriqueParPiece = (pieceId) =>
  axios.get(`${BASE_URL}/mouvements/piece/${pieceId}`).then((res) => res.data)

export const fetchHistoriqueParTechnicien = (technicienId) =>
  axios.get(`${BASE_URL}/mouvements/technicien/${technicienId}`).then((res) => res.data)
