import React, { useMemo, useState } from 'react'
import { useNotifications } from '../hooks/useNotifications'
import './NotificationCenter.css'

const TYPE_ICONES = {
  STOCK_CRITIQUE: '📦',
  MAINTENANCE_RETARD: '📅',
  MAINTENANCE_BIENTOT: '📅',
  EQUIPEMENT_PANNE: '🔧',
  CHANTIER_RETARD: '🏗️',
}

const PAGE_LABELS = {
  stock: 'Aller au Stock',
  maintenance: 'Aller à la Maintenance',
  equipements: 'Aller aux Équipements',
  chantier: 'Aller aux Chantiers',
}

export default function NotificationCenter({ onPageChange }) {
  const { data: notifications = [], isLoading } = useNotifications()
  const [filtreGravite, setFiltreGravite] = useState('') // '' | 'CRITIQUE' | 'ATTENTION'

  const filtres = useMemo(() => {
    if (!filtreGravite) return notifications
    return notifications.filter((n) => n.gravite === filtreGravite)
  }, [notifications, filtreGravite])

  const compteurs = useMemo(() => {
    const critique = notifications.filter((n) => n.gravite === 'CRITIQUE').length
    return { critique, attention: notifications.length - critique, total: notifications.length }
  }, [notifications])

  if (isLoading) {
    return (
      <div className="notif-container anim-fadein">
        <div className="notif-card glass skeleton-card" />
      </div>
    )
  }

  return (
    <div className="notif-container anim-fadein">
      <div className="notif-summary">
        <button
          className={`summary-chip chip-all ${filtreGravite === '' ? 'chip-active' : ''}`}
          onClick={() => setFiltreGravite('')}
        >
          🔔 {compteurs.total} au total
        </button>
        <button
          className={`summary-chip chip-critique ${filtreGravite === 'CRITIQUE' ? 'chip-active' : ''}`}
          onClick={() => setFiltreGravite('CRITIQUE')}
        >
          🔴 {compteurs.critique} critique(s)
        </button>
        <button
          className={`summary-chip chip-attention ${filtreGravite === 'ATTENTION' ? 'chip-active' : ''}`}
          onClick={() => setFiltreGravite('ATTENTION')}
        >
          🟡 {compteurs.attention} à surveiller
        </button>
      </div>

      <div className="notif-card glass">
        {filtres.length === 0 ? (
          <div className="empty-state">
            {notifications.length === 0
              ? "🎉 Aucune alerte — tout est sous contrôle."
              : "Aucune alerte ne correspond à ce filtre."}
          </div>
        ) : (
          <div className="notif-list">
            {filtres.map((n) => (
              <button
                key={n.id}
                className={`notif-item notif-${n.gravite.toLowerCase()}`}
                onClick={() => onPageChange?.(n.page)}
              >
                <span className="notif-icon">{TYPE_ICONES[n.type] ?? '⚠️'}</span>
                <span className="notif-body">
                  <span className="notif-titre">{n.titre}</span>
                  <span className="notif-message">{n.message}</span>
                </span>
                <span className="notif-lien">{PAGE_LABELS[n.page] ?? 'Voir'} →</span>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
