package com.gmao.service.impl;

import com.gmao.entity.Intervention;
import com.gmao.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ⚠️ SIMULATION — comme {@code RoutingServiceImpl}, cette implémentation ne
 * fait qu'écrire dans les logs plutôt que d'envoyer un vrai email, en
 * attendant qu'un serveur SMTP soit configuré.
 *
 * Pour brancher un vrai envoi :
 * 1. Ajoute la dépendance spring-boot-starter-mail
 * 2. Configure spring.mail.* dans application.properties (host, port,
 *    username, password d'un compte SMTP — Gmail, SendGrid, Mailgun...)
 * 3. Remplace le contenu de cette classe par un JavaMailSender.send(...)
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void envoyerLienReinitialisation(String destinataire, String nomUtilisateur, String lienReinitialisation) {
        log.info("""

                ════════════════════════════════════════════════════════════
                📧 [SIMULATION EMAIL] Réinitialisation de mot de passe
                ────────────────────────────────────────────────────────────
                À      : {} ({})
                Lien   : {}
                ────────────────────────────────────────────────────────────
                (Aucun email réel envoyé — SMTP non configuré. Voir
                EmailServiceImpl pour brancher un vrai envoi.)
                ════════════════════════════════════════════════════════════
                """, nomUtilisateur, destinataire, lienReinitialisation);
    }
    @Override
public void envoyerRapportClient(Intervention intervention, byte[] pdfData) {
    String clientEmail = null;

    // Récupération de l'e-mail du client lié au site de l'équipement ou du chantier
    if (intervention.getEquipement() != null 
            && intervention.getEquipement().getSite() != null 
            && intervention.getEquipement().getSite().getClient() != null) {
        clientEmail = intervention.getEquipement().getSite().getClient().getEmail();
    } else if (intervention.getChantier() != null 
            && intervention.getChantier().getSite() != null 
            && intervention.getChantier().getSite().getClient() != null) {
        clientEmail = intervention.getChantier().getSite().getClient().getEmail();
    }

    if (clientEmail == null || clientEmail.isBlank()) {
        log.warn("Impossible d'envoyer le rapport PDF : Aucun email client associé à l'intervention ID={}", intervention.getId());
        return;
    }

    // Simulation de l'envoi du rapport PDF par e-mail
    log.info("================================================================================");
    log.info("📧 ENVOI DU RAPPORT PDF CLIENT");
    log.info("Destinataire : {}", clientEmail);
    log.info("Sujet        : Rapport de clôture - Intervention #{}", intervention.getId());
    log.info("Pièce jointe : rapport_intervention_{}.pdf (Taille : {} octets)", intervention.getId(), pdfData != null ? pdfData.length : 0);
    log.info("================================================================================");

    // TODO: En production avec JavaMailSender + MimeMessageHelper :
    /*
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(clientEmail);
        helper.setSubject("Rapport d'intervention #" + intervention.getId() + " - Clôturée");
        helper.setText("Bonjour,\n\nVeuillez trouver ci-joint le rapport détaillé de l'intervention réalisée.\n\nCordialement,");
        
        ByteArrayResource pdfResource = new ByteArrayResource(pdfData);
        helper.addAttachment("Rapport_Intervention_" + intervention.getId() + ".pdf", pdfResource);

        mailSender.send(message);
    } catch (Exception e) {
        log.error("Erreur lors de l'envoi de l'email client", e);
    }
    */
}
}
