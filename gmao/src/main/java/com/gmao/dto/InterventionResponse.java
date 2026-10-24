package com.gmao.dto;

import com.gmao.entity.Intervention;
import com.gmao.entity.StatutIntervention;
import com.gmao.entity.TypeIntervention;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse exposé par le contrôleur REST pour une intervention.
 * Évite d'exposer directement les entités JPA (et leurs relations lazy).
 */
@Data
@Builder
public class InterventionResponse {

    private Integer id;
    private String  titre;
    private String  description;
    private TypeIntervention   type;
    private StatutIntervention statut;
    private LocalDateTime      dateDebut;
    private LocalDateTime      dateFin;
    private Integer            dureeEstimee;
    private Integer            distanceKm;
    private Integer            tempsTrajetMinutes;
    private LocalDateTime      createdAt;
    private LocalDateTime      updatedAt;

    // ── Résumé équipement et site ─────────────────────────────────────────────
    private Integer equipementId;
    private String  equipementNom;
    private String  equipementNumeroSerie;
    private String  siteNom;
    private String  siteGps;

    // ── Résumé technicien ─────────────────────────────────────────────────────
    private Integer technicienId;
    private String  technicienNom;
    private String  technicienEmail;

    // ── Factory method ────────────────────────────────────────────────────────

    /**
     * Convertit une entité {@link Intervention} en {@link InterventionResponse}.
     * Les relations LAZY doivent avoir été chargées avant l'appel (JOIN FETCH).
     */
    public static InterventionResponse fromEntity(Intervention intervention) {
        InterventionResponseBuilder builder = InterventionResponse.builder()
            .id(intervention.getId())
            .titre(intervention.getTitre())
            .description(intervention.getDescription())
            .type(intervention.getType())
            .statut(intervention.getStatut())
            .dateDebut(intervention.getDateDebut())
            .dateFin(intervention.getDateFin())
            .dureeEstimee(intervention.getDureeEstimee())
            .distanceKm(intervention.getDistanceKm())
            .tempsTrajetMinutes(intervention.getTempsTrajetMinutes())
            .createdAt(intervention.getCreatedAt())
            .updatedAt(intervention.getUpdatedAt());

        // Équipement et site (toujours présent)
        if (intervention.getEquipement() != null) {
            builder
                .equipementId(intervention.getEquipement().getId())
                .equipementNom(intervention.getEquipement().getNom())
                .equipementNumeroSerie(intervention.getEquipement().getNumeroSerie());

            if (intervention.getEquipement().getSite() != null) {
                builder
                    .siteNom(intervention.getEquipement().getSite().getNom())
                    .siteGps(intervention.getEquipement().getSite().getCoordonneesGps());
            }
        }

        // Technicien (optionnel)
        if (intervention.getTechnicien() != null) {
            builder
                .technicienId(intervention.getTechnicien().getId())
                .technicienNom(intervention.getTechnicien().getNom())
                .technicienEmail(intervention.getTechnicien().getEmail());
        }

        return builder.build();
    }
}
