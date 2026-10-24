package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "factureligne")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;

    @Column(name = "cout_pieces", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal coutPieces = BigDecimal.ZERO;

    @Column(name = "cout_main_oeuvre", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal coutMainOeuvre = BigDecimal.ZERO;

    @Column(name = "cout_total", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal coutTotal = BigDecimal.ZERO;
}
