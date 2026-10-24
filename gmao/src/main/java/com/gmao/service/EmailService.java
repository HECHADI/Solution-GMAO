package com.gmao.service;

import com.gmao.entity.Intervention;

public interface EmailService {
    
    void envoyerLienReinitialisation(String destinataire, String nomUtilisateur, String lienReinitialisation);

    /**
     * Envoie le rapport PDF d'intervention clôturée au client.
     *
     * @param intervention L'intervention réalisée
     * @param pdfData Le fichier PDF sous forme de tableau de octets (bytes)
     */
    void envoyerRapportClient(Intervention intervention, byte[] pdfData);
}