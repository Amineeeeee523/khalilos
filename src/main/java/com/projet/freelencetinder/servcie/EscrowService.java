/* EscrowService.java */
package com.projet.freelencetinder.servcie;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.projet.freelencetinder.client.paymee.PaymeeClient;
import com.projet.freelencetinder.client.paymee.PaymeeClient.PaymeeCheckout;
import com.projet.freelencetinder.dto.paiement.*;
import com.projet.freelencetinder.exception.*;
import com.projet.freelencetinder.mapper.TranchePaiementMapper;
import com.projet.freelencetinder.models.*;
import com.projet.freelencetinder.models.TranchePaiement.StatutTranche;
import com.projet.freelencetinder.repository.*;

@Service
public class EscrowService {

    private final MissionRepository      missionRepo;
    private final UtilisateurRepository  userRepo;
    private final TranchePaiementRepository trancheRepo;
    private final TranchePaiementMapper  mapper;
    private final PaymeeClient           paymee;
    private final PaymentAuditRepository auditRepo;
    private final ApplicationEventPublisher publisher;

    private static final Logger log = LoggerFactory.getLogger(EscrowService.class);

    public EscrowService(MissionRepository missionRepo,
                         UtilisateurRepository userRepo,
                         TranchePaiementRepository trancheRepo,
                         TranchePaiementMapper mapper,
                         PaymeeClient paymee,
                         PaymentAuditRepository auditRepo,
                         ApplicationEventPublisher publisher) {
        this.missionRepo  = missionRepo;
        this.userRepo     = userRepo;
        this.trancheRepo  = trancheRepo;
        this.mapper       = mapper;
        this.paymee       = paymee;
        this.auditRepo    = auditRepo;
        this.publisher    = publisher;
    }

