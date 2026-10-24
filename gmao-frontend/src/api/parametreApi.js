import axios from 'axios'

const BASE_URL = '/api/parametres'

export const fetchParametres = () =>
  axios.get(BASE_URL).then((res) => res.data)

export const modifierParametre = (cle, valeur) =>
  axios.put(`${BASE_URL}/${cle}`, { valeur }).then((res) => res.data)