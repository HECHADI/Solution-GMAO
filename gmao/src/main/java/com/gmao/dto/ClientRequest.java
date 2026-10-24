package com.gmao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRequest {

    @NotBlank(message = "Le nom du client est obligatoire")
    private String nom;

    private String adresse;

    private String telephone;

    @Email(message = "Format d'email invalide")
    private String email;
}