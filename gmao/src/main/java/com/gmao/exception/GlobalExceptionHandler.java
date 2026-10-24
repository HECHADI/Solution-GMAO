package com.gmao.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Traduit les exceptions levées par les services en réponses JSON
 * `{ "message": "..." }` cohérentes — c'est le format que tout le frontend
 * (tous les hooks `useXxx.js`) attend via `err.response?.data?.message`.
 *
 * Sans ce handler, Spring Boot renvoie un 500 générique (ou un format
 * d'erreur par défaut différent selon le cas), et le frontend retombe sur
 * ses messages de secours ("réessaie plus tard"…) qui masquent la vraie
 * cause — d'où la confusion sur le login / mot de passe oublié.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return reponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return reponse(HttpStatus.FORBIDDEN, "Action non autorisée pour ce rôle.");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException ex) {
        return reponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Utilisée dans nos services pour les conflits métier (email déjà pris,
     * suppression bloquée par des données liées, token de réinitialisation
     * invalide/expiré...).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return reponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return reponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Erreurs de validation des DTOs (@Valid / @NotBlank / @Email...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Données invalides.");
        return reponse(HttpStatus.BAD_REQUEST, message);
    }

    /** Filet de sécurité : n'importe quelle autre exception non prévue. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.error("Erreur non gérée", ex);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur inattendue est survenue.");
    }

    private ResponseEntity<Map<String, String>> reponse(HttpStatus statut, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return ResponseEntity.status(statut).body(body);
    }
}