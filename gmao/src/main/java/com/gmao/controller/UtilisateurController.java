package com.gmao.controller;

import com.gmao.dto.*;
import com.gmao.entity.RoleUtilisateur;
import com.gmao.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les endpoints de gestion des utilisateurs.
 *
 * <pre>
 * POST   /api/utilisateurs                       → Créer                  [MANAGER uniquement]
 * GET    /api/utilisateurs                       → Lister tout (?role=TECHNICIEN pour filtrer)
 * GET    /api/utilisateurs/{id}                   → Détail
 * PUT    /api/utilisateurs/{id}                   → Modifier (nom/email/rôle) [MANAGER uniquement]
 * PATCH  /api/utilisateurs/{id}/actif             → Activer/désactiver        [MANAGER uniquement]
 * PATCH  /api/utilisateurs/{id}/mot-de-passe      → Changer le mot de passe
 * </pre>
 *
 * <p>La lecture (GET) reste ouverte à tout utilisateur authentifié (manager
 * ou technicien) — seules la création et la modification de comptes sont
 * réservées aux managers, maintenant que les techniciens peuvent aussi se
 * connecter à l'interface web.</p>
 *
 * <p>Pas de DELETE volontairement — voir la note dans {@code UtilisateurService}.</p>
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<UtilisateurResponse>> listerTout(
            @RequestParam(required = false) RoleUtilisateur role) {
        return ResponseEntity.ok(
                role != null ? utilisateurService.listerParRole(role) : utilisateurService.listerTout());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> trouverParId(@PathVariable Integer id) {
        return ResponseEntity.ok(utilisateurService.trouverParId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UtilisateurResponse> modifier(
            @PathVariable Integer id,
            @Valid @RequestBody UtilisateurUpdateRequest request) {
        return ResponseEntity.ok(utilisateurService.modifier(id, request));
    }

    @PatchMapping("/{id}/actif")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UtilisateurResponse> changerActif(
            @PathVariable Integer id,
            @Valid @RequestBody ActifRequest request) {
        return ResponseEntity.ok(utilisateurService.changerActif(id, request.getActif()));
    }

    @PatchMapping("/{id}/mot-de-passe")
    /**
     * Un manager peut changer le mot de passe de n'importe qui ; un technicien
     * ne peut changer que le sien (comparaison sur l'email du token, pas sur
     * l'id fourni dans l'URL qui pourrait être falsifié).
     */
    public ResponseEntity<Void> changerMotDePasse(
            @PathVariable Integer id,
            @Valid @RequestBody ChangerMotDePasseRequest request,
            org.springframework.security.core.Authentication authentication) {

        boolean estManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        if (!estManager) {
            UtilisateurResponse cible = utilisateurService.trouverParId(id);
            if (!cible.getEmail().equalsIgnoreCase(authentication.getName())) {
                return ResponseEntity.status(403).build();
            }
        }

        utilisateurService.changerMotDePasse(id, request);
        return ResponseEntity.ok().build();
    }
}
