package com.gmao.service;

import com.gmao.dto.ChantierRequest;
import com.gmao.dto.ChantierResponse;
import com.gmao.entity.Chantier;
import com.gmao.entity.Intervention;
import com.gmao.entity.Site;
import com.gmao.entity.StatutChantier;
import com.gmao.entity.Utilisateur;
import com.gmao.service.repository.ChantierRepository;
import com.gmao.service.repository.InterventionRepository;
import com.gmao.service.repository.SiteRepository;
import com.gmao.service.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChantierService {

    private final ChantierRepository chantierRepository;
    private final SiteRepository siteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final InterventionRepository interventionRepository;

    @Transactional
    public ChantierResponse creer(ChantierRequest request) {
        Site site = trouverSite(request.getSiteId());
        Utilisateur responsable = request.getResponsableId() != null
                ? trouverUtilisateur(request.getResponsableId())
                : null;

        Chantier chantier = Chantier.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .site(site)
                .statut(request.getStatut() != null ? request.getStatut() : StatutChantier.PLANIFIE)
                .dateDebutPrevue(request.getDateDebutPrevue())
                .dateFinPrevue(request.getDateFinPrevue())
                .budgetEstime(request.getBudgetEstime())
                .responsable(responsable)
                .build();

        Chantier saved = chantierRepository.save(chantier);
        log.info("Chantier créé : id={}, nom={}", saved.getId(), saved.getNom());
        return ChantierResponse.from(saved, Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public List<ChantierResponse> listerTout() {
        return chantierRepository.findAll().stream()
            .map(c -> ChantierResponse.from(c, listerInterventionsParChantier(c.getId())))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChantierResponse trouverParId(Integer id) {
        Chantier chantier = trouverEntiteParId(id);
        List<Intervention> interventions = listerInterventionsParChantier(id);
        return ChantierResponse.from(chantier, interventions);
    }

    @Transactional(readOnly = true)
    public List<ChantierResponse> listerParSite(Integer siteId) {
        return chantierRepository.findBySiteId(siteId).stream()
                .map(c -> ChantierResponse.from(c, listerInterventionsParChantier(c.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public ChantierResponse modifier(Integer id, ChantierRequest request) {
        Chantier chantier = trouverEntiteParId(id);

        if (request.getSiteId() != null && !request.getSiteId().equals(chantier.getSite().getId())) {
            chantier.setSite(trouverSite(request.getSiteId()));
        }

        chantier.setNom(request.getNom());
        chantier.setDescription(request.getDescription());
        if (request.getStatut() != null) {
            chantier.setStatut(request.getStatut());
        }
        chantier.setDateDebutPrevue(request.getDateDebutPrevue());
        chantier.setDateFinPrevue(request.getDateFinPrevue());
        chantier.setBudgetEstime(request.getBudgetEstime());
        chantier.setResponsable(
                request.getResponsableId() != null ? trouverUtilisateur(request.getResponsableId()) : null);

        Chantier saved = chantierRepository.save(chantier);
        return ChantierResponse.from(saved, listerInterventionsParChantier(id));
    }

    private List<Intervention> listerInterventionsParChantier(Integer chantierId) {
        if (chantierId == null) return Collections.emptyList();
        return interventionRepository.findAll().stream()
                .filter(i -> i.getChantier() != null && chantierId.equals(i.getChantier().getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void rattacherIntervention(Integer interventionId, Integer chantierId) {
        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new EntityNotFoundException("Intervention introuvable : " + interventionId));

        Chantier chantier = chantierId != null ? trouverEntiteParId(chantierId) : null;
        intervention.setChantier(chantier);
        interventionRepository.save(intervention);
    }

    @Transactional
    public void supprimer(Integer id) {
        if (!chantierRepository.existsById(id)) {
            throw new EntityNotFoundException("Chantier introuvable : " + id);
        }
        chantierRepository.deleteById(id);
    }

    private Chantier trouverEntiteParId(Integer id) {
        return chantierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chantier introuvable : " + id));
    }

    private Site trouverSite(Integer id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Site introuvable : " + id));
    }

    private Utilisateur trouverUtilisateur(Integer id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + id));
    }
}