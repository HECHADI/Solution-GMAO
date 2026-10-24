package com.gmao.dto;

import com.gmao.entity.Intervention;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EquipementInterventionResume {

    private Integer id;
    private String titre;
    private String type;
    private String statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String technicienNom;

    public static EquipementInterventionResume from(Intervention i) {
        return EquipementInterventionResume.builder()
                .id(i.getId())
                .titre(i.getTitre())
                .type(i.getType().name())
                .statut(i.getStatut().name())
                .dateDebut(i.getDateDebut())
                .dateFin(i.getDateFin())
                .technicienNom(i.getTechnicien() != null ? i.getTechnicien().getNom() : null)
                .build();
    }
}
