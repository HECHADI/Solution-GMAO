package com.gmao.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureRequest {

    @NotNull(message = "Le client est obligatoire")
    private Integer clientId;

    /** Optionnel : renseigné si la facture concerne un chantier précis. */
    private Integer chantierId;

    @NotEmpty(message = "Sélectionne au moins une intervention à facturer")
    private List<Integer> interventionIds;

    /** Optionnel : échéance de paiement. Par défaut 30 jours après émission côté service. */
    private LocalDate dateEcheance;
}
