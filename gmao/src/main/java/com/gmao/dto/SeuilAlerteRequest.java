package com.gmao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeuilAlerteRequest {

    @NotNull(message = "Le seuil d'alerte est obligatoire")
    @Min(value = 0, message = "Le seuil d'alerte ne peut pas être négatif")
    private Integer seuilAlerte;
}
