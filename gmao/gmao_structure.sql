-- ============================================================
--  GMAO - Script de création de la base de données
--  Compatible : MySQL 5.7+ / MariaDB (XAMPP v3.3.0)
--  Encodage   : UTF-8
-- ============================================================

-- Création et sélection de la base de données
CREATE DATABASE IF NOT EXISTS gmao
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gmao;

-- ============================================================
-- Désactivation temporaire des contraintes FK pendant la création
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- Table : Utilisateur
-- ============================================================
CREATE TABLE IF NOT EXISTS Utilisateur (
    id            INT             NOT NULL AUTO_INCREMENT,
    nom           VARCHAR(100)    NOT NULL,
    email         VARCHAR(150)    NOT NULL,
    mot_de_passe  VARCHAR(255)    NOT NULL,   -- Stocker un hash (bcrypt, argon2…)
    role          ENUM('MANAGER','TECHNICIEN') NOT NULL DEFAULT 'TECHNICIEN',
    actif         TINYINT(1)      NOT NULL DEFAULT 1,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_utilisateur   PRIMARY KEY (id),
    CONSTRAINT uq_utilisateur_email UNIQUE (email)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Comptes utilisateurs de l''application GMAO';

-- ============================================================
-- Table : Client
-- ============================================================
CREATE TABLE IF NOT EXISTS Client (
    id            INT             NOT NULL AUTO_INCREMENT,
    nom           VARCHAR(150)    NOT NULL,
    adresse       TEXT,
    telephone     VARCHAR(20),
    email         VARCHAR(150),
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_client        PRIMARY KEY (id),
    CONSTRAINT uq_client_email  UNIQUE (email)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Clients de l''entreprise';

-- ============================================================
-- Table : Site
-- ============================================================
CREATE TABLE IF NOT EXISTS Site (
    id                  INT             NOT NULL AUTO_INCREMENT,
    nom                 VARCHAR(150)    NOT NULL,
    adresse             TEXT,
    coordonnees_gps     VARCHAR(50),    -- Format : "latitude,longitude" ex: "48.8566,2.3522"
    client_id           INT             NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_site          PRIMARY KEY (id),
    CONSTRAINT fk_site_client   FOREIGN KEY (client_id)
                                REFERENCES Client(id)
                                ON UPDATE CASCADE
                                ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Sites géographiques appartenant aux clients';

-- Index sur la clé étrangère
CREATE INDEX idx_site_client_id ON Site (client_id);

-- ============================================================
-- Table : Equipement
-- ============================================================
CREATE TABLE IF NOT EXISTS Equipement (
    id                  INT             NOT NULL AUTO_INCREMENT,
    nom                 VARCHAR(150)    NOT NULL,
    categorie           VARCHAR(100),
    marque              VARCHAR(100),
    modele              VARCHAR(100),
    numero_serie        VARCHAR(100),
    date_installation   DATE,
    statut              ENUM('EN_SERVICE','EN_PANNE','EN_MAINTENANCE','HORS_SERVICE','RETIRE')
                                        NOT NULL DEFAULT 'EN_SERVICE',
    site_id             INT             NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_equipement            PRIMARY KEY (id),
    CONSTRAINT uq_equipement_serie      UNIQUE (numero_serie),
    CONSTRAINT fk_equipement_site       FOREIGN KEY (site_id)
                                        REFERENCES Site(id)
                                        ON UPDATE CASCADE
                                        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Équipements installés sur les sites clients';

-- Index sur la clé étrangère et le statut (requêtes fréquentes)
CREATE INDEX idx_equipement_site_id ON Equipement (site_id);
CREATE INDEX idx_equipement_statut  ON Equipement (statut);

-- ============================================================
-- Réactivation des contraintes FK
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Données de test (optionnel — commenter si non nécessaire)
-- ============================================================

-- Utilisateurs
INSERT INTO Utilisateur (nom, email, mot_de_passe, role) VALUES
    ('Alice Martin',  'alice@gmao.fr',  '$2y$10$EXEMPLE_HASH_BCRYPT_ICI_1', 'MANAGER'),
    ('Bob Dupont',    'bob@gmao.fr',    '$2y$10$EXEMPLE_HASH_BCRYPT_ICI_2', 'TECHNICIEN'),
    ('Clara Morin',   'clara@gmao.fr',  '$2y$10$EXEMPLE_HASH_BCRYPT_ICI_3', 'TECHNICIEN');

-- Clients
INSERT INTO Client (nom, adresse, telephone, email) VALUES
    ('Industrie Alpha', '12 rue de la Forge, 75001 Paris', '0123456789', 'contact@alpha.fr'),
    ('Bêta Services',   '45 avenue du Parc, 69002 Lyon',   '0456789012', 'info@beta.fr');

-- Sites
INSERT INTO Site (nom, adresse, coordonnees_gps, client_id) VALUES
    ('Usine Paris Nord',    '12 rue de la Forge, 75001 Paris', '48.8566,2.3522', 1),
    ('Entrepôt Lyon Sud',   '45 avenue du Parc, 69002 Lyon',   '45.7640,4.8357', 2),
    ('Atelier Lyon Est',    '8 rue des Acacias, 69003 Lyon',    '45.7590,4.8520', 2);

-- Équipements
INSERT INTO Equipement (nom, categorie, marque, modele, numero_serie, date_installation, statut, site_id) VALUES
    ('Compresseur A1',   'Pneumatique', 'Atlas Copco', 'GA15',   'SN-2024-001', '2024-01-15', 'EN_SERVICE',     1),
    ('Pompe Hydraulique','Hydraulique',  'Grundfos',   'CM3-5',  'SN-2023-042', '2023-06-10', 'EN_MAINTENANCE', 1),
    ('CVC Bâtiment B',   'Climatisation','Daikin',     'VRV-IV', 'SN-2022-118', '2022-11-20', 'EN_SERVICE',     2),
    ('Groupe Électrogène','Électrique',  'Kohler',     'KD20',   'SN-2021-007', '2021-03-05', 'EN_PANNE',       3);

-- ============================================================
-- Fin du script
-- ============================================================
