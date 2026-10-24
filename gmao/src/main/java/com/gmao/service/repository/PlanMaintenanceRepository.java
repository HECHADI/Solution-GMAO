package com.gmao.service.repository;

import com.gmao.entity.PlanMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanMaintenanceRepository extends JpaRepository<PlanMaintenance, Integer> {

    List<PlanMaintenance> findByEquipementId(Integer equipementId);

    /**
     * Plans actifs — filtrés ensuite en Java via
     * {@link PlanMaintenance#eligibleGenerationAutomatique} pour le job de
     * génération automatique. Le volume de plans actifs reste raisonnable
     * pour une GMAO (quelques centaines/milliers max), donc un filtrage en
     * mémoire est préférable à une requête JPQL fragile basée sur des
     * fonctions de date natives.
     */
    List<PlanMaintenance> findByActifTrue();
}
