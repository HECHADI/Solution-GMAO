package com.gmao.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParametreRequest {

    @NotBlank(message = "La valeur est obligatoire")
    private String valeur;
}
