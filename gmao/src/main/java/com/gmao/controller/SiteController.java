package com.gmao.controller;

import com.gmao.dto.SiteRequest;
import com.gmao.dto.SiteResponse;
import com.gmao.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les endpoints de gestion des sites.
 * Sert à la fois pour le CRUD complet et pour alimenter les menus déroulants du frontend.
 *
 * <pre>
 * POST   /api/sites                    → Créer
 * GET    /api/sites                    → Lister tout
 * GET    /api/sites/{id}                → Détail
 * GET    /api/sites/client/{clientId}   → Par client
 * PUT    /api/sites/{id}                → Modifier
 * DELETE /api/sites/{id}                → Supprimer (refusé si équipements/chantiers rattachés)
 * </pre>
 */
@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @PostMapping
    public ResponseEntity<SiteResponse> creer(@Valid @RequestBody SiteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siteService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<SiteResponse>> listerTout() {
        return ResponseEntity.ok(siteService.listerTout());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteResponse> trouverParId(@PathVariable Integer id) {
        return ResponseEntity.ok(siteService.trouverParId(id));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<SiteResponse>> listerParClient(@PathVariable Integer clientId) {
        return ResponseEntity.ok(siteService.listerParClient(clientId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteResponse> modifier(
            @PathVariable Integer id,
            @Valid @RequestBody SiteRequest request) {
        return ResponseEntity.ok(siteService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        siteService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}