package com.gmao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteRequest {

    @NotBlank(message = "Le nom du site est obligatoire")
    private String nom;

    private String adresse;

    /** Format attendu : "latitude,longitude", ex: "48.8566,2.3522" */
    private String coordonneesGps;

    @NotNull(message = "Le client est obligatoire")
    private Integer clientId;
}
