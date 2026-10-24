package com.gmao.dto;

import com.gmao.entity.Facture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureResponse {

    private Integer id;
    private String numero;
    private Integer clientId;
    private String clientNom;
    private Integer chantierId;
    private String chantierNom;
    private String statut;
    private BigDecimal montantPieces;
    private BigDecimal montantMainOeuvre;
    private BigDecimal montantTotal;
    private LocalDate dateEmission;
    private LocalDate dateEcheance;
    private LocalDate datePaiement;
    private List<FactureLigneResponse> lignes;

    public static FactureResponse from(Facture facture) {
        return FactureResponse.builder()
                .id(facture.getId())
                .numero(facture.getNumero())
                .clientId(facture.getClient() != null ? facture.getClient().getId() : null)
                .clientNom(facture.getClient() != null ? facture.getClient().getNom() : null)
                .chantierId(facture.getChantier() != null ? facture.getChantier().getId() : null)
                .chantierNom(facture.getChantier() != null ? facture.getChantier().getNom() : null)
                .statut(facture.getStatut() != null ? facture.getStatut().name() : null)
                .montantPieces(facture.getMontantPieces())
                .montantMainOeuvre(facture.getMontantMainOeuvre())
                .montantTotal(facture.getMontantTotal())
                .dateEmission(facture.getDateEmission())
                .dateEcheance(facture.getDateEcheance())
                .datePaiement(facture.getDatePaiement())
                .lignes(facture.getLignes() == null ? List.of() : facture.getLignes().stream()
                        .map(FactureLigneResponse::from)
                        .toList())
                .build();
    }
}
