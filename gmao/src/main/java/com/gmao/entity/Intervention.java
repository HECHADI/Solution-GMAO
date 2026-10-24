package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une intervention de maintenance (préventive ou curative)
 * réalisée sur un équipement par un technicien.
 */
@Entity
@Table(
    name = "intervention",
    indexes = {
        @Index(name = "idx_intervention_equipement_id", columnList = "equipement_id"),
        @Index(name = "idx_intervention_technicien_id", columnList = "technicien_id"),
        @Index(name = "idx_intervention_statut",         columnList = "statut"),
        @Index(name = "idx_intervention_plan_id",       columnList = "plan_maintenance_id"),
        @Index(name = "idx_intervention_chantier_id",   columnList = "chantier_id") // Index ajouté pour le chantier
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 🟢 Ajout de "facture" dans l'exclusion du ToString pour éviter la récursion infinie
@ToString(exclude = {"equipement", "technicien", "planMaintenance", "chantier", "piecesUtilisees", "facture"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TypeIntervention type;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutIntervention statut = StatutIntervention.A_FAIRE;

    /** Date/heure de début effective de l'intervention. */
    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    /** Date/heure de fin effective de l'intervention. */
    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    /** Durée estimée en minutes. */
    @Column(name = "duree_estimee")
    private Integer dureeEstimee;

    /** Distance en kilomètres depuis l'intervention précédente. */
    @Column(name = "distance_km")
    private Integer distanceKm;

    /** Temps de trajet estimé en minutes depuis l'intervention précédente. */
    @Column(name = "temps_trajet_minutes")
    private Integer tempsTrajetMinutes;

    // ── Données de clôture ────────────────────────────────────────────────────

    @Column(name = "compte_rendu", columnDefinition = "TEXT")
    private String compteRendu;

    @Column(name = "signature_base64", columnDefinition = "LONGTEXT")
    private String signatureBase64;

    // ── Horodatage automatique ────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Relations ─────────────────────────────────────────────────────────────

    /**
     * Équipement concerné par l'intervention (obligatoire).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "equipement_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_intervention_equipement")
    )
    private Equipement equipement;

    /**
     * Technicien assigné à l'intervention.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "technicien_id",
        foreignKey = @ForeignKey(name = "fk_intervention_technicien")
    )
    private Utilisateur technicien;

    /**
     * Plan de maintenance préventive à l'origine de cette intervention.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "plan_maintenance_id",
        foreignKey = @ForeignKey(name = "fk_intervention_plan")
    )
    private PlanMaintenance planMaintenance;

    /**
     * 🟢 CHANTIER lié à cette intervention (nullable).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "chantier_id",
        foreignKey = @ForeignKey(name = "fk_intervention_chantier")
    )
    private Chantier chantier;

    /**
     * 🟢 PIÈCES UTILISÉES lors de cette intervention (pour le calcul de la facture).
     */
    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PieceUtilisee> piecesUtilisees = new ArrayList<>();

    /**
     * 🟢 FACTURE liée à cette intervention (nullable).
     * Ajouté pour l'interface de facturation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "facture_id",
        foreignKey = @ForeignKey(name = "fk_intervention_facture")
    )
    private Facture facture;
}