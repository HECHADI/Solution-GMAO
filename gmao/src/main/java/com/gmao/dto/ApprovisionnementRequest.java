package com.gmao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovisionnementRequest {

    @NotNull(message = "Le technicien est obligatoire")
    private Integer technicienId;

    @NotNull(message = "La pièce est obligatoire")
    private Integer pieceId;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private Integer quantite;

    /** Utilisateur (manager) qui déclenche l'approvisionnement. */
    private Integer utilisateurId;

    private String commentaire;
}
