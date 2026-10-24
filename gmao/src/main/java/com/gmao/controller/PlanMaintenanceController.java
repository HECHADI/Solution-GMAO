package com.gmao.controller;

import com.gmao.dto.PlanMaintenanceRequest;
import com.gmao.dto.PlanMaintenanceResponse;
import com.gmao.service.PlanMaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST exposant les endpoints de gestion des plans de maintenance
 * préventive récurrente.
 *
 * <pre>
 * POST   /api/plans-maintenance                         → Créer
 * GET    /api/plans-maintenance                          → Lister tout
 * GET    /api/plans-maintenance/{id}                     → Détail
 * GET    /api/plans-maintenance/equipement/{equipementId} → Par équipement
 * PUT    /api/plans-maintenance/{id}                     → Modifier
 * DELETE /api/plans-maintenance/{id}                     → Supprimer
 * POST   /api/plans-maintenance/{id}/generer             → Générer un ordre de travail manuellement
 * </pre>
 */
@RestController
@RequestMapping("/api/plans-maintenance")
@RequiredArgsConstructor
public class PlanMaintenanceController {

    private final PlanMaintenanceService planMaintenanceService;

    @PostMapping
    public ResponseEntity<PlanMaintenanceResponse> creer(
            @Valid @RequestBody PlanMaintenanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planMaintenanceService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<PlanMaintenanceResponse>> listerTout() {
        return ResponseEntity.ok(planMaintenanceService.listerTout());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanMaintenanceResponse> trouverParId(@PathVariable Integer id) {
        return ResponseEntity.ok(planMaintenanceService.trouverParId(id));
    }

    @GetMapping("/equipement/{equipementId}")
    public ResponseEntity<List<PlanMaintenanceResponse>> listerParEquipement(
            @PathVariable Integer equipementId) {
        return ResponseEntity.ok(planMaintenanceService.listerParEquipement(equipementId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanMaintenanceResponse> modifier(
            @PathVariable Integer id,
            @Valid @RequestBody PlanMaintenanceRequest request) {
        return ResponseEntity.ok(planMaintenanceService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        planMaintenanceService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Génère manuellement un ordre de travail (Intervention) à partir de ce plan.
     * Correspond au bouton "🗓️ Générer l'ordre de travail" côté React.
     */
    @PostMapping("/{id}/generer")
    public ResponseEntity<Map<String, Integer>> genererOrdreTravail(@PathVariable Integer id) {
        Integer interventionId = planMaintenanceService.genererOrdreTravail(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("interventionId", interventionId));
    }
}
