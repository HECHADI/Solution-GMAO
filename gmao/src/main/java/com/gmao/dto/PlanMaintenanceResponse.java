package com.gmao.dto;

import com.gmao.entity.FrequenceUnite;
import com.gmao.entity.PlanMaintenance;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Représentation exposée par l'API pour un {@code PlanMaintenance}.
 *
 * <p>Le champ {@code statutUrgence} est calculé côté serveur (RETARD /
 * BIENTOT / A_JOUR / NON_PLANIFIEE) pour éviter de dupliquer cette logique
 * côté frontend — remplace l'estimation qui était faite en React.</p>
 */
@Getter
@Builder
public class PlanMaintenanceResponse {

    private Integer id;
    private String nom;
    private String description;

    private Integer equipementId;
    private String equipementNom;
    private String siteNom;
    private String clientNom;

    private Integer frequenceValeur;
    private FrequenceUnite frequenceUnite;

    private Integer dureeEstimee;
    private String typeReglementaire;
    private Integer delaiAlerteJours;

    private LocalDate derniereExecution;
    private LocalDate prochaineEcheance;
    private Long joursRestants;
    private String statutUrgence; // RETARD | BIENTOT | A_JOUR | NON_PLANIFIEE

    private boolean actif;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PlanMaintenanceResponse from(PlanMaintenance plan) {
        LocalDate echeance = plan.getProchaineEcheance();
        Long joursRestants = null;
        String statutUrgence = "NON_PLANIFIEE";

        if (echeance != null) {
            joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), echeance);
            if (joursRestants < 0) {
                statutUrgence = "RETARD";
            } else if (joursRestants <= plan.getDelaiAlerteJours()) {
                statutUrgence = "BIENTOT";
            } else {
                statutUrgence = "A_JOUR";
            }
        }

        var equipement = plan.getEquipement();
        var site = equipement != null ? equipement.getSite() : null;
        var client = site != null ? site.getClient() : null;

        return PlanMaintenanceResponse.builder()
                .id(plan.getId())
                .nom(plan.getNom())
                .description(plan.getDescription())
                .equipementId(equipement != null ? equipement.getId() : null)
                .equipementNom(equipement != null ? equipement.getNom() : null)
                .siteNom(site != null ? site.getNom() : null)
                .clientNom(client != null ? client.getNom() : null)
                .frequenceValeur(plan.getFrequenceValeur())
                .frequenceUnite(plan.getFrequenceUnite())
                .dureeEstimee(plan.getDureeEstimee())
                .typeReglementaire(plan.getTypeReglementaire())
                .delaiAlerteJours(plan.getDelaiAlerteJours())
                .derniereExecution(plan.getDerniereExecution())
                .prochaineEcheance(echeance)
                .joursRestants(joursRestants)
                .statutUrgence(statutUrgence)
                .actif(plan.isActif())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
