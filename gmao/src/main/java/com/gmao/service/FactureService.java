package com.gmao.service;

import com.gmao.dto.FactureResponse;
import com.gmao.dto.FactureRequest;
import com.gmao.dto.InterventionAFacturerResponse;
import com.gmao.entity.*;
import com.gmao.service.repository.ChantierRepository;
import com.gmao.service.repository.ClientRepository;
import com.gmao.service.repository.FactureRepository;
import com.gmao.service.repository.InterventionRepository;
import com.gmao.service.repository.PieceUtiliseeRepository;
import com.gmao.service.PdfReportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureService {

    private final FactureRepository factureRepository;
    private final ClientRepository clientRepository;
    private final ChantierRepository chantierRepository;
    private final InterventionRepository interventionRepository;
    private final PdfReportService pdfReportService;
    private final PieceUtiliseeRepository pieceUtiliseeRepository;

    @Value("${gmao.facturation.tarif-horaire:45.00}")
    private BigDecimal tarifHoraire;

    @Transactional(readOnly = true)
    public byte[] genererPdf(Integer id) {
        FactureResponse facture = trouverParId(id);
        return pdfReportService.genererFacturePdf(facture);
    }

    @Transactional(readOnly = true)
    public List<InterventionAFacturerResponse> listerInterventionsAFacturer() {
        return interventionRepository.findByStatutAndFactureIsNull(StatutIntervention.REALISE).stream()
                .map(this::construireInterventionAFacturer)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterventionAFacturerResponse> listerInterventionsAFacturerParClient(Integer clientId) {
        return listerInterventionsAFacturer().stream()
                .filter(i -> clientId.equals(i.getClientId()))
                .collect(Collectors.toList());
    }

    private InterventionAFacturerResponse construireInterventionAFacturer(Intervention intervention) {
        BigDecimal coutPieces = calculerCoutPieces(intervention.getId());
        BigDecimal coutMainOeuvre = calculerCoutMainOeuvre(intervention);
        return InterventionAFacturerResponse.of(intervention, coutPieces, coutMainOeuvre);
    }

    private BigDecimal calculerCoutPieces(Integer interventionId) {
        return pieceUtiliseeRepository.findByInterventionId(interventionId).stream()
                .map(pu -> {
                    PieceDetachee piece = pu.getPieceDetachee();
                    if (piece == null || piece.getPrixUnitaire() == null) {
                        return BigDecimal.ZERO;
                    }
                    BigDecimal prix = BigDecimal.valueOf(piece.getPrixUnitaire());
                    BigDecimal qte = BigDecimal.valueOf(pu.getQuantite() != null ? pu.getQuantite() : 0);
                    return prix.multiply(qte);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculerCoutMainOeuvre(Intervention intervention) {
        if (intervention.getDureeEstimee() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal heures = BigDecimal.valueOf(intervention.getDureeEstimee())
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return heures.multiply(tarifHoraire).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public FactureResponse genererFacture(FactureRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable : " + request.getClientId()));

        Chantier chantier = null;
        if (request.getChantierId() != null) {
            chantier = chantierRepository.findById(request.getChantierId())
                    .orElseThrow(() -> new EntityNotFoundException("Chantier introuvable : " + request.getChantierId()));
        }

        List<Intervention> interventions = interventionRepository.findAllById(request.getInterventionIds());
        if (interventions.size() != request.getInterventionIds().size()) {
            throw new EntityNotFoundException("Une ou plusieurs interventions sont introuvables");
        }

        for (Intervention intervention : interventions) {
            if (intervention.getStatut() != StatutIntervention.REALISE) {
                throw new IllegalStateException("L'intervention " + intervention.getId() + " n'est pas clôturée (REALISE)");
            }
            if (intervention.getFacture() != null) {
                throw new IllegalStateException("L'intervention " + intervention.getId() + " est déjà facturée");
            }
        }

        Facture facture = Facture.builder()
                .numero(genererNumero())
                .client(client)
                .chantier(chantier)
                .statut(StatutFacture.BROUILLON)
                .dateEmission(LocalDate.now())
                .dateEcheance(request.getDateEcheance() != null ? request.getDateEcheance() : LocalDate.now().plusDays(30))
                .lignes(new ArrayList<>())
                .build();

        BigDecimal totalPieces = BigDecimal.ZERO;
        BigDecimal totalMainOeuvre = BigDecimal.ZERO;

        for (Intervention intervention : interventions) {
            BigDecimal coutPieces = calculerCoutPieces(intervention.getId());
            BigDecimal coutMainOeuvre = calculerCoutMainOeuvre(intervention);

            FactureLigne ligne = FactureLigne.builder()
                    .facture(facture)
                    .intervention(intervention)
                    .coutPieces(coutPieces)
                    .coutMainOeuvre(coutMainOeuvre)
                    .coutTotal(coutPieces.add(coutMainOeuvre))
                    .build();

            facture.getLignes().add(ligne);
            intervention.setFacture(facture);

            totalPieces = totalPieces.add(coutPieces);
            totalMainOeuvre = totalMainOeuvre.add(coutMainOeuvre);
        }

        facture.setMontantPieces(totalPieces.setScale(2, RoundingMode.HALF_UP));
        facture.setMontantMainOeuvre(totalMainOeuvre.setScale(2, RoundingMode.HALF_UP));
        facture.setMontantTotal(totalPieces.add(totalMainOeuvre).setScale(2, RoundingMode.HALF_UP));

        Facture saved = factureRepository.save(facture);
        interventionRepository.saveAll(interventions);

        log.info("Facture générée : numero={}, client={}, montant={} €",
                saved.getNumero(), client.getId(), saved.getMontantTotal());

        return FactureResponse.from(saved);
    }

    private String genererNumero() {
        int annee = Year.now().getValue();
        String prefixe = "FACT-" + annee + "-";
        long compteur = factureRepository.countByNumeroStartingWith(prefixe) + 1;
        return prefixe + String.format("%04d", compteur);
    }

    @Transactional(readOnly = true)
    public List<FactureResponse> listerTout() {
        return factureRepository.findAllWithLignesBy().stream()
                .map(FactureResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FactureResponse trouverParId(Integer id) {
        return FactureResponse.from(trouverEntiteAvecLignes(id));
    }

    @Transactional(readOnly = true)
    public List<FactureResponse> listerParClient(Integer clientId) {
        return factureRepository.findByClientId(clientId).stream()
                .map(FactureResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FactureResponse> listerParStatut(StatutFacture statut) {
        return factureRepository.findByStatut(statut).stream()
                .map(FactureResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public FactureResponse marquerEnvoyee(Integer id) {
        Facture facture = trouverEntiteParId(id);
        facture.setStatut(StatutFacture.ENVOYEE);
        return FactureResponse.from(factureRepository.save(facture));
    }

    @Transactional
    public FactureResponse marquerPayee(Integer id, LocalDate datePaiement) {
        Facture facture = trouverEntiteParId(id);
        facture.setStatut(StatutFacture.PAYEE);
        facture.setDatePaiement(datePaiement != null ? datePaiement : LocalDate.now());
        return FactureResponse.from(factureRepository.save(facture));
    }

    @Transactional
    public FactureResponse annuler(Integer id) {
        Facture facture = trouverEntiteAvecLignes(id);
        if (facture.getStatut() == StatutFacture.PAYEE) {
            throw new IllegalStateException("Impossible d'annuler une facture déjà payée");
        }

        List<Intervention> interventions = facture.getLignes().stream()
                .map(FactureLigne::getIntervention)
                .toList();
        interventions.forEach(i -> i.setFacture(null));
        interventionRepository.saveAll(interventions);

        facture.setStatut(StatutFacture.ANNULEE);
        return FactureResponse.from(factureRepository.save(facture));
    }

    private Facture trouverEntiteParId(Integer id) {
        return factureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Facture introuvable : " + id));
    }

    private Facture trouverEntiteAvecLignes(Integer id) {
        return factureRepository.findWithLignesById(id)
                .orElseThrow(() -> new EntityNotFoundException("Facture introuvable : " + id));
    }
}