package com.gmao.service;

import com.gmao.dto.LoginRequest;
import com.gmao.dto.LoginResponse;
import com.gmao.dto.UtilisateurResponse;
import com.gmao.entity.PasswordResetToken;
import com.gmao.entity.Utilisateur;
import com.gmao.service.repository.PasswordResetTokenRepository;
import com.gmao.security.JwtService;
import com.gmao.service.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    /** Durée de validité d'un lien de réinitialisation. */
    private static final long EXPIRATION_TOKEN_HEURES = 1;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ── Connexion ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (!utilisateur.isActif()) {
            throw new BadCredentialsException("Ce compte a été désactivé");
        }

        String token = jwtService.genererToken(
                utilisateur.getEmail(), utilisateur.getId(), utilisateur.getRole().name());

        log.info("Connexion réussie : {} ({})", utilisateur.getEmail(), utilisateur.getRole());

        return LoginResponse.builder()
                .token(token)
                .utilisateur(UtilisateurResponse.from(utilisateur))
                .build();
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse utilisateurCourant(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
        return UtilisateurResponse.from(utilisateur);
    }

    // ── Mot de passe oublié ────────────────────────────────────────────

    /**
     * Ne révèle jamais si l'email existe ou non (réponse identique dans les
     * deux cas côté contrôleur) — évite l'énumération de comptes.
     */
    @Transactional
    public void demanderReinitialisation(String email) {
        utilisateurRepository.findByEmailIgnoreCase(email).ifPresent(utilisateur -> {
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .utilisateur(utilisateur)
                    .dateExpiration(LocalDateTime.now().plusHours(EXPIRATION_TOKEN_HEURES))
                    .utilise(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String lien = frontendUrl + "/reinitialiser-mot-de-passe?token=" + token;
            emailService.envoyerLienReinitialisation(utilisateur.getEmail(), utilisateur.getNom(), lien);
        });
    }

    @Transactional
    public void reinitialiserMotDePasse(String token, String nouveauMotDePasse) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Lien de réinitialisation invalide"));

        if (!resetToken.estValide()) {
            throw new IllegalStateException("Ce lien de réinitialisation a expiré ou a déjà été utilisé");
        }

        Utilisateur utilisateur = resetToken.getUtilisateur();
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(utilisateur);

        resetToken.setUtilise(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Mot de passe réinitialisé pour : {}", utilisateur.getEmail());
    }
}
