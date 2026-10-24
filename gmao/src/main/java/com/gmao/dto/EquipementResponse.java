package com.gmao.dto;

import com.gmao.entity.Equipement;
import com.gmao.entity.StatutEquipement;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class EquipementResponse {

    private Integer id;
    private String nom;
    private String categorie;
    private String marque;
    private String modele;
    private String numeroSerie;
    private LocalDate dateInstallation;
    private StatutEquipement statut;

    private Integer siteId;
    private String siteNom;
    private String clientNom;

    public static EquipementResponse from(Equipement e) {
        var site = e.getSite();
        var client = site != null ? site.getClient() : null;

        return EquipementResponse.builder()
                .id(e.getId())
                .nom(e.getNom())
                .categorie(e.getCategorie())
                .marque(e.getMarque())
                .modele(e.getModele())
                .numeroSerie(e.getNumeroSerie())
                .dateInstallation(e.getDateInstallation())
                .statut(e.getStatut())
                .siteId(site != null ? site.getId() : null)
                .siteNom(site != null ? site.getNom() : null)
                .clientNom(client != null ? client.getNom() : null)
                .build();
    }
}
