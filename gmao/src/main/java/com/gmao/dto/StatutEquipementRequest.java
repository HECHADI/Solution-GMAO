package com.gmao.dto;

import com.gmao.entity.StatutEquipement;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatutEquipementRequest {

    @NotNull(message = "Le statut est obligatoire")
    private StatutEquipement statut;
}
