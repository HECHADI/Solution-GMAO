package com.gmao.service.impl;

import com.gmao.dto.TrajetInfo;
import com.gmao.service.RoutingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class RoutingServiceImpl implements RoutingService {

    private final Random random = new Random();

    @Override
    public TrajetInfo calculerTrajet(String gpsDepart, String gpsArrivee) {
        log.info("Appel API Cartographie simulé : départ={}, arrivée={}", gpsDepart, gpsArrivee);

        // Si une coordonnée manque, on ne peut pas calculer
        if (gpsDepart == null || gpsDepart.isBlank() || gpsArrivee == null || gpsArrivee.isBlank()) {
            return new TrajetInfo(0, 0);
        }

        // Si le site est le même, 0 trajet
        if (gpsDepart.equals(gpsArrivee)) {
            return new TrajetInfo(0, 0);
        }

        // Simulation d'un délai réseau API Google Maps
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulation d'un calcul : distance entre 5 et 80 km
        int distanceKm = 5 + random.nextInt(75);
        // Simulation d'une vitesse moyenne (environ 60 km/h en ville/périurbain) 
        // + marge de trafic aléatoire
        int dureeMinutes = distanceKm + random.nextInt(15);

        log.info("Résultat API : {} km, {} minutes", distanceKm, dureeMinutes);
        return new TrajetInfo(distanceKm, dureeMinutes);
    }
}
