/**
 * Convertit une intervention Spring Boot en événement FullCalendar.
 */
export const STATUT_COLORS = {
  A_FAIRE:    { bg: '#3b82f6', text: '#fff', label: 'À faire'    },
  EN_COURS:   { bg: '#f59e0b', text: '#fff', label: 'En cours'   },
  REALISE:    { bg: '#10b981', text: '#fff', label: 'Réalisé'    },
}

export const TYPE_COLORS = {
  PREVENTIF: { dot: '#06b6d4', label: 'Préventif' },
  CURATIF:   { dot: '#ef4444', label: 'Curatif'   },
}

export const TECHNICIEN_PALETTE = [
  '#6366f1', '#8b5cf6', '#ec4899', '#f59e0b',
  '#10b981', '#06b6d4', '#f97316', '#84cc16',
]

/** Associe une couleur stable à un technicien en fonction de son id. */
export const getTechnicienColor = (technicienId) =>
  TECHNICIEN_PALETTE[(technicienId ?? 0) % TECHNICIEN_PALETTE.length]

/**
 * Transforme un tableau d'interventions en événements FullCalendar.
 * @param {Array}   interventions
 * @param {string}  colorMode  'statut' | 'technicien' | 'type'
 */
export const toCalendarEvents = (interventions, colorMode = 'statut') => {
  return interventions.map((i) => {
    const statutInfo    = STATUT_COLORS[i.statut]  ?? STATUT_COLORS.A_FAIRE
    const typeInfo      = TYPE_COLORS[i.type]       ?? TYPE_COLORS.PREVENTIF
    const techColor     = getTechnicienColor(i.technicienId)

    let bgColor
    switch (colorMode) {
      case 'technicien': bgColor = i.technicienId ? techColor : '#475569'; break
      case 'type':       bgColor = typeInfo.dot; break
      default:           bgColor = statutInfo.bg
    }

    return {
      id:    String(i.id),
      title: i.titre,
      start: i.dateDebut  ?? undefined,
      end:   i.dateFin    ?? undefined,
      // Si pas de date, affiche en "all-day" sur la date de création
      allDay: !i.dateDebut,
      backgroundColor: bgColor,
      borderColor:     'transparent',
      textColor:       '#fff',
      extendedProps:   { ...i, _typeColor: typeInfo.dot, _statutColor: statutInfo.bg },
    }
  })
}

/** Formate une date ISO en "HH:mm DD/MM/YYYY" */
export const formatDateTime = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  return d.toLocaleString('fr-FR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

/** Formate une durée en minutes en "Xh Ym" */
export const formatDuree = (minutes) => {
  if (!minutes) return '—'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0) return `${m}min`
  if (m === 0) return `${h}h`
  return `${h}h ${m}min`
}
