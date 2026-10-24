package com.gmao.controller;

import com.gmao.dto.NotificationCompteur;
import com.gmao.dto.NotificationItem;
import com.gmao.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contrôleur REST du centre de notifications/alertes.
 *
 * <pre>
 * GET /api/notifications           → Liste complète, triée par gravité
 * GET /api/notifications/compteur  → { total, critique, attention } pour un badge léger
 * </pre>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationItem>> listerTout() {
        return ResponseEntity.ok(notificationService.listerTout());
    }

    @GetMapping("/compteur")
    public ResponseEntity<NotificationCompteur> compteur() {
        return ResponseEntity.ok(notificationService.compteur());
    }
}
