-- ============================================================
--  GMAO - Patch : Ajout de la gestion des stocks
-- ============================================================

USE gmao;

-- 1. Catalogue des pièces détachées
CREATE TABLE PieceDetachee (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(100) NOT NULL UNIQUE,
    nom VARCHAR(255) NOT NULL,
    prix_unitaire DECIMAL(10,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Stock embarqué par technicien (Véhicule)
CREATE TABLE StockVehicule (
    id INT AUTO_INCREMENT PRIMARY KEY,
    technicien_id INT NOT NULL,
    piece_id INT NOT NULL,
    quantite INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_stock_tech FOREIGN KEY (technicien_id) REFERENCES Utilisateur(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_piece FOREIGN KEY (piece_id) REFERENCES PieceDetachee(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tech_piece (technicien_id, piece_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Pièces utilisées lors d'une intervention
CREATE TABLE PieceUtilisee (
    id INT AUTO_INCREMENT PRIMARY KEY,
    intervention_id INT NOT NULL,
    piece_id INT NOT NULL,
    quantite INT NOT NULL,
    CONSTRAINT fk_utilisee_interv FOREIGN KEY (intervention_id) REFERENCES Intervention(id) ON DELETE CASCADE,
    CONSTRAINT fk_utilisee_piece FOREIGN KEY (piece_id) REFERENCES PieceDetachee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Insertion de données de test
-- ============================================================
INSERT INTO PieceDetachee (reference, nom, prix_unitaire) VALUES
('FIL-001', 'Filtre à air industriel', 45.50),
('POM-120', 'Pompe de relevage 120W', 250.00),
('JNT-O-50', 'Joint torique 50mm', 2.00);

-- On suppose que le technicien avec ID 2 existe (selon le script initial)
INSERT INTO StockVehicule (technicien_id, piece_id, quantite) VALUES
(2, 1, 5),  -- 5 Filtres
(2, 2, 1),  -- 1 Pompe
(2, 3, 20); -- 20 Joints
