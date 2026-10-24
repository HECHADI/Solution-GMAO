-- ============================================================
--  GMAO - Patch : Ajout de la table Intervention
--  À exécuter après le script gmao_structure.sql initial
-- ============================================================

USE gmao;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- Table : Intervention
-- ============================================================
CREATE TABLE IF NOT EXISTS Intervention (
    id              INT             NOT NULL AUTO_INCREMENT,
    titre           VARCHAR(200)    NOT NULL,
    description     TEXT,
    type            ENUM('PREVENTIF','CURATIF')              NOT NULL,
    statut          ENUM('A_FAIRE','EN_COURS','REALISE')     NOT NULL DEFAULT 'A_FAIRE',
    date_debut      DATETIME,
    date_fin        DATETIME,
    duree_estimee   INT,                                   -- en minutes
    equipement_id   INT             NOT NULL,
    technicien_id   INT,                                   -- NULL = non assignée
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_intervention                  PRIMARY KEY (id),
    CONSTRAINT fk_intervention_equipement       FOREIGN KEY (equipement_id)
                                                REFERENCES Equipement(id)
                                                ON UPDATE CASCADE
                                                ON DELETE RESTRICT,
    CONSTRAINT fk_intervention_technicien       FOREIGN KEY (technicien_id)
                                                REFERENCES Utilisateur(id)
                                                ON UPDATE CASCADE
                                                ON DELETE SET NULL,
    CONSTRAINT chk_intervention_dates           CHECK (date_fin IS NULL OR date_debut IS NULL OR date_debut < date_fin),
    CONSTRAINT chk_intervention_duree           CHECK (duree_estimee IS NULL OR duree_estimee > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Interventions de maintenance sur les équipements';

-- Index de performance
CREATE INDEX idx_intervention_equipement_id ON Intervention (equipement_id);
CREATE INDEX idx_intervention_technicien_id ON Intervention (technicien_id);
CREATE INDEX idx_intervention_statut        ON Intervention (statut);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Données de test
-- ============================================================
INSERT INTO Intervention (titre, description, type, statut, date_debut, duree_estimee, equipement_id, technicien_id)
VALUES
    ('Vérification filtre compresseur',
     'Contrôle et remplacement du filtre à air du compresseur A1',
     'PREVENTIF', 'EN_COURS',
     '2026-07-08 09:00:00', 60, 1, 2),

    ('Réparation pompe hydraulique',
     'Diagnostic et réparation de la panne de la pompe hydraulique',
     'CURATIF', 'A_FAIRE',
     NULL, 120, 2, NULL),

    ('Maintenance CVC annuelle',
     'Nettoyage et contrôle complet du système CVC',
     'PREVENTIF', 'A_FAIRE',
     '2026-07-15 08:00:00', 180, 3, 3);
