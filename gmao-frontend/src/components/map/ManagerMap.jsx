import React, { useEffect, useMemo, useRef, useState } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import { useLocations } from '../../hooks/useLocations'
import { useInterventions } from '../../hooks/useInterventions'
import './ManagerMap.css'

// ── Configuration Leaflet ───────────────────────────────────────────────────

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
})

const siteIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
})

// Palette pour colorMode = 'technicien' (couleur stable par technicien)
const PALETTE_TECHNICIEN = ['#6366f1', '#ec4899', '#f59e0b', '#14b8a6', '#8b5cf6', '#ef4444', '#22c55e', '#06b6d4']

// Couleurs pour colorMode = 'statut' (statut de la prochaine intervention du technicien)
const COULEUR_STATUT = {
  EN_COURS: '#3b82f6',
  A_FAIRE: '#eab308',
  DISPONIBLE: '#94a3b8', // aucune intervention en attente
}

/** Icône ronde générée dynamiquement (évite de multiplier les images PNG par couleur). */
function creerIconeTechnicien(couleur) {
  return L.divIcon({
    className: 'tech-marker-wrapper',
    html: `<div class="tech-marker-dot" style="background:${couleur}">👷</div>`,
    iconSize: [30, 30],
    iconAnchor: [15, 15],
    popupAnchor: [0, -18],
  })
}

function couleurPourTechnicien(tech, colorMode) {
  if (colorMode === 'technicien') {
    return PALETTE_TECHNICIEN[tech.technicienId % PALETTE_TECHNICIEN.length]
  }
  if (!tech.interventionCibleStatut) return COULEUR_STATUT.DISPONIBLE
  return COULEUR_STATUT[tech.interventionCibleStatut] ?? COULEUR_STATUT.DISPONIBLE
}

/** Cadre automatiquement la carte sur tous les points, une seule fois au premier chargement. */
function FitBoundsOnce({ points }) {
  const map = useMap()
  const dejaCadre = useRef(false)

  useEffect(() => {
    if (dejaCadre.current || points.length === 0) return
    map.fitBounds(L.latLngBounds(points), { padding: [40, 40], maxZoom: 14 })
    dejaCadre.current = true
  }, [points, map])

  return null
}

