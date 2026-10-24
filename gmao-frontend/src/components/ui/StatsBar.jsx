import { useInterventions } from '../../hooks/useInterventions'
import { STATUT_COLORS } from '../../utils/calendarHelpers'

const stat = (interventions, statut) =>
  interventions.filter((i) => i.statut === statut).length

export default function StatsBar() {
  const { data: interventions = [] } = useInterventions()

  const stats = [
    { key: 'A_FAIRE',  count: stat(interventions, 'A_FAIRE'),  icon: '📋' },
    { key: 'EN_COURS', count: stat(interventions, 'EN_COURS'), icon: '⚙️'  },
    { key: 'REALISE',  count: stat(interventions, 'REALISE'),  icon: '✅'  },
  ]

  const total = interventions.length

  return (
    <div style={{
      display: 'flex',
      gap: '12px',
      flexWrap: 'wrap',
      padding: '16px 24px',
      background: 'rgba(30,41,59,0.6)',
      borderBottom: '1px solid var(--border)',
      backdropFilter: 'blur(12px)',
    }}>
      {/* Total */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        padding: '10px 20px',
        background: 'rgba(99,102,241,0.1)',
        border: '1px solid rgba(99,102,241,0.25)',
        borderRadius: 'var(--radius-md)',
      }}>
        <span style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--accent-light)' }}>
          {total}
        </span>
        <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', lineHeight: 1.3 }}>
          Total<br />interventions
        </span>
      </div>

      {/* Par statut */}
      {stats.map(({ key, count, icon }) => {
        const color = STATUT_COLORS[key].bg
        const label = STATUT_COLORS[key].label
        const pct   = total > 0 ? Math.round((count / total) * 100) : 0
        return (
          <div key={key} style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            padding: '10px 20px',
            background: `${color}10`,
            border: `1px solid ${color}30`,
            borderRadius: 'var(--radius-md)',
            flex: 1,
            minWidth: 140,
          }}>
            <span style={{ fontSize: '1.1rem' }}>{icon}</span>
            <div>
              <div style={{ fontSize: '1.3rem', fontWeight: 700, color, lineHeight: 1 }}>
                {count}
              </div>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: 2 }}>
                {label} ({pct}%)
              </div>
              {/* Progress bar */}
              <div style={{
                marginTop: 6,
                width: 80,
                height: 3,
                background: `${color}20`,
                borderRadius: 99,
                overflow: 'hidden',
              }}>
                <div style={{
                  width: `${pct}%`,
                  height: '100%',
                  background: color,
                  borderRadius: 99,
                  transition: 'width 0.5s ease',
                }} />
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}
