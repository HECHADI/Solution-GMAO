package com.gmao.service;

import com.gmao.dto.ClientRequest;
import com.gmao.dto.ClientResponse;
import com.gmao.entity.Client;
import com.gmao.service.repository.ClientRepository;
import com.gmao.service.repository.SiteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des clients.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final SiteRepository siteRepository;

    @Transactional
    public ClientResponse creer(ClientRequest request) {
        // 🟢 Initialisation explicite des dates pour éviter la Nullability Exception avec @Builder
        LocalDateTime maintenant = LocalDateTime.now();

        Client client = Client.builder()
                .nom(request.getNom())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .createdAt(maintenant)    // 🟢 Fix : Renseigné explicitement
                .updatedAt(maintenant)    // 🟢 Fix : Renseigné explicitement
                .build();

        Client saved = clientRepository.save(client);
        log.info("Client créé avec succès : id={}, nom={}", saved.getId(), saved.getNom());
        return ClientResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> listerTout() {
        return clientRepository.findAll().stream()
                .map(ClientResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientResponse trouverParId(Integer id) {
        return ClientResponse.from(trouverEntiteParId(id));
    }

    @Transactional
    public ClientResponse modifier(Integer id, ClientRequest request) {
        Client client = trouverEntiteParId(id);
        client.setNom(request.getNom());
        client.setAdresse(request.getAdresse());
        client.setTelephone(request.getTelephone());
        client.setEmail(request.getEmail());
        client.setUpdatedAt(LocalDateTime.now()); // 🟢 Met à jour la date de modification
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public void supprimer(Integer id) {
        Client client = trouverEntiteParId(id);

        boolean aDesSites = !siteRepository.findByClientId(id).isEmpty();
        if (aDesSites) {
            throw new IllegalStateException(
                    "Impossible de supprimer « " + client.getNom() + " » : des sites y sont encore rattachés.");
        }

        clientRepository.deleteById(id);
    }

    private Client trouverEntiteParId(Integer id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable : " + id));
    }
}