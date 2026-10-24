package com.gmao.service;

import com.gmao.dto.SiteRequest;
import com.gmao.dto.SiteResponse;
import com.gmao.entity.Client;
import com.gmao.entity.Site;
import com.gmao.service.repository.ChantierRepository;
import com.gmao.service.repository.ClientRepository;
import com.gmao.service.repository.EquipementRepository;
import com.gmao.service.repository.SiteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des sites.
 *
 * <p>NOTE D'INTÉGRATION : réutilise {@code EquipementRepository.findBySiteId}
 * (créé avec le module Équipements) et {@code ChantierRepository.findBySiteId}
 * (créé avec le module Chantiers) pour empêcher la suppression d'un site
 * encore utilisé.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SiteService {

    private final SiteRepository siteRepository;
    private final ClientRepository clientRepository;
    private final EquipementRepository equipementRepository;
    private final ChantierRepository chantierRepository;

    @Transactional
    public SiteResponse creer(SiteRequest request) {
        Client client = trouverClient(request.getClientId());

        Site site = Site.builder()
                .nom(request.getNom())
                .adresse(request.getAdresse())
                .coordonneesGps(request.getCoordonneesGps())
                .client(client)
                .build();

        Site saved = siteRepository.save(site);
        log.info("Site créé : id={}, nom={}", saved.getId(), saved.getNom());
        return SiteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> listerTout() {
        return siteRepository.findAll().stream()
                .map(SiteResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> listerParClient(Integer clientId) {
        return siteRepository.findByClientId(clientId).stream()
                .map(SiteResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SiteResponse trouverParId(Integer id) {
        return SiteResponse.from(trouverEntiteParId(id));
    }

    @Transactional
    public SiteResponse modifier(Integer id, SiteRequest request) {
        Site site = trouverEntiteParId(id);

        if (request.getClientId() != null && !request.getClientId().equals(site.getClient().getId())) {
            site.setClient(trouverClient(request.getClientId()));
        }

        site.setNom(request.getNom());
        site.setAdresse(request.getAdresse());
        site.setCoordonneesGps(request.getCoordonneesGps());

        return SiteResponse.from(siteRepository.save(site));
    }

    @Transactional
    public void supprimer(Integer id) {
        Site site = trouverEntiteParId(id);

        boolean aDesEquipements = !equipementRepository.findBySiteId(id).isEmpty();
        if (aDesEquipements) {
            throw new IllegalStateException(
                    "Impossible de supprimer « " + site.getNom() + " » : des équipements y sont encore rattachés.");
        }

        boolean aDesChantiers = !chantierRepository.findBySiteId(id).isEmpty();
        if (aDesChantiers) {
            throw new IllegalStateException(
                    "Impossible de supprimer « " + site.getNom() + " » : des chantiers y sont encore rattachés.");
        }

        siteRepository.deleteById(id);
    }

    private Site trouverEntiteParId(Integer id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Site introuvable : " + id));
    }

    private Client trouverClient(Integer id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable : " + id));
    }
}
