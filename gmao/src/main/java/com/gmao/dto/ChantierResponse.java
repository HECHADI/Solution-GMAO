package com.gmao.dto;

import com.gmao.entity.Chantier;
import com.gmao.entity.Intervention;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChantierResponse {
    private Integer id;
    private String nom;
    private String description;
    private Integer siteId;
    private String siteNom;
    private String clientNom;
    private String statut;
    private LocalDate dateDebutPrevue;
    private LocalDate dateFinPrevue;
    private BigDecimal budgetEstime;
    private Integer responsableId;
    private String responsableNom;
    private int avancementPourcentage;
    private List<ChantierInterventionResponse> interventions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChantierResponse from(Chantier chantier, List<Intervention> interventionsLiees) {
        long total = interventionsLiees.size();
        long realisees = interventionsLiees.stream()
                .filter(i -> "REALISE".equals(i.getStatut().name()))
                .count();
        int pourcentage = total == 0 ? 0 : (int) ((realisees * 100) / total);

        return ChantierResponse.builder()
                .id(chantier.getId())
                .nom(chantier.getNom())
                .description(chantier.getDescription())
                .siteId(chantier.getSite() != null ? chantier.getSite().getId() : null)
                .siteNom(chantier.getSite() != null ? chantier.getSite().getNom() : null)
                .clientNom((chantier.getSite() != null && chantier.getSite().getClient() != null) ? chantier.getSite().getClient().getNom() : null)
                .statut(chantier.getStatut() != null ? chantier.getStatut().name() : null)
                .dateDebutPrevue(chantier.getDateDebutPrevue())
                .dateFinPrevue(chantier.getDateFinPrevue())
                .budgetEstime(chantier.getBudgetEstime())
                .responsableId(chantier.getResponsable() != null ? chantier.getResponsable().getId() : null)
                .responsableNom(chantier.getResponsable() != null ? chantier.getResponsable().getNom() : null)
                .avancementPourcentage(pourcentage)
                .interventions(interventionsLiees.stream().map(ChantierInterventionResponse::from).collect(Collectors.toList()))
                .createdAt(chantier.getCreatedAt())
                .updatedAt(chantier.getUpdatedAt())
                .build();
    }
}