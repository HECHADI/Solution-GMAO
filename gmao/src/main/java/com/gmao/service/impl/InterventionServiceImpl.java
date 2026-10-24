package com.gmao.service.impl;
import com.gmao.dto.AssignationRequest;
import com.gmao.dto.ClotureRequest;
import com.gmao.dto.DatesMajRequest;
import com.gmao.dto.InterventionRequest;
import com.gmao.dto.InterventionResponse;
import com.gmao.dto.TrajetInfo;
import com.gmao.entity.*;
import com.gmao.event.InterventionRealiseeEvent;
import com.gmao.exception.BusinessException;
import com.gmao.exception.ResourceNotFoundException;
import com.gmao.service.repository.EquipementRepository;
import com.gmao.service.repository.InterventionRepository;
import com.gmao.service.repository.UtilisateurRepository;
import com.gmao.service.InterventionService;
import com.gmao.service.PlanMaintenanceService;
import com.gmao.service.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation du service de gestion des interventions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)   // Toutes les méthodes sont read-only par défaut
public class InterventionServiceImpl implements InterventionService {

    private final InterventionRepository  interventionRepository;
    private final EquipementRepository    equipementRepository;
    private final UtilisateurRepository   utilisateurRepository;
    private final RoutingService          routingService;
    private final ApplicationEventPublisher eventPublisher;
    private final PlanMaintenanceService planMaintenanceService;

    // ── Création ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public InterventionResponse creer(InterventionRequest request) {
        log.info("Création d'une intervention '{}' pour l'équipement id={}", request.getTitre(), request.getEquipementId());

        Equipement equipement = equipementRepository.findById(request.getEquipementId())
            .orElseThrow(() -> new ResourceNotFoundException("Équipement", request.getEquipementId()));

        Intervention intervention = Intervention.builder()
            .titre(request.getTitre())
            .description(request.getDescription())
            .type(request.getType())
            .dateDebut(request.getDateDebut())
            .dateFin(request.getDateFin())
            .dureeEstimee(request.getDureeEstimee())
            .equipement(equipement)
            .build();

        // Technicien optionnel à la création
        if (request.getTechnicienId() != null) {
            intervention.setTechnicien(validerEtChargerTechnicien(request.getTechnicienId()));
        }

        // Déduction du statut initial selon les dates fournies
        intervention.setStatut(deduireStatutInitial(request));

        // Calcul du temps de trajet
        mettreAJourTrajet(intervention);

