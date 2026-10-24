package com.gmao.dto;

import com.gmao.entity.StockVehicule;
import lombok.Builder;
import lombok.Getter;

/**
 * Représentation exposée par l'API pour une ligne de {@code StockVehicule}.
 * Le statut d'alerte est calculé côté serveur (comparaison quantité / seuil
 * de la pièce), pour éviter de dupliquer ce seuil côté frontend.
 */
@Getter
@Builder
public class StockVehiculeResponse {

    private Integer id;

    private Integer technicienId;
    private String technicienNom;

    private Integer pieceId;
    private String pieceReference;
    private String pieceNom;
    private Double prixUnitaire;
    private Integer seuilAlerte;

    private Integer quantite;
    private boolean critique;
    private Double valeurStock; // quantite * prixUnitaire

    public static StockVehiculeResponse from(StockVehicule stock) {
        var piece = stock.getPiece();
        double prix = piece.getPrixUnitaire() != null ? piece.getPrixUnitaire() : 0.0;

        return StockVehiculeResponse.builder()
                .id(stock.getId())
                .technicienId(stock.getTechnicien().getId())
                .technicienNom(stock.getTechnicien().getNom())
                .pieceId(piece.getId())
                .pieceReference(piece.getReference())
                .pieceNom(piece.getNom())
                .prixUnitaire(piece.getPrixUnitaire())
                .seuilAlerte(piece.getSeuilAlerte())
                .quantite(stock.getQuantite())
                .critique(stock.getQuantite() <= piece.getSeuilAlerte())
                .valeurStock(stock.getQuantite() * prix)
                .build();
    }
}
