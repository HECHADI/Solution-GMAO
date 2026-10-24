package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Trace un mouvement de stock (approvisionnement, consommation, transfert,
 * ajustement) sur un véhicule technicien. Chaque ligne est immuable — on
 * n'édite jamais un mouvement, on en crée un nouveau pour corriger.
 */
@Entity
@Table(
    name = "mouvementstock",
    indexes = {
        @Index(name = "idx_mouvement_technicien_id", columnList = "technicien_id"),
        @Index(name = "idx_mouvement_piece_id", columnList = "piece_id"),
        @Index(name = "idx_mouvement_type", columnList = "type_mouvement"),
        @Index(name = "idx_mouvement_created_at", columnList = "created_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"technicien", "piece", "technicienSource", "intervention", "utilisateur"})
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Véhicule (technicien) concerné par le mouvement. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technicien_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_mouvement_technicien"))
    private Utilisateur technicien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "piece_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_mouvement_piece"))
    private PieceDetachee piece;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false, length = 30)
    private TypeMouvementStock typeMouvement;

    /** Toujours positif — le type du mouvement indique le sens (entrée/sortie). */
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    /** Renseigné uniquement pour TRANSFERT_ENTRANT : d'où vient la pièce. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_source_id",
        foreignKey = @ForeignKey(name = "fk_mouvement_technicien_source"))
    private Utilisateur technicienSource;

    /** Renseigné uniquement pour CONSOMMATION : quelle intervention a utilisé la pièce. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intervention_id",
        foreignKey = @ForeignKey(name = "fk_mouvement_intervention"))
    private Intervention intervention;

    /** Utilisateur (généralement un manager) qui a déclenché le mouvement manuellement. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id",
        foreignKey = @ForeignKey(name = "fk_mouvement_utilisateur"))
    private Utilisateur utilisateur;

    @Column(name = "commentaire", length = 255)
    private String commentaire;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
