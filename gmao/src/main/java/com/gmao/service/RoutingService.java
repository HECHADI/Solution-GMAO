package com.gmao.service;

import com.gmao.dto.TrajetInfo;

/**
 * Service gérant le calcul des itinéraires (Simulation API Externe).
 */
public interface RoutingService {
    
    /**
     * Calcule la distance et le temps de trajet entre deux points GPS.
     *
     * @param gpsDepart  Coordonnées de départ (ex: "48.8566,2.3522")
     * @param gpsArrivee Coordonnées d'arrivée
     * @return Les informations de trajet (distance en km, durée en minutes)
     */
    TrajetInfo calculerTrajet(String gpsDepart, String gpsArrivee);
}
