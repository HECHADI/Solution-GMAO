package com.gmao.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Alerte agrégée pour le centre de notifications. N'est jamais persistée :
 * calculée à la volée à partir de l'état actuel des autres modules
 * (Stock, Maintenance, Équipements, Chantiers).
 */
@Getter
@Builder
public class NotificationItem {

    private String id;           // identifiant stable synthétique, ex: "stock-42"
    private String type;         // STOCK_CRITIQUE | MAINTENANCE_RETARD | MAINTENANCE_BIENTOT | EQUIPEMENT_PANNE | CHANTIER_RETARD
    private String gravite;      // CRITIQUE | ATTENTION
    private String titre;
    private String message;
    private String page;         // page du sidebar vers laquelle naviguer au clic (ex: "stock")
    private Integer entiteId;    // id de l'entité source, pour un lien plus précis si besoin
}
