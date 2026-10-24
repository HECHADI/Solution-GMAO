package com.gmao.controller;

import com.gmao.dto.TechnicienLocation;
import com.gmao.entity.Intervention;
import com.gmao.entity.RoleUtilisateur;
import com.gmao.entity.StatutIntervention;
import com.gmao.entity.Utilisateur;
import com.gmao.service.repository.InterventionRepository;
import com.gmao.service.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Contrôleur simulant un flux de données en temps réel de géolocalisation.
 *
 * <p>⚠️ La position de chaque technicien est désormais simulée AUTOUR DU SITE
 * de sa prochaine intervention non réalisée (au lieu d'un point fixe arbitraire
 * centré sur Paris), pour que la carte reste géographiquement cohérente avec
 * les sites d'intervention réels — y compris ceux situés hors de la région
 * parisienne (ex: Lyon dans les données de test).</p>
 *
 * <p>NOTE D'INTÉGRATION : nécessite
 * {@code List<Intervention> findByTechnicienId(Integer technicienId)} sur
 * {@code InterventionRepository} — probablement déjà présent puisqu'utilisé
 * par {@code InterventionController.listerParTechnicien(...)}.</p>
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final UtilisateurRepository utilisateurRepository;
    private final InterventionRepository interventionRepository;
    private final Random random = new Random();

    /** Point de repli si un technicien n'a aucune intervention en attente (ex: siège/dépôt). */
    private static final double FALLBACK_LAT = 48.8566;
    private static final double FALLBACK_LNG = 2.3522;

    /** Rayon de dispersion (~1km) autour du point d'ancrage, pour simuler un technicien "en approche". */
    private static final double RAYON_DISPERSION = 0.01;

    @GetMapping("/techniciens")
    public ResponseEntity<List<TechnicienLocation>> getLocationsTechniciens() {
        List<TechnicienLocation> locations = utilisateurRepository
                .findByRoleAndActifTrue(RoleUtilisateur.TECHNICIEN)
                .stream()
                .map(this::simulerPosition)
                .toList();

        return ResponseEntity.ok(locations);
    }

    private TechnicienLocation simulerPosition(Utilisateur technicien) {
        Intervention prochaine = trouverProchaineIntervention(technicien);

        double[] ancrage = prochaine != null
                ? parseGps(prochaine.getEquipement().getSite().getCoordonneesGps())
                : new double[]{FALLBACK_LAT, FALLBACK_LNG};

        double lat = ancrage[0] + (random.nextDouble() - 0.5) * RAYON_DISPERSION;
        double lng = ancrage[1] + (random.nextDouble() - 0.5) * RAYON_DISPERSION;

        TechnicienLocation.TechnicienLocationBuilder builder = TechnicienLocation.builder()
                .technicienId(technicien.getId())
                .nom(technicien.getNom())
                .latitude(String.format(Locale.US, "%.5f", lat))
                .longitude(String.format(Locale.US, "%.5f", lng))
                .derniereMiseAJour(LocalDateTime.now());

        if (prochaine != null) {
            builder.destinationLat(ancrage[0])
                   .destinationLng(ancrage[1])
                   .destinationSiteNom(prochaine.getEquipement().getSite().getNom())
                   .interventionCibleId(prochaine.getId())
                   .interventionCibleTitre(prochaine.getTitre())
                   .interventionCibleStatut(prochaine.getStatut().name());
        }

        return builder.build();
    }

    /** Prochaine intervention non réalisée du technicien, triée par date de début (les non-datées en dernier). */
    private Intervention trouverProchaineIntervention(Utilisateur technicien) {
        List<Intervention> interventions = interventionRepository.findByTechnicienId(technicien.getId());

        return interventions.stream()
                .filter(i -> i.getStatut() != StatutIntervention.REALISE)
                .filter(i -> i.getEquipement() != null && i.getEquipement().getSite() != null)
                .filter(i -> i.getEquipement().getSite().getCoordonneesGps() != null
                        && !i.getEquipement().getSite().getCoordonneesGps().isBlank())
                .min(Comparator.comparing(Intervention::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private double[] parseGps(String gps) {
        try {
            String[] parts = gps.split(",");
            return new double[]{Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim())};
        } catch (Exception e) {
            log.warn("Coordonnées GPS invalides ignorées : \"{}\"", gps);
            return new double[]{FALLBACK_LAT, FALLBACK_LNG};
        }
    }
}