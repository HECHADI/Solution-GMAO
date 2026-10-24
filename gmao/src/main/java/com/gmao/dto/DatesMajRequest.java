package com.gmao.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO pour mettre à jour les dates d'une intervention.
 * Utilisé en corps de requête pour {@code PATCH /api/interventions/{id}/dates}.
 *
 * <p>Au moins l'une des deux dates doit être fournie.
 * Si les deux sont présentes, {@code dateDebut} doit être antérieure à {@code dateFin}.</p>
 */
@Data
public class DatesMajRequest {

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    /** Durée estimée en minutes (optionnelle, mise à jour simultanée). */
    private Integer dureeEstimee;

    // ── Validation croisée ────────────────────────────────────────────────────

    @AssertTrue(message = "Au moins une date (dateDebut ou dateFin) doit être fournie")
    public boolean isAtLeastOneDateProvided() {
        return dateDebut != null || dateFin != null;
    }

    @AssertTrue(message = "La date de début doit être antérieure à la date de fin")
    public boolean isDatesConsistentes() {
        if (dateDebut != null && dateFin != null) {
            return dateDebut.isBefore(dateFin);
        }
        return true; // Si une seule date, pas de conflit possible
    }
}
