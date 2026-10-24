package com.gmao.dto;

import com.gmao.entity.FactureLigne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureLigneResponse {

    private Integer id;
    private Integer interventionId;
    private String interventionTitre;
    private BigDecimal coutPieces;
    private BigDecimal coutMainOeuvre;
    private BigDecimal coutTotal;

    public static FactureLigneResponse from(FactureLigne ligne) {
        return FactureLigneResponse.builder()
                .id(ligne.getId())
                .interventionId(ligne.getIntervention() != null ? ligne.getIntervention().getId() : null)
                .interventionTitre(ligne.getIntervention() != null ? ligne.getIntervention().getTitre() : null)
                .coutPieces(ligne.getCoutPieces())
                .coutMainOeuvre(ligne.getCoutMainOeuvre())
                .coutTotal(ligne.getCoutTotal())
                .build();
    }
}
