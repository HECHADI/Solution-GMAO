package com.gmao.dto;

import com.gmao.entity.ParametreApplication;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ParametreResponse {

    private Integer id;
    private String cle;
    private String valeur;
    private String description;
    private LocalDateTime updatedAt;

    public static ParametreResponse from(ParametreApplication p) {
        return ParametreResponse.builder()
                .id(p.getId())
                .cle(p.getCle())
                .valeur(p.getValeur())
                .description(p.getDescription())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
