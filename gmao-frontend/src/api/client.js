import axios from 'axios'

/**
 * Instance Axios préconfigurée pour l'API Spring Boot GMAO.
 * Le proxy Vite redirige /api → http://localhost:8080/api.
 */
const apiClient = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10_000,
})

// Intercepteur de réponse : expose un message d'erreur propre
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.detail ||
      error.response?.data?.message ||
      error.message ||
      'Erreur réseau'
    return Promise.reject(new Error(message))
  }
)

export default apiClient
