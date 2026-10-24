package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente la quantité d'une pièce spécifique embarquée dans le véhicule d'un technicien.
 */
@Entity
@Table(name = "stockvehicule", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"technicien_id", "piece_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockVehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_id", nullable = false)
    private Utilisateur technicien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false)
    private PieceDetachee piece;

    @Column(nullable = false)
    private Integer quantite;
}
