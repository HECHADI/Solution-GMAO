package com.gmao.dto;

import com.gmao.entity.MouvementStock;
import com.gmao.entity.TypeMouvementStock;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MouvementStockResponse {

    private Integer id;
    private TypeMouvementStock typeMouvement;
    private Integer quantite;

    private String pieceReference;
    private String pieceNom;

    private String technicienNom;
    private String technicienSourceNom;

    private Integer interventionId;
    private String interventionTitre;

    private String utilisateurNom;
    private String commentaire;
    private LocalDateTime createdAt;

    public static MouvementStockResponse from(MouvementStock m) {
        return MouvementStockResponse.builder()
                .id(m.getId())
                .typeMouvement(m.getTypeMouvement())
                .quantite(m.getQuantite())
                .pieceReference(m.getPiece().getReference())
                .pieceNom(m.getPiece().getNom())
                .technicienNom(m.getTechnicien().getNom())
                .technicienSourceNom(m.getTechnicienSource() != null ? m.getTechnicienSource().getNom() : null)
                .interventionId(m.getIntervention() != null ? m.getIntervention().getId() : null)
                .interventionTitre(m.getIntervention() != null ? m.getIntervention().getTitre() : null)
                .utilisateurNom(m.getUtilisateur() != null ? m.getUtilisateur().getNom() : null)
                .commentaire(m.getCommentaire())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
