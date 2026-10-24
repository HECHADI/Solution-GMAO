import React, { createContext, useContext, useEffect, useState, useCallback } from 'react'
import axios from 'axios'
import { login as apiLogin, fetchUtilisateurCourant } from '../api/authApi'

const STORAGE_KEY = 'gmao-token'
const AuthContext = createContext(null)

function appliquerHeaderAuth(token) {
  if (token) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
  } else {
    delete axios.defaults.headers.common['Authorization']
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(STORAGE_KEY))
  const [utilisateur, setUtilisateur] = useState(null)
  const [isLoading, setIsLoading] = useState(true)

  // Au montage : si un token existe déjà (session précédente), on l'applique
  // et on vérifie sa validité en récupérant l'utilisateur courant.
  useEffect(() => {
    if (!token) {
      setIsLoading(false)
      return
    }

    appliquerHeaderAuth(token)
    fetchUtilisateurCourant()
      .then(setUtilisateur)
      .catch(() => {
        // Token expiré/invalide : on nettoie silencieusement
        localStorage.removeItem(STORAGE_KEY)
        setToken(null)
        appliquerHeaderAuth(null)
      })
      .finally(() => setIsLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Déconnexion automatique si une requête renvoie 401 (token expiré en cours de session)
  useEffect(() => {
    const interceptorId = axios.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401 && token) {
          logout()
        }
        return Promise.reject(error)
      }
    )
    return () => axios.interceptors.response.eject(interceptorId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const login = useCallback(async (email, motDePasse) => {
    const data = await apiLogin(email, motDePasse)
    localStorage.setItem(STORAGE_KEY, data.token)
    appliquerHeaderAuth(data.token)
    setToken(data.token)
    setUtilisateur(data.utilisateur)
    return data.utilisateur
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    appliquerHeaderAuth(null)
    setToken(null)
    setUtilisateur(null)
  }, [])

  const value = {
    token,
    utilisateur,
    isAuthenticated: !!token && !!utilisateur,
    isLoading,
    isManager: utilisateur?.role === 'MANAGER',
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth doit être utilisé à l\'intérieur de <AuthProvider>')
  }
  return context
}
