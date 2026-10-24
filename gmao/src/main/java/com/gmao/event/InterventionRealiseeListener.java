package com.gmao.event;

import com.gmao.entity.Intervention;
import com.gmao.service.EmailService;
import com.gmao.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener asynchrone écoutant les clôtures d'intervention.
 * Évite de bloquer la réponse HTTP pendant la génération du PDF et l'envoi d'email.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InterventionRealiseeListener {

    private final PdfReportService pdfReportService;
    private final EmailService emailService;

    @EventListener
    @Async // Permet l'exécution dans un thread séparé (nécessite @EnableAsync sur la classe principale)
    public void handleInterventionRealisee(InterventionRealiseeEvent event) {
        Intervention intervention = event.getIntervention();
        log.info("Événement reçu : Clôture de l'intervention ID={}. Lancement des tâches post-clôture...", intervention.getId());

        try {
            // 1. Génération du PDF
            byte[] pdfData = pdfReportService.genererRapportPdf(intervention);

            // 2. Envoi de l'email au client
            emailService.envoyerRapportClient(intervention, pdfData);

        } catch (Exception e) {
            log.error("Échec lors du traitement post-clôture de l'intervention {}", intervention.getId(), e);
            // En production, on pourrait sauvegarder cet échec dans une table pour un système de "Retry" automatique.
        }
    }
}
