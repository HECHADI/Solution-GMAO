package com.gmao.controller;

import com.gmao.dto.AssignationRequest;
import com.gmao.dto.ClotureRequest;
import com.gmao.dto.DatesMajRequest;
import com.gmao.dto.InterventionRequest;
import com.gmao.dto.InterventionResponse;
import com.gmao.entity.StatutIntervention;
import com.gmao.service.InterventionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les endpoints de gestion des interventions GMAO.
 *
 * <pre>
 * POST   /api/interventions                              → Créer
 * GET    /api/interventions                              → Lister tout
 * GET    /api/interventions/{id}                         → Détail
 * GET    /api/interventions/non-assignees                → Sans technicien
 * GET    /api/interventions/equipement/{equipementId}    → Par équipement
 * GET    /api/interventions/technicien/{technicienId}    → Par technicien
 * PATCH  /api/interventions/{id}/assigner               → Assigner technicien
 * PATCH  /api/interventions/{id}/dates                  → MAJ dates
 * PATCH  /api/interventions/{id}/statut                 → MAJ statut
 * DELETE /api/interventions/{id}                        → Supprimer
 * </pre>
 */
@RestController
@RequestMapping("/api/interventions")
@RequiredArgsConstructor
public class InterventionController {

    private final InterventionService interventionService;

    // ── POST /api/interventions ───────────────────────────────────────────────

    /**
     * Crée une nouvelle intervention.
     *
     * @param request Corps JSON contenant les données de l'intervention
     * @return 201 Created avec l'intervention créée
     */
    @PostMapping
    public ResponseEntity<InterventionResponse> creer(
            @Valid @RequestBody InterventionRequest request) {

        InterventionResponse response = interventionService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/interventions ────────────────────────────────────────────────

    /**
     * Retourne la liste de toutes les interventions.
     *
     * @return 200 OK avec la liste
     */
    @GetMapping
    public ResponseEntity<List<InterventionResponse>> listerTout() {
        return ResponseEntity.ok(interventionService.listerTout());
    }

    /**
     * Retourne le détail d'une intervention.
     *
     * @param id Identifiant de l'intervention
     * @return 200 OK ou 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<InterventionResponse> trouverParId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(interventionService.trouverParId(id));
    }

    /**
     * Retourne les interventions non encore assignées à un technicien.
     *
     * @return 200 OK avec la liste
     */
    @GetMapping("/non-assignees")
    public ResponseEntity<List<InterventionResponse>> listerNonAssignees() {
        return ResponseEntity.ok(interventionService.listerNonAssignees());
    }

    /**
     * Retourne toutes les interventions d'un équipement donné.
     *
     * @param equipementId Identifiant de l'équipement
     * @return 200 OK ou 404 si l'équipement n'existe pas
     */
    @GetMapping("/equipement/{equipementId}")
    public ResponseEntity<List<InterventionResponse>> listerParEquipement(
            @PathVariable Integer equipementId) {

        return ResponseEntity.ok(interventionService.listerParEquipement(equipementId));
    }

    /**
     * Retourne toutes les interventions assignées à un technicien.
     *
     * @param technicienId Identifiant du technicien
     * @return 200 OK ou 404 si le technicien n'existe pas
     */
    @GetMapping("/technicien/{technicienId}")
    public ResponseEntity<List<InterventionResponse>> listerParTechnicien(
            @PathVariable Integer technicienId) {

        return ResponseEntity.ok(interventionService.listerParTechnicien(technicienId));
    }

    // ── PATCH /api/interventions/{id}/assigner ────────────────────────────────

    /**
     * Assigne un technicien à une intervention existante.
     *
     * <p>Règles métier appliquées :</p>
     * <ul>
     *   <li>L'utilisateur doit exister et avoir le rôle TECHNICIEN</li>
     *   <li>Le technicien doit être actif</li>
     *   <li>Une intervention REALISE ne peut plus être réassignée</li>
     * </ul>
     *
     * @param id      Identifiant de l'intervention
     * @param request Corps JSON avec {@code technicienId}
     * @return 200 OK avec l'intervention mise à jour
     */
    @PatchMapping("/{id}/assigner")
    public ResponseEntity<InterventionResponse> assignerTechnicien(
            @PathVariable Integer id,
            @Valid @RequestBody AssignationRequest request) {

        return ResponseEntity.ok(interventionService.assignerTechnicien(id, request));
    }

    // ── PATCH /api/interventions/{id}/dates ───────────────────────────────────

    /**
     * Met à jour les dates et/ou la durée estimée d'une intervention.
     *
     * <p>Mise à jour automatique du statut :</p>
     * <ul>
     *   <li>{@code dateFin} fournie → statut passe à REALISE</li>
     *   <li>{@code dateDebut} fournie (sans dateFin) → statut passe à EN_COURS si A_FAIRE</li>
     * </ul>
     *
     * @param id      Identifiant de l'intervention
     * @param request Corps JSON avec les dates à mettre à jour
     * @return 200 OK avec l'intervention mise à jour
     */
    @PatchMapping("/{id}/dates")
    public ResponseEntity<InterventionResponse> mettreAJourDates(
            @PathVariable Integer id,
            @Valid @RequestBody DatesMajRequest request) {

        return ResponseEntity.ok(interventionService.mettreAJourDates(id, request));
    }

    // ── PATCH /api/interventions/{id}/statut ──────────────────────────────────

    /**
     * Met à jour manuellement le statut d'une intervention.
     *
     * @param id     Identifiant de l'intervention
     * @param statut Nouveau statut en query param (A_FAIRE, EN_COURS, REALISE)
     * @return 200 OK avec l'intervention mise à jour
     */
    @PatchMapping("/{id}/statut")
    public ResponseEntity<InterventionResponse> mettreAJourStatut(
            @PathVariable Integer id,
            @RequestParam StatutIntervention statut) {

        return ResponseEntity.ok(interventionService.mettreAJourStatut(id, statut));
    }

    @PostMapping("/{id}/cloturer")
    public ResponseEntity<InterventionResponse> cloturer(
            @PathVariable Integer id,
            @Valid @RequestBody ClotureRequest request) {
        return ResponseEntity.ok(interventionService.cloturer(id, request));
    }

    // ── DELETE /api/interventions/{id} ────────────────────────────────────────

    /**
     * Supprime une intervention.
     *
     * @param id Identifiant de l'intervention
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        interventionService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
