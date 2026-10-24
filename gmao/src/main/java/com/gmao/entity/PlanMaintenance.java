package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Plan de maintenance préventive récurrente sur un équipement.
 *
 * <p>Contrairement à {@link Intervention} qui représente une occurrence
 * concrète (un ordre de travail daté), {@code PlanMaintenance} porte la
 * fréquence de récurrence (ex: "tous les 3 mois") et l'échéance calculée.
 * Chaque exécution génère une {@link Intervention} de type {@code PREVENTIF}
 * liée à ce plan via {@code plan_maintenance_id}.</p>
 */
@Entity
@Table(
    name = "planmaintenance",
    indexes = {
        @Index(name = "idx_plan_equipement_id", columnList = "equipement_id"),
        @Index(name = "idx_plan_actif", columnList = "actif"),
        @Index(name = "idx_plan_prochaine_echeance", columnList = "prochaine_echeance")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "equipement")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PlanMaintenance {

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
        name = "equipement_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_plan_equipement")
    )
    private Equipement equipement;

    /** Valeur numérique de la fréquence (ex: 3 pour "tous les 3 mois"). */
    @Column(name = "frequence_valeur", nullable = false)
    private Integer frequenceValeur;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequence_unite", nullable = false, length = 20)
    @Builder.Default
    private FrequenceUnite frequenceUnite = FrequenceUnite.MOIS;

    /** Durée estimée en minutes, reportée sur les interventions générées. */
    @Column(name = "duree_estimee")
    private Integer dureeEstimee;

    /** Renseigné si ce plan correspond à un contrôle réglementaire (ex: vérification électrique). */
    @Column(name = "type_reglementaire", length = 150)
    private String typeReglementaire;

    /** Nombre de jours avant l'échéance à partir duquel un ordre de travail peut être généré. */
    @Column(name = "delai_alerte_jours", nullable = false)
    @Builder.Default
    private Integer delaiAlerteJours = 15;

    @Column(name = "derniere_execution")
    private LocalDate derniereExecution;

    @Column(name = "prochaine_echeance")
    private LocalDate prochaineEcheance;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private boolean actif = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Logique métier ──────────────────────────────────────────────────────

    /**
     * Recalcule {@code derniereExecution} et {@code prochaineEcheance} à partir
     * d'une date de réalisation effective (à appeler quand l'intervention liée
     * passe au statut REALISE).
     */
    public void recalculerEcheance(LocalDate dateRealisation) {
        this.derniereExecution = dateRealisation;
        this.prochaineEcheance = switch (this.frequenceUnite) {
            case JOUR    -> dateRealisation.plusDays(frequenceValeur);
            case SEMAINE -> dateRealisation.plusWeeks(frequenceValeur);
            case MOIS    -> dateRealisation.plusMonths(frequenceValeur);
            case ANNEE   -> dateRealisation.plusYears(frequenceValeur);
        };
    }

    /**
     * Indique si ce plan est éligible à la génération automatique d'un ordre
     * de travail à la date donnée (actif, échéance connue et dans la fenêtre
     * d'alerte).
     */
    public boolean eligibleGenerationAutomatique(LocalDate aujourdHui) {
        if (!actif || prochaineEcheance == null) return false;
        return !prochaineEcheance.minusDays(delaiAlerteJours).isAfter(aujourdHui);
    }
}
