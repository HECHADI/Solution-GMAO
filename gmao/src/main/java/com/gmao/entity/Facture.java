package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Facture client générée à partir d'interventions REALISE.
 *
 * <p>Les montants (montantPieces, montantMainOeuvre, montantTotal) sont
 * calculés au moment de la génération par {@code FactureService} et stockés
 * ici (contrairement à l'avancement de Chantier) : une facture émise doit
 * rester figée même si le prix d'une pièce change ensuite en catalogue.</p>
 */
@Entity
@Table(name = "facture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chantier_id")
    private Chantier chantier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutFacture statut = StatutFacture.BROUILLON;

    @Column(name = "montant_pieces", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montantPieces = BigDecimal.ZERO;

    @Column(name = "montant_main_oeuvre", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montantMainOeuvre = BigDecimal.ZERO;

    @Column(name = "montant_total", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "date_emission")
    private LocalDate dateEmission;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "facture", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FactureLigne> lignes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime maintenant = LocalDateTime.now();
        this.createdAt = maintenant;
        this.updatedAt = maintenant;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
