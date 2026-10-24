package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant un équipement installé sur un site client.
 */
@Entity
@Table(
    name = "Equipement",
    uniqueConstraints = @UniqueConstraint(name = "uq_equipement_serie", columnNames = "numero_serie"),
    indexes = {
        @Index(name = "idx_equipement_site_id", columnList = "site_id"),
        @Index(name = "idx_equipement_statut",  columnList = "statut")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "site")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "categorie", length = 100)
    private String categorie;

    @Column(name = "marque", length = 100)
    private String marque;

    @Column(name = "modele", length = 100)
    private String modele;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "date_installation")
    private LocalDate dateInstallation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutEquipement statut = StatutEquipement.EN_SERVICE;

    // ── Horodatage automatique ────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Relations ─────────────────────────────────────────────────────────────

    /** Site sur lequel l'équipement est installé (Many-to-One obligatoire). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "site_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_equipement_site")
    )
    private Site site;
}