    /* ---------- Création des tranches ---------- */
    @Transactional
    public TranchePaiementResponseDTO createTranche(TranchePaiementCreateDTO dto, Long clientId) {
        Mission mission = missionRepo.findById(dto.getMissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Mission introuvable"));

        if (!mission.getClient().getId().equals(clientId))
            throw new BusinessException("Seul le client propriétaire peut ajouter des tranches");

        // ✅ Vérifie que le freelance est bien affecté à la mission
        if (mission.getFreelanceSelectionne() == null)
            throw new BusinessException("Aucun freelance sélectionné pour cette mission.");

        TranchePaiement tranche = mapper.toEntity(dto);
        tranche.setClient(mission.getClient());
        tranche.setFreelance(mission.getFreelanceSelectionne()); // ← obligatoire !
        tranche.setMission(mission);
        tranche.setMontantBrut(dto.getMontantBrut());
        tranche.setDevise(dto.getDevise());

        trancheRepo.save(tranche);
        logEvent(tranche.getId(), "TRANCHE_CREE", "Ordre=" + tranche.getOrdre());
        return mapper.toDto(tranche);
    }


    /* ---------- Démarrer le paiement (checkout Paymee) ---------- */
    @Transactional
    public TranchePaiementResponseDTO initPaiement(Long trancheId, Long clientId) {
        TranchePaiement t = getAndCheck(trancheId, clientId, StatutTranche.EN_ATTENTE_DEPOT);

        PaymeeCheckout checkout = paymee.createCheckout(
            t.getMontantBrut(), t.getDevise(), "Tranche#" + t.getId());

        t.marquerDepotEffectue(checkout.id());  // => EN_ATTENTE_PAIEMENT
        t.setPaymeePaymentUrl(checkout.paymentUrl());

        logEvent(t.getId(), "CHECKOUT_GENERE", checkout.paymentUrl());

        return mapper.toDto(t);
    }

    /* ---------- Webhook “PAID” Paymee ---------- */
    @Transactional
    public void handleWebhookPaiement(String checkoutId, String status) {
        TranchePaiement t = trancheRepo.findByPaymeeCheckoutId(checkoutId)
            .orElseThrow(() -> new BusinessException("Checkout inconnu"));

        // Idempotence & validation
        if ("PAID".equalsIgnoreCase(status)) {
            if (t.getStatut() == StatutTranche.EN_ATTENTE_PAIEMENT) {
                t.setStatut(StatutTranche.FONDS_BLOQUES);
                logEvent(t.getId(), "PAIEMENT_PAYEE", "CheckoutId=" + checkoutId);
            }
        }
        else if ("CANCELLED".equalsIgnoreCase(status)) {
            if (t.getStatut() == StatutTranche.EN_ATTENTE_PAIEMENT) {
                t.setStatut(StatutTranche.EN_ATTENTE_DEPOT);
            }
        }
    }

    /* ---------- Validation livrable client ---------- */
    @Transactional
    public TranchePaiementResponseDTO validerLivrable(Long trancheId, Long clientId) {
        TranchePaiement t = getAndCheck(trancheId, clientId, StatutTranche.FONDS_BLOQUES);

        t.marquerLivrableValide();
        logEvent(t.getId(), "LIVRABLE_VALIDE", "");
        trancheRepo.save(t);

        // Déclencher la capture & versement après commit pour ne pas bloquer la txn
        publisher.publishEvent(new CapturePaiementEvent(t.getId()));

        return mapper.toDto(t);
    }

    /* ---------- Helpers ---------- */
    private TranchePaiement getAndCheck(Long id, Long clientId, StatutTranche attendu) {
        TranchePaiement t = trancheRepo.findByIdForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tranche introuvable"));
        if (!t.getClient().getId().equals(clientId))
            throw new BusinessException("Non autorisé");
        if (t.getStatut() != attendu)
            throw new BusinessException("Statut invalide");
        return t;
    }

    /* ---------- Récapitulatif mission ---------- */
    @Transactional(readOnly = true)
    public MissionPaiementSummaryDTO summary(Long missionId, Long userId) {
        Mission m = missionRepo.findById(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Mission introuvable"));

        if (!m.getClient().getId().equals(userId) &&
            (m.getFreelanceSelectionne() == null || !m.getFreelanceSelectionne().getId().equals(userId)))
            throw new BusinessException("Accès refusé");

        List<TranchePaiement> list = trancheRepo.findByMissionIdOrderByOrdreAsc(missionId);

        java.math.BigDecimal totalBrut       = list.stream().map(TranchePaiement::getMontantBrut).reduce(BigDecimal.ZERO, BigDecimal::add);
        java.math.BigDecimal totalComm       = list.stream().map(TranchePaiement::getCommissionPlateforme).reduce(BigDecimal.ZERO, BigDecimal::add);
        java.math.BigDecimal totalNetFreelance = list.stream().map(TranchePaiement::getMontantNetFreelance).reduce(BigDecimal.ZERO, BigDecimal::add);

        MissionPaiementSummaryDTO dto = new MissionPaiementSummaryDTO();
        dto.setMissionId(missionId);
        dto.setTitreMission(m.getTitre());
        dto.setTotalBrut(totalBrut);
        dto.setTotalCommission(totalComm);
        dto.setTotalNetFreelance(totalNetFreelance);
        dto.setTranches(list.stream().map(mapper::toDto).toList());
        return dto;
    }

    /* ---------- Event & handler asynchrone ---------- */

    /** Événement interne déclenché juste après validation livrable. */
    public record CapturePaiementEvent(Long trancheId) {}

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCapturePaiement(CapturePaiementEvent evt) {
        processCapture(evt.trancheId());
    }

    /** Traitement de capture isolé dans une nouvelle transaction. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processCapture(Long trancheId) {
        TranchePaiement t = trancheRepo.findByIdForUpdate(trancheId)
            .orElseThrow(() -> new ResourceNotFoundException("Tranche introuvable (event)"));

        // Déjà versée ?  -> idempotence
        if (t.getStatut() == StatutTranche.VERSEE_FREELANCE) return;

        if (t.getStatut() != StatutTranche.FONDS_BLOQUES &&
            t.getStatut() != StatutTranche.ERREUR_CAPTURE) return;

        // Capture Paymee
        try {
            paymee.transferToFreelance(t.getPaymeeCheckoutId());
        } catch (Exception ex) {
            log.error("Erreur lors de la capture Paymee pour la tranche {}", t.getId(), ex);
            t.setStatut(StatutTranche.ERREUR_CAPTURE);
            trancheRepo.save(t);
            logEvent(t.getId(), "CAPTURE_ERREUR", ex.getMessage());
            return;
        }

        // MAJ statut & dates
        t.marquerVersementEffectue();
        logEvent(t.getId(), "CAPTURE_OK", "");

        // Mise à jour solde freelance
        Utilisateur f = t.getFreelance();
        f.setSoldeEscrow(f.getSoldeEscrow().add(t.getMontantNetFreelance()));
        userRepo.save(f);

        // Mission terminée uniquement si toutes les tranches sont versées
        Mission mission = t.getMission();
        boolean resteTranche = trancheRepo.findByMissionIdOrderByOrdreAsc(mission.getId())
            .stream()
            .anyMatch(tr -> tr.getStatut() != StatutTranche.VERSEE_FREELANCE);
        if (!resteTranche) {
            mission.setStatut(Mission.Statut.TERMINEE);
            missionRepo.save(mission);
            logEvent(t.getId(), "MISSION_TERMINEE", "");
        }
    }

    /* ----------- Retry manuel (scheduled) ----------- */
    @Transactional
    public void retryCapture(Long trancheId) {
        publisher.publishEvent(new CapturePaiementEvent(trancheId));
    }

    /* --------- Audit helper --------- */
    private void logEvent(Long trancheId, String event, String details) {
        com.projet.freelencetinder.models.PaymentAudit a = new com.projet.freelencetinder.models.PaymentAudit();
        a.setTrancheId(trancheId);
        a.setEvent(event);
        a.setDetails(details);
        auditRepo.save(a);
    }
}
