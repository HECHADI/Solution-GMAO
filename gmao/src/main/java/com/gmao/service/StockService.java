package com.gmao.service;

import com.gmao.dto.*;
import com.gmao.entity.*;
import com.gmao.service.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service central de gestion du stock embarqué.
 *
 * <p>Toute variation de quantité dans {@link StockVehicule} passe par ce
 * service et génère systématiquement une ligne {@link MouvementStock} —
 * c'est la garantie que l'historique reste fidèle à la réalité.</p>
 *
 * <p>NOTE D'INTÉGRATION : quand une intervention est clôturée avec des
 * {@code PieceUtilisee}, il faut appeler {@link #consommer} pour chaque
 * pièce utilisée, afin que le stock du technicien soit décrémenté
 * automatiquement. Voir le snippet fourni séparément.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final PieceDetacheeRepository pieceDetacheeRepository;
    private final StockVehiculeRepository stockVehiculeRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final InterventionRepository interventionRepository;

    // ── Catalogue ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PieceDetacheeResponse> listerCatalogue() {
        return pieceDetacheeRepository.findAll().stream()
                .map(PieceDetacheeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PieceDetacheeResponse modifierSeuilAlerte(Integer pieceId, SeuilAlerteRequest request) {
        PieceDetachee piece = pieceDetacheeRepository.findById(pieceId)
                .orElseThrow(() -> new EntityNotFoundException("Pièce introuvable : " + pieceId));
        piece.setSeuilAlerte(request.getSeuilAlerte());
        return PieceDetacheeResponse.from(pieceDetacheeRepository.save(piece));
    }

    // ── Consultation du stock embarqué ──────────────────────────────────

    @Transactional(readOnly = true)
    public List<StockVehiculeResponse> listerToutStock() {
        return stockVehiculeRepository.findAll().stream()
                .map(StockVehiculeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockVehiculeResponse> listerStockParTechnicien(Integer technicienId) {
        return stockVehiculeRepository.findByTechnicienId(technicienId).stream()
                .map(StockVehiculeResponse::from)
                .collect(Collectors.toList());
    }

    // ── Approvisionnement (dépôt → véhicule) ────────────────────────────

    @Transactional
    public StockVehiculeResponse approvisionner(ApprovisionnementRequest request) {
        Utilisateur technicien = trouverUtilisateur(request.getTechnicienId());
        PieceDetachee piece = trouverPiece(request.getPieceId());

        StockVehicule stock = ajouterQuantite(technicien, piece, request.getQuantite());

        enregistrerMouvement(MouvementStock.builder()
                .technicien(technicien)
                .piece(piece)
                .typeMouvement(TypeMouvementStock.APPROVISIONNEMENT)
                .quantite(request.getQuantite())
                .utilisateur(request.getUtilisateurId() != null ? trouverUtilisateur(request.getUtilisateurId()) : null)
                .commentaire(request.getCommentaire())
                .build());

        return StockVehiculeResponse.from(stock);
    }

    // ── Transfert entre deux techniciens ────────────────────────────────

    @Transactional
    public void transferer(TransfertRequest request) {
        if (request.getTechnicienSourceId().equals(request.getTechnicienDestinationId())) {
            throw new IllegalArgumentException("Le technicien source et destinataire doivent être différents");
        }

        Utilisateur source = trouverUtilisateur(request.getTechnicienSourceId());
        Utilisateur destination = trouverUtilisateur(request.getTechnicienDestinationId());
        PieceDetachee piece = trouverPiece(request.getPieceId());

        StockVehicule stockSource = stockVehiculeRepository
                .findByTechnicienIdAndPieceId(source.getId(), piece.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Le technicien source n'a pas cette pièce en stock"));

        if (stockSource.getQuantite() < request.getQuantite()) {
            throw new IllegalStateException(
                    "Stock insuffisant chez le technicien source (" + stockSource.getQuantite() + " disponible(s))");
        }

        // Retrait chez la source
        stockSource.setQuantite(stockSource.getQuantite() - request.getQuantite());
        stockVehiculeRepository.save(stockSource);
        enregistrerMouvement(MouvementStock.builder()
                .technicien(source)
                .piece(piece)
                .typeMouvement(TypeMouvementStock.TRANSFERT_SORTANT)
                .quantite(request.getQuantite())
                .commentaire(request.getCommentaire())
                .build());

        // Ajout chez la destination
        ajouterQuantite(destination, piece, request.getQuantite());
        enregistrerMouvement(MouvementStock.builder()
                .technicien(destination)
                .piece(piece)
                .typeMouvement(TypeMouvementStock.TRANSFERT_ENTRANT)
                .quantite(request.getQuantite())
                .technicienSource(source)
                .commentaire(request.getCommentaire())
                .build());

        log.info("Transfert de {} x {} : technicien {} → technicien {}",
                request.getQuantite(), piece.getReference(), source.getId(), destination.getId());
    }

    // ── Consommation (appelée à la clôture d'une intervention) ──────────

    /**
     * Décrémente le stock d'un technicien suite à l'utilisation d'une pièce
     * lors d'une intervention. À appeler depuis le service qui persiste les
     * {@code PieceUtilisee} au moment de la clôture.
     */
    @Transactional
    public void consommer(Integer technicienId, Integer pieceId, Integer quantite, Integer interventionId) {
        Utilisateur technicien = trouverUtilisateur(technicienId);
        PieceDetachee piece = trouverPiece(pieceId);

        StockVehicule stock = stockVehiculeRepository
                .findByTechnicienIdAndPieceId(technicienId, pieceId)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucun stock de cette pièce chez ce technicien — impossible de consommer"));

        if (stock.getQuantite() < quantite) {
            log.warn("Consommation de {} x {} demandée mais seulement {} en stock chez le technicien {} — stock négatif évité, mis à 0",
                    quantite, piece.getReference(), stock.getQuantite(), technicienId);
        }

        stock.setQuantite(Math.max(0, stock.getQuantite() - quantite));
        stockVehiculeRepository.save(stock);

        Intervention intervention = interventionId != null
                ? interventionRepository.findById(interventionId).orElse(null)
                : null;

        enregistrerMouvement(MouvementStock.builder()
                .technicien(technicien)
                .piece(piece)
                .typeMouvement(TypeMouvementStock.CONSOMMATION)
                .quantite(quantite)
                .intervention(intervention)
                .build());
    }

    // ── Ajustement manuel (correction suite à inventaire physique) ──────

    @Transactional
    public StockVehiculeResponse ajuster(AjustementRequest request) {
        Utilisateur technicien = trouverUtilisateur(request.getTechnicienId());
        PieceDetachee piece = trouverPiece(request.getPieceId());

        StockVehicule stock = stockVehiculeRepository
                .findByTechnicienIdAndPieceId(request.getTechnicienId(), request.getPieceId())
                .orElseGet(() -> stockVehiculeRepository.save(
                        StockVehicule.builder().technicien(technicien).piece(piece).quantite(0).build()));

        int ancienneQuantite = stock.getQuantite();
        int delta = request.getNouvelleQuantite() - ancienneQuantite;

        stock.setQuantite(request.getNouvelleQuantite());
        stockVehiculeRepository.save(stock);

        if (delta != 0) {
            enregistrerMouvement(MouvementStock.builder()
                    .technicien(technicien)
                    .piece(piece)
                    .typeMouvement(TypeMouvementStock.AJUSTEMENT)
                    .quantite(Math.abs(delta))
                    .commentaire((request.getCommentaire() != null ? request.getCommentaire() + " — " : "")
                            + "Ajustement de " + ancienneQuantite + " à " + request.getNouvelleQuantite())
                    .build());
        }

        return StockVehiculeResponse.from(stock);
    }

    // ── Historique ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MouvementStockResponse> historiqueComplet() {
        return mouvementStockRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(MouvementStockResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MouvementStockResponse> historiqueParPiece(Integer pieceId) {
        return mouvementStockRepository.findByPieceIdOrderByCreatedAtDesc(pieceId).stream()
                .map(MouvementStockResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MouvementStockResponse> historiqueParTechnicien(Integer technicienId) {
        return mouvementStockRepository.findByTechnicienIdOrderByCreatedAtDesc(technicienId).stream()
                .map(MouvementStockResponse::from)
                .collect(Collectors.toList());
    }

    // ── Utilitaires internes ─────────────────────────────────────────────

    private StockVehicule ajouterQuantite(Utilisateur technicien, PieceDetachee piece, int quantite) {
        StockVehicule stock = stockVehiculeRepository
                .findByTechnicienIdAndPieceId(technicien.getId(), piece.getId())
                .orElseGet(() -> StockVehicule.builder()
                        .technicien(technicien)
                        .piece(piece)
                        .quantite(0)
                        .build());

        stock.setQuantite(stock.getQuantite() + quantite);
        return stockVehiculeRepository.save(stock);
    }

    private void enregistrerMouvement(MouvementStock mouvement) {
        mouvementStockRepository.save(mouvement);
    }

    private Utilisateur trouverUtilisateur(Integer id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + id));
    }

    private PieceDetachee trouverPiece(Integer id) {
        return pieceDetacheeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pièce introuvable : " + id));
    }
}
