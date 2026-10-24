package com.gmao.service.repository;

import com.gmao.entity.RoleUtilisateur;
import com.gmao.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité {@link Utilisateur}.
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    // ── Recherches par e-mail ─────────────────────────────────────────────────

    /** Recherche un utilisateur par son e-mail (insensible à la casse). */
    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    /** Vérifie qu'un e-mail est déjà utilisé. */
    boolean existsByEmailIgnoreCase(String email);

    // ── Recherches par rôle ───────────────────────────────────────────────────

    /** Retourne tous les utilisateurs ayant le rôle donné. */
    List<Utilisateur> findByRole(RoleUtilisateur role);

    /** Retourne tous les utilisateurs actifs ayant le rôle donné. */
    List<Utilisateur> findByRoleAndActifTrue(RoleUtilisateur role);

    // ── Recherches textuelles ─────────────────────────────────────────────────

    /** Recherche des utilisateurs dont le nom contient le terme donné (insensible à la casse). */
    List<Utilisateur> findByNomContainingIgnoreCase(String nom);

    // ── Statut actif ──────────────────────────────────────────────────────────

    /** Retourne tous les utilisateurs actifs. */
    List<Utilisateur> findByActifTrue();

    /** Retourne tous les utilisateurs désactivés. */
    List<Utilisateur> findByActifFalse();

    // ── Mises à jour directes ─────────────────────────────────────────────────

    /**
     * Active ou désactive un compte utilisateur sans charger l'entité complète.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.actif = :actif WHERE u.id = :id")
    int updateActif(@Param("id") Integer id, @Param("actif") boolean actif);

    /**
     * Met à jour le mot de passe haché d'un utilisateur.
     * Le hash doit être calculé par le service appelant (bcrypt, argon2…).
     */
    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.motDePasse = :hash WHERE u.id = :id")
    int updateMotDePasse(@Param("id") Integer id, @Param("hash") String hash);
}
