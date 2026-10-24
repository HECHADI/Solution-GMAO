package com.gmao.controller;

import com.gmao.dto.FactureRequest;
import com.gmao.dto.FactureResponse;
import com.gmao.dto.InterventionAFacturerResponse;
import com.gmao.entity.StatutFacture;
import com.gmao.service.FactureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST exposant les endpoints de facturation.
 *
 * <pre>
 * GET    /api/facturation/a-facturer                    → Interventions REALISE non encore facturées
 * GET    /api/facturation/a-facturer/client/{clientId}   → Idem, filtré par client
 * POST   /api/facturation/factures                       → Générer une facture
 * GET    /api/facturation/factures                       → Lister toutes les factures
 * GET    /api/facturation/factures/{id}                  → Détail d'une facture
 * GET    /api/facturation/factures/{id}/pdf               → Télécharger la facture en PDF
 * GET    /api/facturation/factures/client/{clientId}      → Factures d'un client
 * GET    /api/facturation/factures/statut/{statut}        → Factures par statut
 * PATCH  /api/facturation/factures/{id}/envoyer           → Marquer comme envoyée
 * PATCH  /api/facturation/factures/{id}/payer             → Marquer comme payée
 * PATCH  /api/facturation/factures/{id}/annuler           → Annuler (libère les interventions)
 * </pre>
 */
@RestController
@RequestMapping("/api/facturation")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;

    @GetMapping("/a-facturer")
    public ResponseEntity<List<InterventionAFacturerResponse>> listerInterventionsAFacturer() {
        return ResponseEntity.ok(factureService.listerInterventionsAFacturer());
    }

    @GetMapping("/a-facturer/client/{clientId}")
    public ResponseEntity<List<InterventionAFacturerResponse>> listerInterventionsAFacturerParClient(
            @PathVariable Integer clientId) {
        return ResponseEntity.ok(factureService.listerInterventionsAFacturerParClient(clientId));
    }

    @PostMapping("/factures")
    public ResponseEntity<FactureResponse> genererFacture(@Valid @RequestBody FactureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(factureService.genererFacture(request));
    }

    @GetMapping("/factures")
    public ResponseEntity<List<FactureResponse>> listerTout() {
        return ResponseEntity.ok(factureService.listerTout());
    }

    @GetMapping("/factures/{id}")
    public ResponseEntity<FactureResponse> trouverParId(@PathVariable Integer id) {
        return ResponseEntity.ok(factureService.trouverParId(id));
    }

    @GetMapping("/factures/{id}/pdf")
    public ResponseEntity<byte[]> telechargerPdf(@PathVariable Integer id) {
        byte[] pdf = factureService.genererPdf(id);
        String nomFichier = "facture-" + id + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(nomFichier).build());

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/factures/client/{clientId}")
    public ResponseEntity<List<FactureResponse>> listerParClient(@PathVariable Integer clientId) {
        return ResponseEntity.ok(factureService.listerParClient(clientId));
    }

    @GetMapping("/factures/statut/{statut}")
    public ResponseEntity<List<FactureResponse>> listerParStatut(@PathVariable StatutFacture statut) {
        return ResponseEntity.ok(factureService.listerParStatut(statut));
    }

    @PatchMapping("/factures/{id}/envoyer")
    public ResponseEntity<FactureResponse> marquerEnvoyee(@PathVariable Integer id) {
        return ResponseEntity.ok(factureService.marquerEnvoyee(id));
    }

    @PatchMapping("/factures/{id}/payer")
    public ResponseEntity<FactureResponse> marquerPayee(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, LocalDate> body) {
        LocalDate datePaiement = body != null ? body.get("datePaiement") : null;
        return ResponseEntity.ok(factureService.marquerPayee(id, datePaiement));
    }

    @PatchMapping("/factures/{id}/annuler")
    public ResponseEntity<FactureResponse> annuler(@PathVariable Integer id) {
        return ResponseEntity.ok(factureService.annuler(id));
    }
}
