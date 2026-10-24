import { useCallback, useEffect, useState } from 'react'

const STORAGE_KEY = 'gmao-theme'

function lireThemeInitial() {
  if (typeof window === 'undefined') return 'dark'
  return localStorage.getItem(STORAGE_KEY) || 'dark'
}

/**
 * Gère le thème clair/sombre de toute l'application.
 * Applique `data-theme="dark" | "light"` sur <html>, à faire correspondre
 * avec les variables CSS définies dans theme-variables.css.
 *
 * Utilisable depuis n'importe quel composant (Paramètres, sidebar…) sans
 * Provider — le state est répliqué via localStorage + l'attribut DOM, donc
 * plusieurs composants qui utilisent ce hook restent cohérents entre eux
 * tant qu'ils sont montés en même temps que le changement a lieu.
 */
export function useTheme() {
  const [theme, setTheme] = useState(lireThemeInitial)

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem(STORAGE_KEY, theme)
  }, [theme])

  const toggleTheme = useCallback(() => {
    setTheme((t) => (t === 'dark' ? 'light' : 'dark'))
  }, [])

  return { theme, setTheme, toggleTheme, isDark: theme === 'dark' }
}