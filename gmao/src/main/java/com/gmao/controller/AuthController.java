package com.gmao.controller;

import com.gmao.dto.*;
import com.gmao.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST d'authentification.
 *
 * <pre>
 * POST /api/auth/login                        → Connexion, renvoie un JWT
 * GET  /api/auth/me                            → Utilisateur courant (nécessite un token valide)
 * POST /api/auth/mot-de-passe-oublie           → Envoie un lien de réinitialisation par email
 * POST /api/auth/reinitialiser-mot-de-passe    → Applique le nouveau mot de passe via le token reçu par email
 * </pre>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UtilisateurResponse> utilisateurCourant(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(authService.utilisateurCourant(principal.getUsername()));
    }

    @PostMapping("/mot-de-passe-oublie")
    public ResponseEntity<Map<String, String>> motDePasseOublie(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.demanderReinitialisation(request.getEmail());
        // Message identique que l'email existe ou non — évite l'énumération de comptes
        return ResponseEntity.ok(Map.of(
                "message", "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé."));
    }

    @PostMapping("/reinitialiser-mot-de-passe")
    public ResponseEntity<Map<String, String>> reinitialiserMotDePasse(@Valid @RequestBody ResetPasswordRequest request) {
        authService.reinitialiserMotDePasse(request.getToken(), request.getNouveauMotDePasse());
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès."));
    }
}
