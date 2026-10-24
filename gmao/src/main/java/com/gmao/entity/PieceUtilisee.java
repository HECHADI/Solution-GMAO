package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité pivot représentant une pièce détachée consommée/utilisée
 * par un technicien lors d'une intervention spécifique.
 */
@Entity
@Table(name = "pieceutilisee") // En minuscules pour correspondre à phpMyAdmin
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceUtilisee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** L'intervention durant laquelle la pièce a été posée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "intervention_id", 
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_pieceutilisee_intervention")
    )
    private Intervention intervention;

    /** La pièce du catalogue qui a été utilisée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "piece_id", 
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_pieceutilisee_piece")
    )
    private PieceDetachee pieceDetachee;

    /** La quantité exacte consommée par le technicien. */
    @Column(name = "quantite", nullable = false)
    private Integer quantite;
}