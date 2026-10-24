package com.gmao.controller;

import com.gmao.dto.ParametreRequest;
import com.gmao.dto.ParametreResponse;
import com.gmao.service.ParametreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST des paramètres applicatifs.
 *
 * <pre>
 * GET /api/parametres         → Lister tout
 * PUT /api/parametres/{cle}   → Modifier la valeur d'un paramètre
 * </pre>
 */
@RestController
@RequestMapping("/api/parametres")
@RequiredArgsConstructor
public class ParametreController {

    private final ParametreService parametreService;

    @GetMapping
    public ResponseEntity<List<ParametreResponse>> listerTout() {
        return ResponseEntity.ok(parametreService.listerTout());
    }

    @PutMapping("/{cle}")
    public ResponseEntity<ParametreResponse> modifier(
            @PathVariable String cle,
            @Valid @RequestBody ParametreRequest request) {
        return ResponseEntity.ok(parametreService.modifierValeur(cle, request.getValeur()));
    }
}
