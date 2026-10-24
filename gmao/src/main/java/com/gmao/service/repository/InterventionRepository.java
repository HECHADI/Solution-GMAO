package com.gmao.service.repository;

import com.gmao.entity.Intervention;
import com.gmao.entity.StatutIntervention;
import com.gmao.entity.TypeIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité {@link Intervention}.
 */
@Repository
public interface InterventionRepository extends JpaRepository<Intervention, Integer> {

    // ── Recherches par équipement ─────────────────────────────────────────────
    List<Intervention> findByEquipementId(Integer equipementId);
    List<Intervention> findByEquipementIdAndStatut(Integer equipementId, StatutIntervention statut);
    long countByEquipementId(Integer equipementId);

    // ── Recherches par technicien ─────────────────────────────────────────────
    List<Intervention> findByTechnicienId(Integer technicienId);
    List<Intervention> findByTechnicienIdAndStatut(Integer technicienId, StatutIntervention statut);
    
    /** Interventions non encore assignées à un technicien. */
    List<Intervention> findByTechnicienIsNull();

    // ── Recherches par statut / type / facturation ───────────────────────────
    /**
     * Retrouve toutes les interventions ayant un statut spécifique (ex: REALISE)
     * et qui ne sont rattachées à aucune facture (facture_id IS NULL).
     */
    List<Intervention> findByStatutAndFactureIsNull(StatutIntervention statut);

    // ── Recherche détaillée d'une seule intervention ─────────────────────────
    /**
     * Récupère une intervention par son ID en chargeant immédiatement 
     * l'équipement, le site, le client et le technicien associés.
     */
    @Query("""
        SELECT i FROM Intervention i
        LEFT JOIN FETCH i.equipement e
        LEFT JOIN FETCH e.site s
        LEFT JOIN FETCH s.client c
        LEFT JOIN FETCH i.technicien t
        WHERE i.id = :id
        """)
    Optional<Intervention> findByIdWithDetails(@Param("id") Integer id);

    // ── 🟢 AJOUT : Recherche détaillée des interventions d'un Technicien ──────
    /**
     * Récupère toutes les interventions d'un technicien spécifique avec toutes
     * les données d'équipement, de site et de client associées.
     */
    @Query("""
        SELECT i FROM Intervention i
        LEFT JOIN FETCH i.equipement e
        LEFT JOIN FETCH e.site s
        LEFT JOIN FETCH s.client c
        LEFT JOIN FETCH i.technicien t
        WHERE t.id = :technicienId
        ORDER BY i.dateDebut DESC
        """)
    List<Intervention> findByTechnicienIdWithDetails(@Param("technicienId") Integer technicienId);

    // ── Recherches de trajets ─────────────────────────────────────────────────
    /**
     * Recherche la précédente intervention d'un technicien (ayant une date_fin).
     * Utilisé pour calculer le temps de trajet depuis le site précédent.
     */
    @Query("""
        SELECT i FROM Intervention i
        JOIN FETCH i.equipement e
        JOIN FETCH e.site s
        WHERE i.technicien.id = :technicienId
          AND i.dateFin <= :dateDebut
          AND (:ignoreId IS NULL OR i.id != :ignoreId)
        ORDER BY i.dateFin DESC
        """)
    List<Intervention> findPreviousInterventions(
        @Param("technicienId") Integer technicienId,
        @Param("dateDebut") LocalDateTime dateDebut,
        @Param("ignoreId") Integer ignoreId
    );

    // ── Mises à jour directes (bulk) ──────────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Intervention i SET i.statut = :statut WHERE i.id = :id")
    int updateStatut(@Param("id") Integer id, @Param("statut") StatutIntervention statut);

    @Modifying
    @Transactional
    @Query("UPDATE Intervention i SET i.technicien.id = :technicienId WHERE i.id = :id")
    int assignerTechnicien(@Param("id") Integer id, @Param("technicienId") Integer technicienId);
    
    /** Vérifie s'il existe une tâche préventive en cours pour éviter les doublons automatiques. */
    boolean existsByPlanMaintenanceIdAndStatutNot(Integer planMaintenanceId, StatutIntervention statut);
}