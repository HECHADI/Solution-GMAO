package com.gmao.controller;

import com.gmao.dto.ChantierRequest;
import com.gmao.dto.ChantierResponse;
import com.gmao.service.ChantierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST exposant les endpoints de gestion des chantiers/projets.
 *
 * <pre>
 * POST   /api/chantiers                            → Créer
 * GET    /api/chantiers                            → Lister tout
 * GET    /api/chantiers/{id}                        → Détail (avec interventions liées)
 * GET    /api/chantiers/site/{siteId}               → Par site
 * PUT    /api/chantiers/{id}                        → Modifier
 * DELETE /api/chantiers/{id}                        → Supprimer
 * PATCH  /api/chantiers/interventions/{interventionId}/rattacher → Rattacher/détacher une intervention
 * </pre>
 */
@RestController
@RequestMapping("/api/chantiers")
@RequiredArgsConstructor
public class ChantierController {

    private final ChantierService chantierService;

    @PostMapping
    public ResponseEntity<ChantierResponse> creer(@Valid @RequestBody ChantierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chantierService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<ChantierResponse>> listerTout() {
        return ResponseEntity.ok(chantierService.listerTout());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChantierResponse> trouverParId(@PathVariable Integer id) {
        return ResponseEntity.ok(chantierService.trouverParId(id));
    }

    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<ChantierResponse>> listerParSite(@PathVariable Integer siteId) {
        return ResponseEntity.ok(chantierService.listerParSite(siteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChantierResponse> modifier(
            @PathVariable Integer id,
            @Valid @RequestBody ChantierRequest request) {
        return ResponseEntity.ok(chantierService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        chantierService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/interventions/{interventionId}/rattacher")
    public ResponseEntity<Void> rattacherIntervention(
            @PathVariable Integer interventionId,
            @RequestBody Map<String, Integer> body) {
        chantierService.rattacherIntervention(interventionId, body.get("chantierId"));
        return ResponseEntity.ok().build();
    }
}
