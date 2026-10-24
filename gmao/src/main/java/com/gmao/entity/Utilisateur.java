package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entité représentant un compte utilisateur de l'application GMAO.
 *
 * <p>Le champ {@code motDePasse} doit toujours contenir un hash
 * (bcrypt, argon2…) — jamais le mot de passe en clair.</p>
 */
@Entity
@Table(
    name = "utilisateur",
    uniqueConstraints = @UniqueConstraint(name = "uq_utilisateur_email", columnNames = "email")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "motDePasse")       // ne jamais loguer le mot de passe
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    /** Hash du mot de passe (bcrypt recommandé). */
    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private RoleUtilisateur role = RoleUtilisateur.TECHNICIEN;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private boolean actif = true;

    // ── Horodatage automatique ────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
