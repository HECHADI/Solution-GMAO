-- ============================================================
--  GMAO - Patch : Ajout des champs de routage à Intervention
--  À exécuter après gmao_patch_intervention.sql
-- ============================================================

USE gmao;

ALTER TABLE Intervention
    ADD COLUMN distance_km INT DEFAULT NULL COMMENT 'Distance estimée depuis l''intervention précédente (km)',
    ADD COLUMN temps_trajet_minutes INT DEFAULT NULL COMMENT 'Temps de trajet estimé (minutes)';

-- ============================================================
-- Fin du script
-- ============================================================
