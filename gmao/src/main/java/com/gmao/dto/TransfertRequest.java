package com.gmao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransfertRequest {

    @NotNull(message = "Le technicien source est obligatoire")
    private Integer technicienSourceId;

    @NotNull(message = "Le technicien destinataire est obligatoire")
    private Integer technicienDestinationId;

    @NotNull(message = "La pièce est obligatoire")
    private Integer pieceId;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private Integer quantite;

    private String commentaire;
}
