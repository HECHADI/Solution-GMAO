package com.gmao.dto;

import com.gmao.entity.Intervention;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterventionAFacturerResponse {

    private Integer id;
    private String titre;
    private Integer clientId;
    private String clientNom;
    private Integer chantierId;
    private String chantierNom;
    private String equipementNom;
    private LocalDate dateRealisation;
    private BigDecimal coutPiecesEstime;
    private BigDecimal coutMainOeuvreEstime;
    private BigDecimal coutTotalEstime;

    // Construite côté service (FactureService.calculerCoutIntervention),
    // car elle nécessite l'agrégation des PieceUtilisee + le tarif horaire.
    public static InterventionAFacturerResponse of(
            Intervention intervention,
            BigDecimal coutPieces,
            BigDecimal coutMainOeuvre
    ) {
        return InterventionAFacturerResponse.builder()
                .id(intervention.getId())
                .titre(intervention.getTitre())
                .clientId(intervention.getEquipement() != null && intervention.getEquipement().getSite() != null
                        ? intervention.getEquipement().getSite().getClient().getId() : null)
                .clientNom(intervention.getEquipement() != null && intervention.getEquipement().getSite() != null
                        ? intervention.getEquipement().getSite().getClient().getNom() : null)
                .chantierId(intervention.getChantier() != null ? intervention.getChantier().getId() : null)
                .chantierNom(intervention.getChantier() != null ? intervention.getChantier().getNom() : null)
                .equipementNom(intervention.getEquipement() != null ? intervention.getEquipement().getNom() : null)
                .dateRealisation(intervention.getDateFin() != null ? intervention.getDateFin().toLocalDate() : null)
                .coutPiecesEstime(coutPieces)
                .coutMainOeuvreEstime(coutMainOeuvre)
                .coutTotalEstime(coutPieces.add(coutMainOeuvre))
                .build();
    }
}
