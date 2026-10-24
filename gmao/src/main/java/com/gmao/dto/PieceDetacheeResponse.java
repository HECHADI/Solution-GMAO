package com.gmao.dto;

import com.gmao.entity.PieceDetachee;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PieceDetacheeResponse {

    private Integer id;
    private String reference;
    private String nom;
    private Double prixUnitaire;
    private Integer seuilAlerte;

    public static PieceDetacheeResponse from(PieceDetachee piece) {
        return PieceDetacheeResponse.builder()
                .id(piece.getId())
                .reference(piece.getReference())
                .nom(piece.getNom())
                .prixUnitaire(piece.getPrixUnitaire())
                .seuilAlerte(piece.getSeuilAlerte())
                .build();
    }
}
