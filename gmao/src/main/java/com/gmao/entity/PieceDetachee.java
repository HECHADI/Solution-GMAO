package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catalogue global des pièces détachées.
 */
@Entity
@Table(name = "piecedetachee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceDetachee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @Column(nullable = false)
    private String nom;

    @Column(name = "prix_unitaire")
    private Double prixUnitaire;

    /** Quantité (par véhicule) en dessous de laquelle le stock est considéré critique. */
    @Column(name = "seuil_alerte", nullable = false)
    @Builder.Default
    private Integer seuilAlerte = 5;
}
