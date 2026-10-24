package com.gmao.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Réglage applicatif générique (clé-valeur). Évite une migration de schéma
 * à chaque nouveau paramètre configurable.
 */
@Entity
@Table(
    name = "parametreapplication",
    uniqueConstraints = @UniqueConstraint(name = "uq_parametre_cle", columnNames = "cle")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametreApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cle", nullable = false, length = 100)
    private String cle;

    @Column(name = "valeur", nullable = false, length = 255)
    private String valeur;

    @Column(name = "description", length = 255)
    private String description;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
