import React, { useState } from 'react'
import { useAuth } from './context/AuthContext'
import LoginPage from './pages/LoginPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import FilterSidebar from './components/sidebar/FilterSidebar'
import StatsBar from './components/ui/StatsBar'
import ManagerMap from './components/map/ManagerMap'
import PlanningCalendar from './components/PlanningCalendar' 
import InterventionsList from './components/InterventionsList'
import MaintenancePreventive from './components/MaintenancePreventive'
import StockManager from './components/StockManager'
import SiteProjectManager from './components/SiteProjectManager'
import BillingManager from './components/BillingManager'
import ClientManager from './components/ClientManager'
import UtilisateurManager from './components/UtilisateurManager'
import EquipementManager from './components/EquipementManager'
import SiteManager from './components/SiteManager'
import NotificationCenter from './components/NotificationCenter'
import ParametresManager from './components/ParametresManager'

import './App.css'

/**
 * ⚠️ INTÉGRATION REQUISE dans main.jsx :
 *
 *   import { AuthProvider } from './context/AuthContext'
 *   ...
 *   <AuthProvider>
 *     <App />
 *   </AuthProvider>
 *
 * (à placer à l'intérieur de ton éventuel QueryClientProvider existant,
 * l'ordre exact entre les deux n'a pas d'importance ici).
 */
export default function App() {
  const { isAuthenticated, isLoading, utilisateur, logout } = useAuth()

  const [authView, setAuthView] = useState(() =>
    window.location.pathname === '/reinitialiser-mot-de-passe' ? 'reset' : 'login'
  )

  const [currentPage, setCurrentPage] = useState('supervision')
  const [filters, setFilters] = useState({
    colorMode: 'statut',
    statuts: [],
    types: [],
    techniciens: [],
  })

  // ── Chargement initial (vérification du token existant) ──────────────
  if (isLoading) {
    return (
      <div className="auth-loading-screen">
        <div className="spinner" />
      </div>
    )
  }

  // ── Non connecté : login / mot de passe oublié / réinitialisation ────
  if (!isAuthenticated) {
    if (authView === 'reset') {
      return (
        <ResetPasswordPage
          onRetourConnexion={() => {
            window.history.replaceState({}, '', '/')
            setAuthView('login')
          }}
        />
      )
    }
    if (authView === 'forgot') {
      return <ForgotPasswordPage onRetourConnexion={() => setAuthView('login')} />
    }
    return <LoginPage onNaviguerVersMotDePasseOublie={() => setAuthView('forgot')} />
  }

  // ── Connecté : application normale ────────────────────────────────────
  return (
    <div className="app-container">
      {/* 1. Panneau latéral gauche */}
      <FilterSidebar 
        filters={filters} 
        onFiltersChange={setFilters} 
        currentPage={currentPage}
        onPageChange={setCurrentPage}
        utilisateur={utilisateur}
        onLogout={logout}
      />

      {/* 2. Zone principale droite */}
      <main className="main-content">
        <header className="page-header">
          <h1 className="page-title gradient-text">
            {currentPage === 'supervision' && 'Tableau de Bord Supervision'}
            {currentPage === 'planification' && 'Planification des Interventions'}
            {currentPage === 'interventions' && 'Registre des Interventions'}
            {currentPage === 'maintenance' && 'Maintenance Préventive'}
            {currentPage === 'stock' && 'Gestion des Stocks & Pièces'}
            {currentPage === 'chantier' && 'Gestion des Chantiers & Projets'}
            {currentPage === 'facturation' && 'Facturation & Clôtures Financières'}
            {currentPage === 'clients' && 'Gestion des Clients'}
            {currentPage === 'utilisateurs' && 'Gestion des Utilisateurs'}
            {currentPage === 'equipements' && 'Gestion des Équipements'}
            {currentPage === 'sites' && 'Gestion des Sites'}
            {(currentPage === 'notifications' || currentPage === 'alertes') && 'Centre de Notifications & Alertes'}
            {currentPage === 'parametres' && 'Paramètres'}
          </h1>
          <p className="page-description">
            {currentPage === 'supervision' && 'Suivi cartographique des techniciens sur le terrain et état du planning.'}
            {currentPage === 'planification' && 'Attribuez et organisez les interventions par simple glisser-déposer.'}
            {currentPage === 'interventions' && 'Consultez, recherchez et gérez l’historique complet de vos ordres de travail.'}
            {currentPage === 'maintenance' && 'Planifiez les gammes de maintenance, inspections périodiques et contrôles réglementaires.'}
            {currentPage === 'stock' && 'Suivi du catalogue de pièces détachées, des stocks véhicules techniciens.'}
            {currentPage === 'chantier' && 'Supervision des interventions regroupées par projets complexes.'}
            {currentPage === 'facturation' && 'Calcul automatique des coûts de maintenance, édition des factures clients basées sur les pièces utilisées.'}
            {currentPage === 'clients' && 'Consultez et gérez le portefeuille de clients rattachés à vos sites et chantiers.'}
            {currentPage === 'utilisateurs' && 'Gérez les comptes managers et techniciens : rôles, activation, mots de passe.'}
            {currentPage === 'equipements' && 'Fiche par équipement : statut, historique des interventions et plans de maintenance liés.'}
            {currentPage === 'sites' && 'Gérez les sites géographiques rattachés à vos clients.'}
            {(currentPage === 'notifications' || currentPage === 'alertes') && 'Toutes les alertes actives : stock critique, maintenance en retard, équipements en panne, chantiers en retard.'}
            {currentPage === 'parametres' && "Apparence et réglages généraux de l'application."}
          </p>
        </header>

        <StatsBar />

        {/* Contenu de la page active */}
        <div className="content-body">
          {currentPage === 'supervision' && (
            <>
              <div className="content-header">
                <h2 className="content-subtitle">🗺️ Géolocalisation Live</h2>
              </div>
              <ManagerMap filters={filters} />
            </>
          )}
          {currentPage === 'planification' && <PlanningCalendar filters={filters} />}
          {currentPage === 'interventions' && <InterventionsList filters={filters} />}
          {currentPage === 'maintenance' && <MaintenancePreventive />}
          {currentPage === 'stock' && <StockManager />}
          {currentPage === 'chantier' && <SiteProjectManager />}
          {currentPage === 'facturation' && <BillingManager />}
          {currentPage === 'clients' && <ClientManager />}
          {currentPage === 'utilisateurs' && <UtilisateurManager />}
          {currentPage === 'equipements' && <EquipementManager />}
          {currentPage === 'sites' && <SiteManager />}
          {(currentPage === 'notifications' || currentPage === 'alertes') && <NotificationCenter onPageChange={setCurrentPage} />}
          {currentPage === 'parametres' && <ParametresManager />}
        </div>
      </main>
    </div>
  )
}
