package com.gmao.dto;

import com.gmao.entity.TypeIntervention;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de création d'une intervention.
 * Utilisé en corps de requête pour {@code POST /api/interventions}.
 */
@Data
public class InterventionRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    private String titre;

    @Size(max = 5000, message = "La description ne peut pas dépasser 5000 caractères")
    private String description;

    @NotNull(message = "Le type d'intervention est obligatoire (PREVENTIF ou CURATIF)")
    private TypeIntervention type;

    /** Date/heure de début prévue (optionnelle à la création). */
    private LocalDateTime dateDebut;

    /** Date/heure de fin prévue (optionnelle à la création). */
    private LocalDateTime dateFin;

    /** Durée estimée en minutes. */
    @Min(value = 1, message = "La durée estimée doit être d'au moins 1 minute")
    private Integer dureeEstimee;

    @NotNull(message = "L'identifiant de l'équipement est obligatoire")
    private Integer equipementId;

    /** Technicien assigné dès la création (optionnel). */
    private Integer technicienId;
}
