package com.gmao.service.repository;

import com.gmao.entity.Equipement;
import com.gmao.entity.StatutEquipement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité {@link Equipement}.
 */
@Repository
public interface EquipementRepository extends JpaRepository<Equipement, Integer> {

    // ── Recherches par site ───────────────────────────────────────────────────

    /** Retourne tous les équipements d'un site. */
    List<Equipement> findBySiteId(Integer siteId);

    /** Retourne les équipements d'un site filtrés par statut. */
    List<Equipement> findBySiteIdAndStatut(Integer siteId, StatutEquipement statut);

    /** Compte les équipements d'un site. */
    long countBySiteId(Integer siteId);

    // ── Recherches par statut ─────────────────────────────────────────────────

    /** Retourne tous les équipements ayant le statut donné. */
    List<Equipement> findByStatut(StatutEquipement statut);

    /** Compte les équipements par statut. */
    long countByStatut(StatutEquipement statut);

    // ── Recherche par numéro de série ─────────────────────────────────────────

    /** Recherche un équipement par son numéro de série (unique). */
    Optional<Equipement> findByNumeroSerie(String numeroSerie);

    /** Vérifie l'existence d'un numéro de série. */
    boolean existsByNumeroSerie(String numeroSerie);

    // ── Recherches textuelles ─────────────────────────────────────────────────

    /** Recherche des équipements dont le nom, la marque ou le modèle correspond. */
    @Query("""
        SELECT e FROM Equipement e
        WHERE LOWER(e.nom)     LIKE LOWER(CONCAT('%', :terme, '%'))
           OR LOWER(e.marque)  LIKE LOWER(CONCAT('%', :terme, '%'))
           OR LOWER(e.modele)  LIKE LOWER(CONCAT('%', :terme, '%'))
        """)
    List<Equipement> searchByTerme(@Param("terme") String terme);

    // ── Requêtes JPQL avec JOIN FETCH ─────────────────────────────────────────

    /**
     * Récupère un équipement avec son site et le client associé (évite le N+1).
     */
    @Query("""
        SELECT e FROM Equipement e
        JOIN FETCH e.site s
        JOIN FETCH s.client
        WHERE e.id = :id
        """)
    Optional<Equipement> findByIdWithSiteAndClient(@Param("id") Integer id);

    /**
     * Retourne tous les équipements en panne ou en maintenance.
     */
    @Query("""
        SELECT e FROM Equipement e
        WHERE e.statut IN (
            com.gmao.entity.StatutEquipement.EN_PANNE,
            com.gmao.entity.StatutEquipement.EN_MAINTENANCE
        )
        ORDER BY e.updatedAt DESC
        """)
    List<Equipement> findEquipementsNecessitantIntervention();

    // ── Mise à jour directe du statut ─────────────────────────────────────────

    /**
     * Met à jour directement le statut d'un équipement sans charger l'entité.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Equipement e SET e.statut = :statut WHERE e.id = :id")
    int updateStatut(@Param("id") Integer id, @Param("statut") StatutEquipement statut);
}
