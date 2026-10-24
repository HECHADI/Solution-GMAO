package com.gmao.service;

import com.gmao.dto.*;
import com.gmao.entity.RoleUtilisateur;
import com.gmao.entity.Utilisateur;
import com.gmao.service.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des comptes utilisateurs.
 *
 * <p>Pas de suppression définitive : {@code StockVehicule} a un
 * {@code ON DELETE CASCADE} sur {@code technicien_id}, supprimer un
 * technicien effacerait silencieusement tout son stock embarqué. On
 * utilise le champ {@code actif} existant pour désactiver un compte
 * sans perdre l'historique lié.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UtilisateurResponse creer(UtilisateurRequest request) {
        if (utilisateurRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalStateException("Un compte existe déjà avec cet email : " + request.getEmail());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .role(request.getRole())
                .actif(true)
                .build();

        Utilisateur saved = utilisateurRepository.save(utilisateur);
        log.info("Utilisateur créé : id={}, email={}, role={}", saved.getId(), saved.getEmail(), saved.getRole());
        return UtilisateurResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerTout() {
        return utilisateurRepository.findAll().stream()
                .map(UtilisateurResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerParRole(RoleUtilisateur role) {
        return utilisateurRepository.findByRole(role).stream()
                .map(UtilisateurResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse trouverParId(Integer id) {
        return UtilisateurResponse.from(trouverEntiteParId(id));
    }

    @Transactional
    public UtilisateurResponse modifier(Integer id, UtilisateurUpdateRequest request) {
        Utilisateur utilisateur = trouverEntiteParId(id);

        if (!utilisateur.getEmail().equalsIgnoreCase(request.getEmail())
                && utilisateurRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalStateException("Un compte existe déjà avec cet email : " + request.getEmail());
        }

        utilisateur.setNom(request.getNom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setRole(request.getRole());

        return UtilisateurResponse.from(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public void changerMotDePasse(Integer id, ChangerMotDePasseRequest request) {
        trouverEntiteParId(id); // 404 si inexistant
        String hash = passwordEncoder.encode(request.getNouveauMotDePasse());
        utilisateurRepository.updateMotDePasse(id, hash);
        log.info("Mot de passe changé pour l'utilisateur id={}", id);
    }

    @Transactional
    public UtilisateurResponse changerActif(Integer id, boolean actif) {
        Utilisateur utilisateur = trouverEntiteParId(id);
        utilisateurRepository.updateActif(id, actif);
        utilisateur.setActif(actif); // reflété dans la réponse sans recharger depuis la base
        log.info("Utilisateur id={} {}", id, actif ? "activé" : "désactivé");
        return UtilisateurResponse.from(utilisateur);
    }

    private Utilisateur trouverEntiteParId(Integer id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + id));
    }
}
