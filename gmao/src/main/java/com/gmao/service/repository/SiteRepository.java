package com.gmao.service.repository;

import com.gmao.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité {@link Site}.
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    // ── Recherches par client ─────────────────────────────────────────────────

    /** Retourne tous les sites d'un client donné. */
    List<Site> findByClientId(Integer clientId);

    /** Compte le nombre de sites d'un client. */
    long countByClientId(Integer clientId);

    // ── Recherches textuelles ─────────────────────────────────────────────────

    /** Recherche des sites dont le nom contient le terme donné (insensible à la casse). */
    List<Site> findByNomContainingIgnoreCase(String nom);

    // ── Requêtes JPQL ─────────────────────────────────────────────────────────

    /**
     * Récupère un site avec son client et ses équipements (évite le N+1).
     */
    @Query("""
        SELECT s FROM Site s
        LEFT JOIN FETCH s.client
        LEFT JOIN FETCH s.equipements
        WHERE s.id = :id
        """)
    Optional<Site> findByIdWithClientAndEquipements(@Param("id") Integer id);

    /**
     * Récupère tous les sites d'un client avec leurs équipements.
     */
    @Query("""
        SELECT DISTINCT s FROM Site s
        LEFT JOIN FETCH s.equipements
        WHERE s.client.id = :clientId
        """)
    List<Site> findByClientIdWithEquipements(@Param("clientId") Integer clientId);
}
