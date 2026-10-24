package com.gmao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Position (simulée ou réelle) d'un technicien, enrichie de la destination
 * vers laquelle il se dirige (site de sa prochaine intervention non réalisée),
 * pour permettre à la carte de tracer le trajet sans avoir à recalculer
 * cette information côté frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicienLocation {

    private Integer technicienId;
    private String nom;
    private String latitude;
    private String longitude;
    private LocalDateTime derniereMiseAJour;

    // ── Destination (null si le technicien n'a aucune intervention en attente) ──
    private Double destinationLat;
    private Double destinationLng;
    private String destinationSiteNom;
    private Integer interventionCibleId;
    private String interventionCibleTitre;
    private String interventionCibleStatut; // A_FAIRE | EN_COURS
}