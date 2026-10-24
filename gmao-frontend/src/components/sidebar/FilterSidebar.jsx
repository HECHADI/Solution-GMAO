import React from 'react'
import { useNotificationsCompteur } from '../../hooks/useNotifications'
import { useTheme } from '../../hooks/useTheme'
import './FilterSidebar.css' // 🟢 Import du CSS isolé

export default function FilterSidebar({ filters, onFiltersChange, currentPage, onPageChange, utilisateur, onLogout }) {
  const { data: compteur } = useNotificationsCompteur()
  const { toggleTheme, isDark } = useTheme()
  const estManager = utilisateur?.role === 'MANAGER'
  
  const handleColorModeChange = (mode) => {
    onFiltersChange({ ...filters, colorMode: mode })
  }

  return (
    <aside className="sidebar">
      {/* Logo / Header */}
      <div className="sidebar-brand sidebar-brand-row">
        <h2 className="sidebar-title gradient-text">⚙️ GMAO Hub</h2>
        <button className="theme-toggle-mini" onClick={toggleTheme} title="Changer de thème">
          {isDark ? '🌙' : '☀️'}
        </button>
      </div>

      {/* Navigation principale */}
      <div className="sidebar-section">
        <div className="sidebar-section-title">Navigation</div>
        <button
          onClick={() => onPageChange('notifications')}
          className={`btn ${currentPage === 'notifications' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}
        >
          🔔 Alertes
          {compteur?.total > 0 && (
            <span className={`notif-badge ${compteur.critique > 0 ? 'notif-badge-critique' : ''}`}>
              {compteur.total}
            </span>
          )}
        </button>
        <button
          onClick={() => onPageChange('supervision')}
          className={`btn ${currentPage === 'supervision' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}
        >
          👁️‍🗨️ Carte Live
        </button>
        <button
          onClick={() => onPageChange('planification')}
          className={`btn ${currentPage === 'planification' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}
        >
          📅 Planification
        </button>

        <button
          onClick={() => onPageChange('interventions')}
          className={`btn ${currentPage === 'interventions' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}
        >
          🛠️ Interventions
        </button>
        <button
          onClick={() => onPageChange('maintenance')}
          className={`btn ${currentPage === 'maintenance' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}
        >
          📋 Maintenance
        </button>
        <button
          onClick={() => onPageChange('stock')}
          className={`btn ${currentPage === 'stock' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}
        >
          📦 Stock & Pièces
        </button>
        <button
          onClick={() => onPageChange('chantier')}
          className={`btn ${currentPage === 'chantier' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}
        >
          🏗️ Chantiers
        </button>
        <button
          onClick={() => onPageChange('facturation')}
          className={`btn ${currentPage === 'facturation' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}
        >
          🪙 Facturation
        </button>
        <button onClick={() => onPageChange('clients')} className={`btn ${currentPage === 'clients' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}>
          💼 Clients
        </button>
        {estManager && (
          <button onClick={() => onPageChange('utilisateurs')} className={`btn ${currentPage === 'utilisateurs' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}>
            👤 Utilisateurs
          </button>
        )}
        <button
            onClick={() => onPageChange('equipements')} className={`btn ${currentPage === 'equipements' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}>
            ⚙️ Gestion Équipements
        </button>
        <button
            onClick={() => onPageChange('sites')} className={`btn ${currentPage === 'sites' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}>
            📍 Sites
        </button>
        <button
            onClick={() => onPageChange('parametres')} className={`btn ${currentPage === 'parametres' ? 'btn-primary' : 'btn-ghost'} sidebar-nav-btn`}>
            🔧 Paramètres
        </button>
      </div>

      {/* Modes de coloration */}
      <div className="sidebar-section">
        <div className="sidebar-section-title">Couleur par</div>
        <div className="sidebar-options-grid">
          <button
            onClick={() => handleColorModeChange('statut')}
            className={`btn ${filters.colorMode === 'statut' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Statut de l'intervention
          </button>
          <button
            onClick={() => handleColorModeChange('technicien')}
            className={`btn ${filters.colorMode === 'technicien' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Technicien assigné
          </button>
        </div>
      </div>

      {/* Utilisateur connecté / déconnexion */}
      {utilisateur && (
        <div className="sidebar-user">
          <div className="sidebar-user-info">
            <div className="sidebar-user-avatar">{utilisateur.nom?.[0]?.toUpperCase()}</div>
            <div className="sidebar-user-text">
              <div className="sidebar-user-nom">{utilisateur.nom}</div>
              <div className="sidebar-user-role">{utilisateur.role === 'MANAGER' ? 'Manager' : 'Technicien'}</div>
            </div>
          </div>
          <button className="sidebar-logout-btn" onClick={onLogout} title="Se déconnecter">
            🚪
          </button>
        </div>
      )}
    </aside>
  )
}