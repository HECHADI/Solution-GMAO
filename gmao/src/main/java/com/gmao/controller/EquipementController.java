package com.gmao.controller;

import com.gmao.dto.EquipementDetailResponse;
import com.gmao.dto.EquipementRequest;
import com.gmao.dto.EquipementResponse;
import com.gmao.dto.StatutEquipementRequest;
import com.gmao.service.EquipementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les endpoints de gestion des équipements.
 *
 * <pre>
 * POST   /api/equipements                  → Créer
 * GET    /api/equipements                  → Lister tout
 * GET    /api/equipements/{id}              → Fiche détail (+ historique + plans de maintenance)
 * PUT    /api/equipements/{id}              → Modifier
 * PATCH  /api/equipements/{id}/statut       → Changer uniquement le statut
 * DELETE /api/equipements/{id}              → Supprimer (refusé si des interventions y sont rattachées)
 * </pre>
 */
@RestController
@RequestMapping("/api/equipements")
@RequiredArgsConstructor
public class EquipementController {

    private final EquipementService equipementService;

    @PostMapping
    public ResponseEntity<EquipementResponse> creer(@Valid @RequestBody EquipementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipementService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<EquipementResponse>> listerTout() {
        return ResponseEntity.ok(equipementService.listerTout());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipementDetailResponse> trouverDetailParId(@PathVariable Integer id) {
        return ResponseEntity.ok(equipementService.trouverDetailParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipementResponse> modifier(
            @PathVariable Integer id,
            @Valid @RequestBody EquipementRequest request) {
        return ResponseEntity.ok(equipementService.modifier(id, request));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<EquipementResponse> changerStatut(
            @PathVariable Integer id,
            @Valid @RequestBody StatutEquipementRequest request) {
        return ResponseEntity.ok(equipementService.changerStatut(id, request.getStatut()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        equipementService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
