package com.gmao.controller;

import com.gmao.dto.ClientRequest;
import com.gmao.dto.ClientResponse;
import com.gmao.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les endpoints de gestion des clients.
 *
 * <pre>
 * POST   /api/clients        → Créer
 * GET    /api/clients        → Lister tout
 * GET    /api/clients/{id}   → Détail
 * PUT    /api/clients/{id}   → Modifier
 * DELETE /api/clients/{id}   → Supprimer (refusé si des sites y sont rattachés)
 * </pre>
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> creer(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> listerTout() {
        return ResponseEntity.ok(clientService.listerTout());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> trouverParId(@PathVariable Integer id) {
        return ResponseEntity.ok(clientService.trouverParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> modifier(
            @PathVariable Integer id,
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        clientService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}