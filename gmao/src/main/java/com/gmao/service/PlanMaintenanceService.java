package com.gmao.service;

import com.gmao.dto.PlanMaintenanceRequest;
import com.gmao.dto.PlanMaintenanceResponse;
import com.gmao.entity.Equipement;
import com.gmao.entity.Intervention;
import com.gmao.entity.PlanMaintenance;
import com.gmao.entity.StatutIntervention;
import com.gmao.entity.TypeIntervention;
import com.gmao.service.repository.EquipementRepository;
import com.gmao.service.repository.InterventionRepository;
import com.gmao.service.repository.PlanMaintenanceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des plans de maintenance préventive récurrente.
 *
 * <p>NOTE D'INTÉGRATION : quand une {@link Intervention} liée à un plan passe
 * au statut {@code REALISE} (typiquement dans {@code InterventionService}),
 * il faut appeler {@link #recalculerApresRealisation(Intervention)} pour que
 * la prochaine échéance du plan soit mise à jour. Voir le snippet fourni
 * séparément pour {@code InterventionService.mettreAJourStatut(...)}.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanMaintenanceService {

    private final PlanMaintenanceRepository planMaintenanceRepository;
    private final EquipementRepository equipementRepository;
    private final InterventionRepository interventionRepository;

    // ── CRUD ─────────────────────────────────────────────────────────────

    @Transactional
    public PlanMaintenanceResponse creer(PlanMaintenanceRequest request) {
        Equipement equipement = equipementRepository.findById(request.getEquipementId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Équipement introuvable : " + request.getEquipementId()));

        PlanMaintenance plan = PlanMaintenance.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .equipement(equipement)
                .frequenceValeur(request.getFrequenceValeur())
                .frequenceUnite(request.getFrequenceUnite())
                .dureeEstimee(request.getDureeEstimee())
                .typeReglementaire(request.getTypeReglementaire())
                .delaiAlerteJours(request.getDelaiAlerteJours() != null ? request.getDelaiAlerteJours() : 15)
                .actif(request.getActif() == null || request.getActif())
                .build();

        // Première échéance = aujourd'hui + fréquence (pas d'historique de départ)
        plan.recalculerEcheance(LocalDate.now());
        // recalculerEcheance() renseigne aussi derniereExecution, ce qui n'a pas
        // de sens pour un plan tout juste créé sans historique réel :
        plan.setDerniereExecution(null);

        PlanMaintenance saved = planMaintenanceRepository.save(plan);
        log.info("Plan de maintenance créé : id={}, nom={}", saved.getId(), saved.getNom());
        return PlanMaintenanceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PlanMaintenanceResponse> listerTout() {
        return planMaintenanceRepository.findAll().stream()
                .map(PlanMaintenanceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanMaintenanceResponse trouverParId(Integer id) {
        return PlanMaintenanceResponse.from(trouverEntiteParId(id));
    }

    @Transactional(readOnly = true)
    public List<PlanMaintenanceResponse> listerParEquipement(Integer equipementId) {
        return planMaintenanceRepository.findByEquipementId(equipementId).stream()
                .map(PlanMaintenanceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PlanMaintenanceResponse modifier(Integer id, PlanMaintenanceRequest request) {
        PlanMaintenance plan = trouverEntiteParId(id);

        if (request.getEquipementId() != null
                && !request.getEquipementId().equals(plan.getEquipement().getId())) {
            Equipement equipement = equipementRepository.findById(request.getEquipementId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Équipement introuvable : " + request.getEquipementId()));
            plan.setEquipement(equipement);
        }

        plan.setNom(request.getNom());
        plan.setDescription(request.getDescription());
        plan.setFrequenceValeur(request.getFrequenceValeur());
        plan.setFrequenceUnite(request.getFrequenceUnite());
        plan.setDureeEstimee(request.getDureeEstimee());
        plan.setTypeReglementaire(request.getTypeReglementaire());
        if (request.getDelaiAlerteJours() != null) {
            plan.setDelaiAlerteJours(request.getDelaiAlerteJours());
        }
        if (request.getActif() != null) {
            plan.setActif(request.getActif());
        }

        return PlanMaintenanceResponse.from(planMaintenanceRepository.save(plan));
    }

    @Transactional
    public void supprimer(Integer id) {
        if (!planMaintenanceRepository.existsById(id)) {
            throw new EntityNotFoundException("Plan de maintenance introuvable : " + id);
        }
        planMaintenanceRepository.deleteById(id);
    }

    // ── Génération d'ordres de travail ──────────────────────────────────

    /**
     * Génère manuellement une {@link Intervention} PREVENTIF à partir d'un plan
     * (appelé par le bouton "Générer l'ordre de travail" côté React).
     */
    @Transactional
    public Integer genererOrdreTravail(Integer planId) {
        PlanMaintenance plan = trouverEntiteParId(planId);
        return genererIntervention(plan).getId();
    }

    private Intervention genererIntervention(PlanMaintenance plan) {
        LocalDate dateReference = plan.getProchaineEcheance() != null
                ? plan.getProchaineEcheance()
                : LocalDate.now();

        Intervention intervention = Intervention.builder()
                .titre(plan.getNom())
                .description(plan.getDescription())
                .type(TypeIntervention.PREVENTIF)
                .statut(StatutIntervention.A_FAIRE)
                .dateDebut(dateReference.atTime(LocalTime.of(8, 0)))
                .dureeEstimee(plan.getDureeEstimee())
                .equipement(plan.getEquipement())
                .planMaintenance(plan)
                .build();

        Intervention saved = interventionRepository.save(intervention);
        log.info("Ordre de travail généré depuis le plan {} : intervention id={}", plan.getId(), saved.getId());
        return saved;
    }

    /**
     * À appeler quand une intervention liée à un plan passe au statut REALISE,
     * pour recalculer automatiquement la prochaine échéance.
     */
    @Transactional
    public void recalculerApresRealisation(Intervention intervention) {
        PlanMaintenance plan = intervention.getPlanMaintenance();
        if (plan == null) return;

        LocalDate dateRealisation = intervention.getDateFin() != null
                ? intervention.getDateFin().toLocalDate()
                : LocalDate.now();

        plan.recalculerEcheance(dateRealisation);
        planMaintenanceRepository.save(plan);
        log.info("Échéance recalculée pour le plan {} : prochaine = {}", plan.getId(), plan.getProchaineEcheance());
    }

    // ── Job planifié ─────────────────────────────────────────────────────

    /**
     * Génère automatiquement les ordres de travail pour les plans actifs
     * arrivant à échéance (dans la fenêtre de {@code delaiAlerteJours}),
     * en évitant les doublons si un ordre de travail non clos existe déjà
     * pour ce plan.
     *
     * <p>Exécuté chaque jour à 6h. Ajuste le cron selon tes besoins.</p>
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void genererOrdresTravailAutomatiques() {
        LocalDate aujourdHui = LocalDate.now();

        List<PlanMaintenance> eligibles = planMaintenanceRepository.findByActifTrue().stream()
                .filter(p -> p.eligibleGenerationAutomatique(aujourdHui))
                .toList();

        int genere = 0;
        for (PlanMaintenance plan : eligibles) {
            boolean ordreDejaOuvert = interventionRepository
                    .existsByPlanMaintenanceIdAndStatutNot(plan.getId(), StatutIntervention.REALISE);

            if (ordreDejaOuvert) {
                continue;
            }

            genererIntervention(plan);
            genere++;
        }

        if (genere > 0) {
            log.info("Génération automatique : {} ordre(s) de travail créé(s)", genere);
        }
    }
    private PlanMaintenance trouverEntiteParId(Integer id) {
        return planMaintenanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Plan de maintenance introuvable avec l'ID : " + id));
    }
}
