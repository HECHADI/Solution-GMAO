package com.gmao.service;

import com.gmao.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Agrège les alertes déjà calculées par les autres modules en un flux unique.
 *
 * <p>Volontairement sans persistance ni notion de "lu/non lu" : recalculé à
 * chaque appel à partir de l'état courant. Si un jour un vrai suivi par
 * utilisateur (lu/non lu, notifications poussées) est nécessaire, il faudra
 * introduire une table dédiée — ce service donne une base saine pour ça.</p>
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final StockService stockService;
    private final PlanMaintenanceService planMaintenanceService;
    private final EquipementService equipementService;
    private final ChantierService chantierService;

    private static final int ORDRE_CRITIQUE = 0;
    private static final int ORDRE_ATTENTION = 1;

    public List<NotificationItem> listerTout() {
        List<NotificationItem> items = new ArrayList<>();

        items.addAll(alertesStockCritique());
        items.addAll(alertesMaintenance());
        items.addAll(alertesEquipementEnPanne());
        items.addAll(alertesChantierRetard());

        items.sort(Comparator.comparingInt(this::ordreGravite));
        return items;
    }

    public NotificationCompteur compteur() {
        List<NotificationItem> items = listerTout();
        long critiques = items.stream().filter(i -> "CRITIQUE".equals(i.getGravite())).count();
        return NotificationCompteur.builder()
                .total(items.size())
                .critique((int) critiques)
                .attention(items.size() - (int) critiques)
                .build();
    }

    // ── Stock critique ───────────────────────────────────────────────────

    private List<NotificationItem> alertesStockCritique() {
        return stockService.listerToutStock().stream()
                .filter(StockVehiculeResponse::isCritique)
                .map(s -> NotificationItem.builder()
                        .id("stock-" + s.getId())
                        .type("STOCK_CRITIQUE")
                        .gravite("CRITIQUE")
                        .titre("Stock critique : " + s.getPieceNom())
                        .message(s.getTechnicienNom() + " — " + s.getQuantite()
                                + " restant(s) (seuil " + s.getSeuilAlerte() + ")")
                        .page("stock")
                        .entiteId(s.getId())
                        .build())
                .toList();
    }

    // ── Maintenance en retard / bientôt due ──────────────────────────────

    private List<NotificationItem> alertesMaintenance() {
        return planMaintenanceService.listerTout().stream()
                .filter(p -> "RETARD".equals(p.getStatutUrgence()) || "BIENTOT".equals(p.getStatutUrgence()))
                .map(p -> {
                    boolean enRetard = "RETARD".equals(p.getStatutUrgence());
                    return NotificationItem.builder()
                            .id("maintenance-" + p.getId())
                            .type(enRetard ? "MAINTENANCE_RETARD" : "MAINTENANCE_BIENTOT")
                            .gravite(enRetard ? "CRITIQUE" : "ATTENTION")
                            .titre((enRetard ? "Maintenance en retard : " : "Maintenance à venir : ") + p.getNom())
                            .message("Échéance le " + p.getProchaineEcheance() + " — " + p.getEquipementNom())
                            .page("maintenance")
                            .entiteId(p.getId())
                            .build();
                })
                .toList();
    }

    // ── Équipements en panne ─────────────────────────────────────────────

    private List<NotificationItem> alertesEquipementEnPanne() {
        return equipementService.listerTout().stream()
                .filter(e -> e.getStatut() != null && "EN_PANNE".equalsIgnoreCase(e.getStatut().name()))
                .map(e -> NotificationItem.builder()
                        .id("equipement-" + e.getId())
                        .type("EQUIPEMENT_PANNE")
                        .gravite("CRITIQUE")
                        .titre("Équipement en panne : " + e.getNom())
                        .message(e.getSiteNom() + (e.getClientNom() != null ? " (" + e.getClientNom() + ")" : ""))
                        .page("equipements")
                        .entiteId(e.getId())
                        .build())
                .toList();
    }

    // ── Chantiers en retard ───────────────────────────────────────────────

    private List<NotificationItem> alertesChantierRetard() {
        LocalDate aujourdHui = LocalDate.now();

        return chantierService.listerTout().stream()
                .filter(c -> c.getDateFinPrevue() != null
                        && c.getDateFinPrevue().isBefore(aujourdHui)
                        && c.getStatut() != null
                        && !"TERMINE".equalsIgnoreCase(c.getStatut())
                        && !"ANNULE".equalsIgnoreCase(c.getStatut()))
                .map(c -> NotificationItem.builder()
                        .id("chantier-" + c.getId())
                        .type("CHANTIER_RETARD")
                        .gravite("ATTENTION")
                        .titre("Chantier en retard : " + c.getNom())
                        .message("Fin prévue le " + c.getDateFinPrevue() + " — avancement " + c.getAvancementPourcentage() + "%")
                        .page("chantier")
                        .entiteId(c.getId())
                        .build())
                .toList();
    }

    private int ordreGravite(NotificationItem item) {
        return "CRITIQUE".equals(item.getGravite()) ? ORDRE_CRITIQUE : ORDRE_ATTENTION;
    }
}
