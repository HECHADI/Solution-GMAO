package com.gmao.dto;

import com.gmao.entity.Equipement;
import com.gmao.entity.Intervention;
import com.gmao.entity.PlanMaintenance;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class EquipementDetailResponse {

    private EquipementResponse equipement;
    private List<EquipementInterventionResume> historiqueInterventions;
    private List<PlanMaintenanceResponse> plansMaintenance;

    public static EquipementDetailResponse from(
            Equipement equipement, List<Intervention> interventions, List<PlanMaintenance> plans) {

        return EquipementDetailResponse.builder()
                .equipement(EquipementResponse.from(equipement))
                .historiqueInterventions(interventions.stream()
                        .map(EquipementInterventionResume::from)
                        .collect(Collectors.toList()))
                .plansMaintenance(plans.stream()
                        .map(PlanMaintenanceResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
