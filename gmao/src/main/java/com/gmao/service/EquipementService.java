package com.gmao.service;

import com.gmao.dto.EquipementDetailResponse;
import com.gmao.dto.EquipementRequest;
import com.gmao.dto.EquipementResponse;
import com.gmao.entity.Equipement;
import com.gmao.entity.Site;
import com.gmao.entity.StatutEquipement;
import com.gmao.service.repository.EquipementRepository;
import com.gmao.service.repository.InterventionRepository;
import com.gmao.service.repository.PlanMaintenanceRepository;
import com.gmao.service.repository.SiteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des équipements.
 *
 * <p>NOTE D'INTÉGRATION : nécessite {@code List<Intervention> findByEquipementId(Integer)}
 * sur {@code InterventionRepository} (probablement déjà présent, utilisé par
 * {@code InterventionController.listerParEquipement(...)}), et
 * {@code List<PlanMaintenance> findByEquipementId(Integer)} sur
 * {@code PlanMaintenanceRepository} — déjà ajouté lors de la livraison de
 * PlanMaintenance ({@code listerParEquipement}).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EquipementService {

    private final EquipementRepository equipementRepository;
    private final SiteRepository siteRepository;
    private final InterventionRepository interventionRepository;
    private final PlanMaintenanceRepository planMaintenanceRepository;

    @Transactional
    public EquipementResponse creer(EquipementRequest request) {
        Site site = trouverSite(request.getSiteId());

        Equipement equipement = Equipement.builder()
                .nom(request.getNom())
                .categorie(request.getCategorie())
                .marque(request.getMarque())
                .modele(request.getModele())
                .numeroSerie(request.getNumeroSerie())
                .dateInstallation(request.getDateInstallation())
                .statut(request.getStatut() != null ? request.getStatut() : StatutEquipement.EN_SERVICE)
                .site(site)
                .build();

        Equipement saved = equipementRepository.save(equipement);
        log.info("Équipement créé : id={}, nom={}", saved.getId(), saved.getNom());
        return EquipementResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<EquipementResponse> listerTout() {
        return equipementRepository.findAll().stream()
                .map(EquipementResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EquipementDetailResponse trouverDetailParId(Integer id) {
        Equipement equipement = trouverEntiteParId(id);
        return EquipementDetailResponse.from(
                equipement,
                interventionRepository.findByEquipementId(id),
                planMaintenanceRepository.findByEquipementId(id));
    }

    @Transactional
    public EquipementResponse modifier(Integer id, EquipementRequest request) {
        Equipement equipement = trouverEntiteParId(id);

        if (request.getSiteId() != null && !request.getSiteId().equals(equipement.getSite().getId())) {
            equipement.setSite(trouverSite(request.getSiteId()));
        }

        equipement.setNom(request.getNom());
        equipement.setCategorie(request.getCategorie());
        equipement.setMarque(request.getMarque());
        equipement.setModele(request.getModele());
        equipement.setNumeroSerie(request.getNumeroSerie());
        equipement.setDateInstallation(request.getDateInstallation());
        if (request.getStatut() != null) {
            equipement.setStatut(request.getStatut());
        }

        return EquipementResponse.from(equipementRepository.save(equipement));
    }

    @Transactional
    public EquipementResponse changerStatut(Integer id, StatutEquipement statut) {
        Equipement equipement = trouverEntiteParId(id);
        equipement.setStatut(statut);
        return EquipementResponse.from(equipementRepository.save(equipement));
    }

    @Transactional
    public void supprimer(Integer id) {
        Equipement equipement = trouverEntiteParId(id);

        boolean aDesInterventions = !interventionRepository.findByEquipementId(id).isEmpty();
        if (aDesInterventions) {
            throw new IllegalStateException(
                    "Impossible de supprimer « " + equipement.getNom() + " » : des interventions y sont rattachées.");
        }

        // Les plans de maintenance liés sont supprimés automatiquement (ON DELETE CASCADE)
        equipementRepository.deleteById(id);
    }

    private Equipement trouverEntiteParId(Integer id) {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Équipement introuvable : " + id));
    }

    private Site trouverSite(Integer id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Site introuvable : " + id));
    }
}