        Intervention saved = interventionRepository.save(intervention);
        log.info("Intervention créée avec l'id={}", saved.getId());
        return InterventionResponse.fromEntity(saved);
    }

    // ── Lectures ──────────────────────────────────────────────────────────────

    @Override
    public List<InterventionResponse> listerTout() {
        return interventionRepository.findAll()
            .stream()
            .map(InterventionResponse::fromEntity)
            .toList();
    }

    @Override
    public InterventionResponse trouverParId(Integer id) {
        Intervention intervention = interventionRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Intervention", id));
        return InterventionResponse.fromEntity(intervention);
    }

    @Override
    public List<InterventionResponse> listerParEquipement(Integer equipementId) {
        if (!equipementRepository.existsById(equipementId)) {
            throw new ResourceNotFoundException("Équipement", equipementId);
        }
        return interventionRepository.findByEquipementId(equipementId)
            .stream()
            .map(InterventionResponse::fromEntity)
            .toList();
    }

    @Override
    public List<InterventionResponse> listerParTechnicien(Integer technicienId) {
        if (!utilisateurRepository.existsById(technicienId)) {
            throw new ResourceNotFoundException("Technicien", technicienId);
        }
        return interventionRepository.findByTechnicienIdWithDetails(technicienId)
            .stream()
            .map(InterventionResponse::fromEntity)
            .toList();
    }

    @Override
    public List<InterventionResponse> listerNonAssignees() {
        return interventionRepository.findByTechnicienIsNull()
            .stream()
            .map(InterventionResponse::fromEntity)
            .toList();
    }

    // ── Assignation d'un technicien ───────────────────────────────────────────

    @Override
    @Transactional
    public InterventionResponse assignerTechnicien(Integer interventionId, AssignationRequest request) {
        log.info("Assignation du technicien id={} à l'intervention id={}", request.getTechnicienId(), interventionId);

        Intervention intervention = interventionRepository.findByIdWithDetails(interventionId)
            .orElseThrow(() -> new ResourceNotFoundException("Intervention", interventionId));

        // Règle métier : une intervention REALISE ne peut plus être réassignée
        if (StatutIntervention.REALISE.equals(intervention.getStatut())) {
            throw new BusinessException("Impossible d'assigner un technicien à une intervention déjà réalisée");
        }

        Utilisateur technicien = validerEtChargerTechnicien(request.getTechnicienId());

        intervention.setTechnicien(technicien);

        // Passage automatique en EN_COURS si l'intervention était A_FAIRE
        if (StatutIntervention.A_FAIRE.equals(intervention.getStatut()) && intervention.getDateDebut() != null) {
            intervention.setStatut(StatutIntervention.EN_COURS);
            log.info("Intervention id={} passée en EN_COURS suite à l'assignation", interventionId);
        }

        // Recalcul du trajet avec le nouveau technicien
        mettreAJourTrajet(intervention);

        Intervention saved = interventionRepository.save(intervention);
        log.info("Technicien '{}' assigné à l'intervention '{}'", technicien.getNom(), intervention.getTitre());
        return InterventionResponse.fromEntity(saved);
    }

    // ── Mise à jour des dates ─────────────────────────────────────────────────

    @Override
    @Transactional
    public InterventionResponse mettreAJourDates(Integer interventionId, DatesMajRequest request) {
        log.info("Mise à jour des dates de l'intervention id={}", interventionId);

        Intervention intervention = interventionRepository.findByIdWithDetails(interventionId)
            .orElseThrow(() -> new ResourceNotFoundException("Intervention", interventionId));

        // Règle métier : une intervention réalisée ne peut plus être modifiée
        if (StatutIntervention.REALISE.equals(intervention.getStatut())) {
            throw new BusinessException("Impossible de modifier les dates d'une intervention déjà réalisée");
        }

        // Application des dates fournies (null = pas de changement)
        if (request.getDateDebut() != null) {
            intervention.setDateDebut(request.getDateDebut());
        }
        if (request.getDateFin() != null) {
            intervention.setDateFin(request.getDateFin());
        }
        if (request.getDureeEstimee() != null) {
            intervention.setDureeEstimee(request.getDureeEstimee());
        }

        // Validation de la cohérence après mise à jour
        if (intervention.getDateDebut() != null && intervention.getDateFin() != null) {
            if (!intervention.getDateDebut().isBefore(intervention.getDateFin())) {
                throw new BusinessException("La date de début doit être antérieure à la date de fin");
            }
        }

        // ── Mise à jour automatique du statut ──────────────────────────────
        // dateFin renseignée → REALISE (prioritaire)
        if (intervention.getDateFin() != null) {
            intervention.setStatut(StatutIntervention.REALISE);
            log.info("Intervention id={} marquée REALISE (dateFin renseignée)", interventionId);
        }
        // dateDebut renseignée sans dateFin → EN_COURS (si A_FAIRE)
        else if (intervention.getDateDebut() != null
                 && StatutIntervention.A_FAIRE.equals(intervention.getStatut())) {
            intervention.setStatut(StatutIntervention.EN_COURS);
            log.info("Intervention id={} passée EN_COURS (dateDebut renseignée)", interventionId);
        }

        // Recalcul du trajet car les dates ont changé
        mettreAJourTrajet(intervention);

        Intervention saved = interventionRepository.save(intervention);
        return InterventionResponse.fromEntity(saved);
    }

    // ── Mise à jour manuelle du statut ────────────────────────────────────────

    @Override
    @Transactional
    public InterventionResponse mettreAJourStatut(Integer interventionId, StatutIntervention statut) {
        log.info("Changement de statut de l'intervention id={} → {}", interventionId, statut);

        Intervention intervention = interventionRepository.findByIdWithDetails(interventionId)
            .orElseThrow(() -> new ResourceNotFoundException("Intervention", interventionId));

        intervention.setStatut(statut);
         if (intervention.getStatut() == StatutIntervention.REALISE) {
                
             planMaintenanceService.recalculerApresRealisation(intervention);
         }
        Intervention saved = interventionRepository.save(intervention);
        return InterventionResponse.fromEntity(saved);
    }

    // ── Clôture de l'intervention (depuis Mobile) ─────────────────────────────

    @Override
    @Transactional
    public InterventionResponse cloturer(Integer interventionId, ClotureRequest request) {
        log.info("Clôture de l'intervention id={}", interventionId);

        Intervention intervention = interventionRepository.findByIdWithDetails(interventionId)
            .orElseThrow(() -> new ResourceNotFoundException("Intervention", interventionId));

        if (StatutIntervention.REALISE.equals(intervention.getStatut())) {
            throw new BusinessException("Cette intervention est déjà clôturée.");
        }

        // Enregistrement des données de clôture
        intervention.setCompteRendu(request.getCompteRendu());
        intervention.setSignatureBase64(request.getSignatureBase64());
        
        // Clôture logicielle
        intervention.setStatut(StatutIntervention.REALISE);
        if (intervention.getDateFin() == null) {
            intervention.setDateFin(java.time.LocalDateTime.now());
        }

        Intervention saved = interventionRepository.save(intervention);

        // Déclenche l'événement pour la génération du PDF et l'envoi d'email
        eventPublisher.publishEvent(new InterventionRealiseeEvent(this, saved));

        return InterventionResponse.fromEntity(saved);
    }

    // ── Suppression ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void supprimer(Integer id) {
        if (!interventionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Intervention", id);
        }
        interventionRepository.deleteById(id);
        log.info("Intervention id={} supprimée", id);

    }

    // ── Méthodes privées ──────────────────────────────────────────────────────

    /**
     * Charge un utilisateur et vérifie qu'il possède le rôle TECHNICIEN.
     */
    private Utilisateur validerEtChargerTechnicien(Integer technicienId) {
        Utilisateur technicien = utilisateurRepository.findById(technicienId)
            .orElseThrow(() -> new ResourceNotFoundException("Technicien", technicienId));

        if (!RoleUtilisateur.TECHNICIEN.equals(technicien.getRole())) {
            throw new BusinessException(
                "L'utilisateur '" + technicien.getNom() + "' n'a pas le rôle TECHNICIEN"
            );
        }
        if (!technicien.isActif()) {
            throw new BusinessException(
                "Le technicien '" + technicien.getNom() + "' est désactivé"
            );
        }
        return technicien;
    }

    /**
     * Déduit le statut initial d'une intervention à partir des dates fournies.
     */
    private StatutIntervention deduireStatutInitial(InterventionRequest request) {
        if (request.getDateFin() != null) {
            return StatutIntervention.REALISE;
        }
        if (request.getDateDebut() != null) {
            return StatutIntervention.EN_COURS;
        }
        return StatutIntervention.A_FAIRE;
    }

    /**
     * Calcule et met à jour le temps de trajet depuis la dernière intervention du technicien.
     */
    private void mettreAJourTrajet(Intervention intervention) {
        // Pas de technicien ou pas de date de début prévue -> pas de calcul possible
        if (intervention.getTechnicien() == null || intervention.getDateDebut() == null) {
            intervention.setDistanceKm(null);
            intervention.setTempsTrajetMinutes(null);
            return;
        }

        List<Intervention> precedentes = interventionRepository.findPreviousInterventions(
            intervention.getTechnicien().getId(),
            intervention.getDateDebut(),
            intervention.getId()
        );

        if (!precedentes.isEmpty()) {
            Intervention precedente = precedentes.get(0);
            
            String gpsDepart = precedente.getEquipement().getSite().getCoordonneesGps();
            String gpsArrivee = intervention.getEquipement().getSite().getCoordonneesGps();

            TrajetInfo trajet = routingService.calculerTrajet(gpsDepart, gpsArrivee);
            intervention.setDistanceKm(trajet.getDistanceKm());
            intervention.setTempsTrajetMinutes(trajet.getDureeMinutes());
            log.info("Trajet calculé pour intervention id={} : {} km, {} min", intervention.getId(), trajet.getDistanceKm(), trajet.getDureeMinutes());
        } else {
            // Pas d'intervention précédente pour ce technicien avant cette date
            intervention.setDistanceKm(0);
            intervention.setTempsTrajetMinutes(0);
        }
    }
    
   

}
