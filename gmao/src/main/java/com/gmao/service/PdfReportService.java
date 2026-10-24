package com.gmao.service;

import com.gmao.dto.FactureResponse;
import com.gmao.dto.FactureLigneResponse;
import com.gmao.entity.Intervention;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Service de génération de PDF utilisant OpenPDF pour les rapports et les factures.
 */
@Service
@Slf4j
public class PdfReportService {

    /**
     * Génère le compte-rendu technique d'une intervention.
     */
    public byte[] genererRapportPdf(Intervention intervention) {
        log.info("Génération du rapport PDF pour l'intervention ID: {}", intervention.getId());
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Titre Principal
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, java.awt.Color.BLUE);
            Paragraph title = new Paragraph("Rapport d'Intervention - GMAO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Sections et contenu général
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            
            document.add(new Paragraph("Informations Générales", sectionFont));
            document.add(new Paragraph("Titre : " + intervention.getTitre(), normalFont));
            document.add(new Paragraph("Description : " + intervention.getDescription(), normalFont));
            document.add(new Paragraph("Statut : " + intervention.getStatut(), normalFont));
            document.add(new Paragraph("Type : " + intervention.getType(), normalFont));
            
            if (intervention.getEquipement() != null) {
                document.add(new Paragraph("Équipement : " + intervention.getEquipement().getNom(), normalFont));
            }

            document.add(new Paragraph("\nValidation et Signature Client", sectionFont));
            if (intervention.getSignatureBase64() != null && !intervention.getSignatureBase64().isEmpty()) {
                try {
                    String base64Image = intervention.getSignatureBase64();
                    if (base64Image.contains(",")) {
                        base64Image = base64Image.split(",")[1];
                    }
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                    Image signatureImage = Image.getInstance(imageBytes);
                    signatureImage.scaleToFit(250, 150);
                    signatureImage.setAlignment(Element.ALIGN_LEFT);
                    document.add(signatureImage);
                } catch (Exception e) {
                    log.error("Erreur lors de l'intégration de la signature dans le PDF", e);
                    document.add(new Paragraph("[Signature invalide ou corrompue]", normalFont));
                }
            } else {
                document.add(new Paragraph("Aucune signature enregistrée.", normalFont));
            }

            document.close();
            return baos.toByteArray();
            
        } catch (DocumentException | IOException e) {
            log.error("Erreur lors de la génération du document PDF", e);
            throw new RuntimeException("Erreur de génération PDF", e);
        }
    }

    /**
     * 🟢 NOUVELLE MÉTHODE : Génère la facture commerciale en PDF.
     * Appelée par FactureService pour le téléchargement client.
     */
    public byte[] genererFacturePdf(FactureResponse facture) {
        log.info("Génération du PDF pour la facture N°: {}", facture.getNumero());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Polices de caractères
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, java.awt.Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.WHITE);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

            // Titre "FACTURE"
            Paragraph title = new Paragraph("FACTURE", titleFont);
            title.setAlignment(Element.ALIGN_RIGHT);
            title.setSpacingAfter(20f);
            document.add(title);

            // Infos Facture & Client (Métadonnées)
            document.add(new Paragraph("Numéro de facture : " + facture.getNumero(), boldFont));
            document.add(new Paragraph("Date d'émission : " + facture.getDateEmission(), normalFont));
            document.add(new Paragraph("Date d'échéance : " + facture.getDateEcheance(), normalFont));
            document.add(new Paragraph("Statut : " + facture.getStatut(), normalFont));
            document.add(new Paragraph("\nFacturé à :", boldFont));
            document.add(new Paragraph("Client : " + facture.getClientNom() + " (ID: " + facture.getClientId() + ")", normalFont));
            if (facture.getChantierNom() != null) {
                document.add(new Paragraph("Projet / Chantier : " + facture.getChantierNom(), normalFont));
            }
            document.add(new Paragraph("\n"));

            // Tableau des lignes de la facture
            PdfPTable table = new PdfPTable(4); // 4 colonnes
            table.setWidthPercentage(100);
            table.setWidths(new float[]{40, 20, 20, 20});

            // Entête du tableau
            String[] headers = {"Intervention / Description", "Coût Pièces", "Coût M.O.", "Total Ligne"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, headerFont));
                cell.setBackgroundColor(java.awt.Color.GRAY);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // Remplissage du tableau avec les interventions facturées
            if (facture.getLignes() != null) {
                for (FactureLigneResponse ligne : facture.getLignes()) {
                    table.addCell(new PdfPCell(new Paragraph(ligne.getInterventionTitre(), normalFont)));
                    table.addCell(new PdfPCell(new Paragraph(ligne.getCoutPieces() + " €", normalFont)));
                    table.addCell(new PdfPCell(new Paragraph(ligne.getCoutMainOeuvre() + " €", normalFont)));
                    table.addCell(new PdfPCell(new Paragraph(ligne.getCoutTotal() + " €", boldFont)));
                }
            }
            document.add(table);
            document.add(new Paragraph("\n"));

            // Section récapitulative des totaux
            Paragraph recap = new Paragraph();
            recap.setAlignment(Element.ALIGN_RIGHT);
            recap.add(new Chunk("Total Pièces : " + facture.getMontantPieces() + " €\n", normalFont));
            recap.add(new Chunk("Total Main d'Œuvre : " + facture.getMontantMainOeuvre() + " €\n", normalFont));
            recap.add(new Chunk("MONTANT TOTAL À PAYER : " + facture.getMontantTotal() + " €\n", titleFont));
            document.add(recap);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException | IOException e) {
            log.error("Erreur lors de la génération du PDF de la facture {}", facture.getNumero(), e);
            throw new RuntimeException("Erreur de génération du PDF de la facture", e);
        }
    }
}