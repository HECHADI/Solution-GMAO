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
 * Entité représentant un site géographique appartenant à un client.
 */
@Entity
@Table(name = "Site")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"client", "equipements"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;

    /**
     * Coordonnées GPS stockées sous forme "latitude,longitude".
     * Exemple : "48.8566,2.3522"
     */
    @Column(name = "coordonnees_gps", length = 50)
    private String coordonneesGps;

    // ── Horodatage automatique ────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Relations ─────────────────────────────────────────────────────────────

    /** Client propriétaire du site (Many-to-One obligatoire). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "client_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_site_client")
    )
    private Client client;

    /** Équipements installés sur ce site. */
    @OneToMany(
        mappedBy = "site",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Equipement> equipements = new ArrayList<>();

    // ── Méthodes utilitaires ──────────────────────────────────────────────────

    /** Ajoute un équipement et maintient la cohérence bidirectionnelle. */
    public void addEquipement(Equipement equipement) {
        equipements.add(equipement);
        equipement.setSite(this);
    }

    /** Retire un équipement et maintient la cohérence bidirectionnelle. */
    public void removeEquipement(Equipement equipement) {
        equipements.remove(equipement);
        equipement.setSite(null);
    }
}
