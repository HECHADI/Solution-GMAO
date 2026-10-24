package com.gmao.dto;

import com.gmao.entity.StatutEquipement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class EquipementRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    private String categorie;
    private String marque;
    private String modele;
    private String numeroSerie;
    private LocalDate dateInstallation;
    @NotNull(message = "Le site est obligatoire")
    private Integer siteId;
    private StatutEquipement statut;
}