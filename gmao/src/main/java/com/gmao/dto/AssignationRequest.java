package com.gmao.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO pour assigner un technicien à une intervention existante.
 * Utilisé en corps de requête pour {@code PATCH /api/interventions/{id}/assigner}.
 */
@Data
public class AssignationRequest {

    @NotNull(message = "L'identifiant du technicien est obligatoire")
    private Integer technicienId;
}