export default function ManagerMap({ filters }) {
  const { data: locations = [] } = useLocations()
  const { data: interventions = [] } = useInterventions()
  const [mapInstance, setMapInstance] = useState(null)

  // ⚠️ Hypothèse : filters.colorMode vaut 'statut' (défaut) ou 'technicien'.
  // À ajuster si FilterSidebar utilise d'autres valeurs.
  const colorMode = filters?.colorMode ?? 'statut'

  const defaultCenter = [48.8566, 2.3522]

  // ── Sites uniques extraits des interventions ─────────────────────────
  // ⚠️ Suppose que InterventionResponse expose un champ "siteGps" — à
  // confirmer côté DTO, comme signalé lors de la revue d'InterventionsList.
  const uniqueSites = useMemo(() => {
    const seen = new Set()
    const sites = []
    interventions.forEach((i) => {
      if (i.siteGps && !seen.has(i.siteGps)) {
        seen.add(i.siteGps)
        const [lat, lng] = i.siteGps.split(',').map(Number)
        if (!Number.isNaN(lat) && !Number.isNaN(lng)) {
          sites.push({ nom: i.siteNom || 'Site inconnu', lat, lng })
        }
      }
    })
    return sites
  }, [interventions])

  // ── Positions techniciens parsées ─────────────────────────────────────
  const techniciensValides = useMemo(() => {
    return locations
      .map((t) => ({ ...t, lat: parseFloat(t.latitude), lng: parseFloat(t.longitude) }))
      .filter((t) => !Number.isNaN(t.lat) && !Number.isNaN(t.lng))
  }, [locations])

  // ── Tous les points, pour le cadrage automatique ─────────────────────
  const tousLesPoints = useMemo(() => {
    const points = techniciensValides.map((t) => [t.lat, t.lng])
    uniqueSites.forEach((s) => points.push([s.lat, s.lng]))
    return points
  }, [techniciensValides, uniqueSites])

  const handleRecentrer = () => {
    if (mapInstance && tousLesPoints.length > 0) {
      mapInstance.fitBounds(L.latLngBounds(tousLesPoints), { padding: [40, 40], maxZoom: 14 })
    }
  }

  return (
    <div className="map-frame">
      <MapContainer center={defaultCenter} zoom={12} ref={setMapInstance} className="map-root">
        <TileLayer
          attribution='&copy; <a href="https://osm.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          className="map-tiles"
        />

        <FitBoundsOnce points={tousLesPoints} />

        {/* Sites d'intervention */}
        {uniqueSites.map((site, idx) => (
          <Marker key={`site-${idx}`} position={[site.lat, site.lng]} icon={siteIcon}>
            <Popup>
              <div className="popup-content">
                <strong>🏢 Site</strong>
                <div className="popup-sub">{site.nom}</div>
              </div>
            </Popup>
          </Marker>
        ))}

        {/* Trajets : technicien → site de sa prochaine intervention */}
        {techniciensValides
          .filter((t) => t.destinationLat != null && t.destinationLng != null)
          .map((t) => (
            <Polyline
              key={`route-${t.technicienId}`}
              positions={[[t.lat, t.lng], [t.destinationLat, t.destinationLng]]}
              pathOptions={{
                color: couleurPourTechnicien(t, colorMode),
                weight: 2,
                dashArray: '6 8',
                opacity: 0.65,
              }}
            />
          ))}

        {/* Techniciens en direct */}
        {techniciensValides.map((tech) => (
          <Marker
            key={`tech-${tech.technicienId}`}
            position={[tech.lat, tech.lng]}
            icon={creerIconeTechnicien(couleurPourTechnicien(tech, colorMode))}
          >
            <Popup>
              <div className="popup-content popup-content-center">
                <strong>{tech.nom}</strong>
                <div className="popup-sub">
                  🟢 En direct · MAJ {new Date(tech.derniereMiseAJour).toLocaleTimeString('fr-FR')}
                </div>
                {tech.interventionCibleTitre ? (
                  <div className="popup-cible">
                    ➡️ {tech.interventionCibleTitre}
                    <div className="popup-sub">{tech.destinationSiteNom}</div>
                  </div>
                ) : (
                  <div className="popup-sub">Aucune intervention en attente</div>
                )}
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>

      {/* Bouton recentrer */}
      <button className="map-btn-recentrer" onClick={handleRecentrer} title="Recentrer la carte">
        🎯 Recentrer
      </button>

      {/* Légende flottante, adaptée au colorMode */}
      <div className="map-legend">
        {colorMode === 'technicien' ? (
          <>
            <div className="legend-title">Par technicien</div>
            {techniciensValides.slice(0, 6).map((t) => (
              <div className="legend-row" key={t.technicienId}>
                <span className="legend-dot" style={{ background: couleurPourTechnicien(t, colorMode) }} />
                <span>{t.nom}</span>
              </div>
            ))}
          </>
        ) : (
          <>
            <div className="legend-title">Par statut</div>
            <div className="legend-row">
              <span className="legend-dot" style={{ background: COULEUR_STATUT.EN_COURS }} />
              <span>Intervention en cours</span>
            </div>
            <div className="legend-row">
              <span className="legend-dot" style={{ background: COULEUR_STATUT.A_FAIRE }} />
              <span>En route / à faire</span>
            </div>
            <div className="legend-row">
              <span className="legend-dot" style={{ background: COULEUR_STATUT.DISPONIBLE }} />
              <span>Disponible</span>
            </div>
          </>
        )}
        <div className="legend-row legend-row-site">
          <img src="https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png" alt="Site" />
          <span>Site d'intervention</span>
        </div>
      </div>
    </div>
  )
}
