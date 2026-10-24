package com.gmao.dto;

import com.gmao.entity.Intervention;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChantierInterventionResponse {
    private Integer id;
    private String titre;
    private String type;
    private String statut;
    private String technicienNom;

    public static ChantierInterventionResponse from(Intervention i) {
        return ChantierInterventionResponse.builder()
                .id(i.getId())
                .titre(i.getTitre())
                .type(i.getType() != null ? i.getType().name() : null)
                .statut(i.getStatut() != null ? i.getStatut().name() : null)
                .technicienNom(i.getTechnicien() != null ? i.getTechnicien().getNom() : null)
                .build();
    }
}