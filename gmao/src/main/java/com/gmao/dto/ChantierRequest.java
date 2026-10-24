package com.gmao.dto;

import com.gmao.entity.StatutChantier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ChantierRequest {
    @NotBlank(message = "Le nom du chantier est obligatoire")
    private String nom;
    private String description;
    @NotNull(message = "Le site est obligatoire")
    private Integer siteId;
    private StatutChantier statut;
    private LocalDate dateDebutPrevue;
    private LocalDate dateFinPrevue;
    private BigDecimal budgetEstime;
    private Integer responsableId;
}