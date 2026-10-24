package com.gmao.dto;

import com.gmao.entity.RoleUtilisateur;
import com.gmao.entity.Utilisateur;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UtilisateurResponse {

    private Integer id;
    private String nom;
    private String email;
    private RoleUtilisateur role;
    private boolean actif;
    private LocalDateTime createdAt;

    public static UtilisateurResponse from(Utilisateur utilisateur) {
        return UtilisateurResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .actif(utilisateur.isActif())
                .createdAt(utilisateur.getCreatedAt())
                .build();
    }
}
