package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Chantier (ou projet complexe) regroupant plusieurs {@link Intervention}
 * sur un même site. Permet un suivi consolidé (avancement, budget) plutôt
 * que de traiter chaque intervention isolément.
 */
@Entity
@Table(
    name = "Chantier",
    indexes = {
        @Index(name = "idx_chantier_site_id", columnList = "site_id"),
        @Index(name = "idx_chantier_statut", columnList = "statut"),
        @Index(name = "idx_chantier_responsable", columnList = "responsable_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"site", "responsable"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Chantier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "nom", nullable = false, length = 200)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "site_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_chantier_site")
    )
    private Site site;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutChantier statut = StatutChantier.PLANIFIE;

    @Column(name = "date_debut_prevue")
    private LocalDate dateDebutPrevue;

    @Column(name = "date_fin_prevue")
    private LocalDate dateFinPrevue;

    @Column(name = "budget_estime", precision = 10, scale = 2)
    private BigDecimal budgetEstime;

    /** Responsable du chantier, généralement un utilisateur MANAGER. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "responsable_id",
        foreignKey = @ForeignKey(name = "fk_chantier_responsable")
    )
    private Utilisateur responsable;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
