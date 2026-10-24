package com.gmao.controller;

import com.gmao.dto.*;
import com.gmao.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les endpoints de gestion du stock embarqué.
 *
 * <pre>
 * GET    /api/stock/pieces                              → Catalogue complet
 * PATCH  /api/stock/pieces/{id}/seuil                    → Modifier le seuil d'alerte
 * GET    /api/stock/vehicules                            → Tout le stock embarqué
 * GET    /api/stock/vehicules/technicien/{technicienId}  → Stock d'un technicien
 * POST   /api/stock/vehicules/approvisionner             → Ajouter du stock (dépôt → véhicule)
 * POST   /api/stock/vehicules/transferer                 → Transférer entre deux techniciens
 * PATCH  /api/stock/vehicules/ajuster                    → Correction manuelle (inventaire)
 * GET    /api/stock/mouvements                           → Historique complet
 * GET    /api/stock/mouvements/piece/{pieceId}           → Historique d'une pièce
 * GET    /api/stock/mouvements/technicien/{technicienId} → Historique d'un technicien
 * </pre>
 */
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // ── Catalogue ────────────────────────────────────────────────────────

    @GetMapping("/pieces")
    public ResponseEntity<List<PieceDetacheeResponse>> listerCatalogue() {
        return ResponseEntity.ok(stockService.listerCatalogue());
    }

    @PatchMapping("/pieces/{id}/seuil")
    public ResponseEntity<PieceDetacheeResponse> modifierSeuilAlerte(
            @PathVariable Integer id,
            @Valid @RequestBody SeuilAlerteRequest request) {
        return ResponseEntity.ok(stockService.modifierSeuilAlerte(id, request));
    }

    // ── Stock embarqué ──────────────────────────────────────────────────

    @GetMapping("/vehicules")
    public ResponseEntity<List<StockVehiculeResponse>> listerToutStock() {
        return ResponseEntity.ok(stockService.listerToutStock());
    }

    @GetMapping("/vehicules/technicien/{technicienId}")
    public ResponseEntity<List<StockVehiculeResponse>> listerStockParTechnicien(
            @PathVariable Integer technicienId) {
        return ResponseEntity.ok(stockService.listerStockParTechnicien(technicienId));
    }

    @PostMapping("/vehicules/approvisionner")
    public ResponseEntity<StockVehiculeResponse> approvisionner(
            @Valid @RequestBody ApprovisionnementRequest request) {
        return ResponseEntity.ok(stockService.approvisionner(request));
    }

    @PostMapping("/vehicules/transferer")
    public ResponseEntity<Void> transferer(@Valid @RequestBody TransfertRequest request) {
        stockService.transferer(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/vehicules/ajuster")
    public ResponseEntity<StockVehiculeResponse> ajuster(@Valid @RequestBody AjustementRequest request) {
        return ResponseEntity.ok(stockService.ajuster(request));
    }

    // ── Historique ────────────────────────────────────────────────────

    @GetMapping("/mouvements")
    public ResponseEntity<List<MouvementStockResponse>> historiqueComplet() {
        return ResponseEntity.ok(stockService.historiqueComplet());
    }

    @GetMapping("/mouvements/piece/{pieceId}")
    public ResponseEntity<List<MouvementStockResponse>> historiqueParPiece(@PathVariable Integer pieceId) {
        return ResponseEntity.ok(stockService.historiqueParPiece(pieceId));
    }

    @GetMapping("/mouvements/technicien/{technicienId}")
    public ResponseEntity<List<MouvementStockResponse>> historiqueParTechnicien(
            @PathVariable Integer technicienId) {
        return ResponseEntity.ok(stockService.historiqueParTechnicien(technicienId));
    }
}
