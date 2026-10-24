package com.gmao.service;

import com.gmao.dto.ParametreResponse;
import com.gmao.entity.ParametreApplication;
import com.gmao.service.repository.ParametreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des paramètres applicatifs.
 *
 * <p>Les méthodes {@code lireValeur*} sont pensées pour être appelées depuis
 * d'autres services (ex: le futur module Facturation lira
 * {@code lireValeurDecimal("taux_horaire_defaut", ...)} plutôt que de
 * hardcoder 50.00 comme dans le mock initial).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParametreService {

    private final ParametreRepository parametreRepository;

    @Transactional(readOnly = true)
    public List<ParametreResponse> listerTout() {
        return parametreRepository.findAll().stream()
                .map(ParametreResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParametreResponse modifierValeur(String cle, String nouvelleValeur) {
        ParametreApplication parametre = parametreRepository.findByCle(cle)
                .orElseThrow(() -> new EntityNotFoundException("Paramètre introuvable : " + cle));

        parametre.setValeur(nouvelleValeur);
        ParametreApplication saved = parametreRepository.save(parametre);
        log.info("Paramètre modifié : {} = {}", cle, nouvelleValeur);
        return ParametreResponse.from(saved);
    }

    // ── Lecture typée, pour usage interne par d'autres services ──────────

    @Transactional(readOnly = true)
    public String lireValeur(String cle, String valeurParDefaut) {
        return parametreRepository.findByCle(cle)
                .map(ParametreApplication::getValeur)
                .orElse(valeurParDefaut);
    }

    @Transactional(readOnly = true)
    public BigDecimal lireValeurDecimal(String cle, BigDecimal valeurParDefaut) {
        try {
            return new BigDecimal(lireValeur(cle, valeurParDefaut.toString()));
        } catch (NumberFormatException e) {
            log.warn("Paramètre '{}' invalide, valeur par défaut utilisée", cle);
            return valeurParDefaut;
        }
    }

    @Transactional(readOnly = true)
    public int lireValeurEntier(String cle, int valeurParDefaut) {
        try {
            return Integer.parseInt(lireValeur(cle, String.valueOf(valeurParDefaut)));
        } catch (NumberFormatException e) {
            log.warn("Paramètre '{}' invalide, valeur par défaut utilisée", cle);
            return valeurParDefaut;
        }
    }
}
