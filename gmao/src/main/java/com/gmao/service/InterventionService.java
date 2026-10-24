package com.gmao.service;

import com.gmao.dto.AssignationRequest;
import com.gmao.dto.DatesMajRequest;
import com.gmao.dto.InterventionRequest;
import com.gmao.dto.InterventionResponse;
import com.gmao.entity.StatutIntervention;

import java.util.List;

/**
 * Contrat du service métier pour la gestion des interventions.
 */
public interface InterventionService {

    /** Crée une nouvelle intervention. */
    InterventionResponse creer(InterventionRequest request);

    /** Retourne toutes les interventions. */
    List<InterventionResponse> listerTout();

    /** Retourne une intervention par son identifiant. */
    InterventionResponse trouverParId(Integer id);

    /** Retourne les interventions d'un équipement donné. */
    List<InterventionResponse> listerParEquipement(Integer equipementId);

    /** Retourne les interventions assignées à un technicien. */
    List<InterventionResponse> listerParTechnicien(Integer technicienId);

    /** Retourne les interventions non encore assignées. */
    List<InterventionResponse> listerNonAssignees();

    /**
     * Assigne un technicien à une intervention existante.
     * Vérifie que l'utilisateur a bien le rôle TECHNICIEN.
     */
    InterventionResponse assignerTechnicien(Integer interventionId, AssignationRequest request);

    /**
     * Met à jour les dates (début, fin) et/ou la durée estimée d'une intervention.
     * Met automatiquement à jour le statut selon la logique métier :
     * <ul>
     *   <li>dateDebut renseignée → passe en EN_COURS si A_FAIRE</li>
     *   <li>dateFin renseignée   → passe en REALISE</li>
     * </ul>
     */
    InterventionResponse mettreAJourDates(Integer interventionId, DatesMajRequest request);

    /** Met à jour manuellement le statut d'une intervention. */
    InterventionResponse mettreAJourStatut(Integer interventionId, StatutIntervention statut);

    /**
     * Clôture une intervention avec le compte-rendu, la signature et passe son statut à REALISE.
     */
    InterventionResponse cloturer(Integer interventionId, com.gmao.dto.ClotureRequest request);

    /**
     * Supprime une intervention.
     */
    void supprimer(Integer id);
}
