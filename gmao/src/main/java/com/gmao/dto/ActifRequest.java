package com.gmao.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActifRequest {

    @NotNull(message = "Le champ actif est obligatoire")
    private Boolean actif;
}
