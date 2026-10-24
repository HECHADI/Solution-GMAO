package com.gmao.dto;

import com.gmao.entity.Site;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SiteResponse {

    private Integer id;
    private String nom;
    private String adresse;
    private Integer clientId;
    private String clientNom;

    public static SiteResponse from(Site site) {
        var client = site.getClient();
        return SiteResponse.builder()
                .id(site.getId())
                .nom(site.getNom())
                .adresse(site.getAdresse())
                .clientId(client != null ? client.getId() : null)
                .clientNom(client != null ? client.getNom() : null)
                .build();
    }
}
