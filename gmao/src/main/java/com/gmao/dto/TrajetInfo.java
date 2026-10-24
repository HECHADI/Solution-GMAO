package com.gmao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Résultat de l'appel à l'API de cartographie.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrajetInfo {
    private Integer distanceKm;
    private Integer dureeMinutes;
}
