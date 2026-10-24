package com.gmao.dto;

import com.gmao.entity.FrequenceUnite;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Corps de requête pour la création/modification d'un {@code PlanMaintenance}.
 */
@Getter
@Setter
public class PlanMaintenanceRequest {

    @NotBlank(message = "Le nom du plan est obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "L'équipement est obligatoire")
    private Integer equipementId;

    @NotNull(message = "La fréquence est obligatoire")
    @Positive(message = "La fréquence doit être positive")
    private Integer frequenceValeur;

    @NotNull(message = "L'unité de fréquence est obligatoire")
    private FrequenceUnite frequenceUnite;

    private Integer dureeEstimee;

    private String typeReglementaire;

    @Min(value = 0, message = "Le délai d'alerte ne peut pas être négatif")
    private Integer delaiAlerteJours;

    private Boolean actif;
}
