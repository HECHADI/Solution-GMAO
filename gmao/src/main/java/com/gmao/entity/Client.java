package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un client de l'entreprise.
 */
@Entity
@Table(
    name = "Client",
    uniqueConstraints = @UniqueConstraint(name = "uq_client_email", columnNames = "email")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "sites")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "email", length = 150)
    private String email;

    // ── Horodatage automatique ────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Relations ─────────────────────────────────────────────────────────────

    /**
     * Liste des sites appartenant à ce client.
     * mappedBy → champ "client" dans l'entité Site.
     */
    @OneToMany(
        mappedBy = "client",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Site> sites = new ArrayList<>();

    // ── Méthodes utilitaires ──────────────────────────────────────────────────

    /** Ajoute un site et maintient la cohérence bidirectionnelle. */
    public void addSite(Site site) {
        sites.add(site);
        site.setClient(this);
    }

    /** Retire un site et maintient la cohérence bidirectionnelle. */
    public void removeSite(Site site) {
        sites.remove(site);
        site.setClient(null);
    }
}
