package com.gmao.dto;

import com.gmao.entity.Client;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClientResponse {

    private Integer id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private LocalDateTime createdAt;

    public static ClientResponse from(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .nom(client.getNom())
                .adresse(client.getAdresse())
                .telephone(client.getTelephone())
                .email(client.getEmail())
                .createdAt(client.getCreatedAt())
                .build();
    }
}