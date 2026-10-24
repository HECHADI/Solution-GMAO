package com.gmao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AjustementRequest {

    @NotNull(message = "Le technicien est obligatoire")
    private Integer technicienId;

    @NotNull(message = "La pièce est obligatoire")
    private Integer pieceId;

    @NotNull(message = "La nouvelle quantité est obligatoire")
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    private Integer nouvelleQuantite;

    private String commentaire;
}
