import { STATUT_COLORS, TYPE_COLORS } from '../../utils/calendarHelpers'

const STATUT_LABELS = {
  A_FAIRE:  { ...STATUT_COLORS.A_FAIRE,  icon: '🔵' },
  EN_COURS: { ...STATUT_COLORS.EN_COURS, icon: '🟡' },
  REALISE:  { ...STATUT_COLORS.REALISE,  icon: '🟢' },
}

const TYPE_LABELS = {
  PREVENTIF: { color: TYPE_COLORS.PREVENTIF.dot, icon: '🛡️' },
  CURATIF:   { color: TYPE_COLORS.CURATIF.dot,   icon: '🔧' },
}

export const StatutBadge = ({ statut }) => {
  const info = STATUT_LABELS[statut] ?? STATUT_LABELS.A_FAIRE
  return (
    <span style={{
      display: 'inline-flex',
      alignItems: 'center',
      gap: '5px',
      padding: '3px 10px',
      borderRadius: '99px',
      fontSize: '0.72rem',
      fontWeight: 600,
      letterSpacing: '0.04em',
      background: `${info.bg}22`,
      color: info.bg,
      border: `1px solid ${info.bg}44`,
    }}>
      {info.icon} {info.label}
    </span>
  )
}

export const TypeBadge = ({ type }) => {
  const info = TYPE_LABELS[type] ?? TYPE_LABELS.PREVENTIF
  const label = TYPE_COLORS[type]?.label ?? type
  return (
    <span style={{
      display: 'inline-flex',
      alignItems: 'center',
      gap: '5px',
      padding: '3px 10px',
      borderRadius: '99px',
      fontSize: '0.72rem',
      fontWeight: 600,
      background: `${info.color}22`,
      color: info.color,
      border: `1px solid ${info.color}44`,
    }}>
      {info.icon} {label}
    </span>
  )
}
