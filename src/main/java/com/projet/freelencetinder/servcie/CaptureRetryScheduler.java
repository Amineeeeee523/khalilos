package com.projet.freelencetinder.servcie;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.projet.freelencetinder.models.TranchePaiement;
import com.projet.freelencetinder.models.TranchePaiement.StatutTranche;
import com.projet.freelencetinder.repository.TranchePaiementRepository;

/**
 * Tâches planifiées :
 * 1) retry capture pour les tranches en erreur.
 * 2) rappels / timeouts si aucune action après X jours.
 */
@Component
public class CaptureRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(CaptureRetryScheduler.class);

    private final TranchePaiementRepository repo;
    private final EscrowService escrow;

    public CaptureRetryScheduler(TranchePaiementRepository repo, EscrowService escrow) {
        this.repo   = repo;
        this.escrow = escrow;
    }

    /** Re-essaye la capture toutes les 30 min pour les tranches en erreur. */
    @Scheduled(cron = "0 */30 * * * *")
    public void retryFailedCaptures() {
        List<TranchePaiement> list = repo.findByStatut(StatutTranche.ERREUR_CAPTURE);
        list.forEach(t -> {
            log.info("Retry capture tranche {}", t.getId());
            escrow.retryCapture(t.getId());
        });
    }

    /** Vérifie les expirations et génère des logs (ou notifications) une fois par jour. */
    @Scheduled(cron = "0 0 3 * * *")
    public void checkTimeouts() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // Tranches en attente de paiement > 7j
        repo.findByStatut(StatutTranche.EN_ATTENTE_PAIEMENT).stream()
            .filter(t -> t.getDateDepot() != null && t.getDateDepot().isBefore(sevenDaysAgo))
            .forEach(t -> log.warn("Tranche {} en attente de paiement depuis plus de 7 jours", t.getId()));

        // Tranches fonds bloqués sans validation > 7j
        repo.findByStatut(StatutTranche.FONDS_BLOQUES).stream()
            .filter(t -> t.getDateDepot() != null && t.getDateDepot().isBefore(sevenDaysAgo))
            .forEach(t -> log.warn("Tranche {} fonds bloqués sans validation depuis 7 jours", t.getId()));
    }
} 