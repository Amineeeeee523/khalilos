package com.projet.freelencetinder.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Tranche (milestone) de paiement sécurisée par escrow.
 * Une mission peut en comporter plusieurs.
 */
@Entity
@Table(
    name = "tranche_paiement",
    indexes = {
        @Index(name = "idx_tranche_statut", columnList = "statut"),
        @Index(name = "idx_tranche_mission", columnList = "mission_id")
    }
)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TranchePaiement {

    /* ---------- Constantes ---------- */
    /** Commission plateforme (7 %). */
    public static final BigDecimal COMMISSION_RATE = new BigDecimal("0.07");

    /* ---------- Identité ---------- */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    /* ---------- Métadonnées ---------- */
    @NotNull @Min(1)
    private Integer ordre;

    @NotBlank @Size(max = 160)
    private String titre;

    @DecimalMin("0.0")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montantBrut;

    /** Commission calculée (= montantBrut × 7 %). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal commissionPlateforme;

    /** Montant réellement versé au freelance (= montantBrut − commission). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montantNetFreelance;

    @Size(max = 10)
    private String devise = "TND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutTranche statut = StatutTranche.EN_ATTENTE_DEPOT;

    /* ---------- Dates ---------- */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateDepot;        // quand le client a payé
    private LocalDateTime dateValidation;   // quand le client valide le livrable
    private LocalDateTime dateVersement;    // quand l’argent est versé au freelance

    /* ---------- Paymee ---------- */
    @Size(max = 100)
    private String paymeeCheckoutId;   // ID renvoyé par Paymee
    @Size(max = 600)
    private String paymeePaymentUrl;   // lien de paiement pour le client

    /* ---------- Relations ---------- */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelance_id", nullable = false)
    private Utilisateur freelance;

    /* ---------- Hooks ---------- */
    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        calculerCommissions();
    }

    @PreUpdate
    protected void onUpdate() {
        // au cas où le montant évoluerait (rare)
        calculerCommissions();
    }

    private void calculerCommissions() {
        if (montantBrut == null) return;
        commissionPlateforme = montantBrut
                .multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        montantNetFreelance = montantBrut.subtract(commissionPlateforme);
    }

    /* ---------- Méthodes métier ---------- */
    public void marquerDepotEffectue(String paymeeCheckoutId) {
        // La tranche est en attente de paiement ; Paymee confirmera via webhook.
        this.statut = StatutTranche.EN_ATTENTE_PAIEMENT;
        this.dateDepot = LocalDateTime.now();
        this.paymeeCheckoutId = paymeeCheckoutId;
    }

    public void marquerLivrableValide() {
        this.statut = StatutTranche.VALIDEE;
        this.dateValidation = LocalDateTime.now();
    }

    public void marquerVersementEffectue() {
        this.statut = StatutTranche.VERSEE_FREELANCE;
        this.dateVersement = LocalDateTime.now();
    }

    /* ---------- Getters / Setters ---------- */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public BigDecimal getMontantBrut() {
        return montantBrut;
    }

    public void setMontantBrut(BigDecimal montantBrut) {
        this.montantBrut = montantBrut;
    }

    public BigDecimal getCommissionPlateforme() {
        return commissionPlateforme;
    }

    public void setCommissionPlateforme(BigDecimal commissionPlateforme) {
        this.commissionPlateforme = commissionPlateforme;
    }

    public BigDecimal getMontantNetFreelance() {
        return montantNetFreelance;
    }

    public void setMontantNetFreelance(BigDecimal montantNetFreelance) {
        this.montantNetFreelance = montantNetFreelance;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public StatutTranche getStatut() {
        return statut;
    }

    public void setStatut(StatutTranche statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateDepot() {
        return dateDepot;
    }

    public void setDateDepot(LocalDateTime dateDepot) {
        this.dateDepot = dateDepot;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }

    public LocalDateTime getDateVersement() {
        return dateVersement;
    }

    public void setDateVersement(LocalDateTime dateVersement) {
        this.dateVersement = dateVersement;
    }

    public String getPaymeeCheckoutId() {
        return paymeeCheckoutId;
    }

    public void setPaymeeCheckoutId(String paymeeCheckoutId) {
        this.paymeeCheckoutId = paymeeCheckoutId;
    }

    public String getPaymeePaymentUrl() {
        return paymeePaymentUrl;
    }

    public void setPaymeePaymentUrl(String paymeePaymentUrl) {
        this.paymeePaymentUrl = paymeePaymentUrl;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public Utilisateur getClient() {
        return client;
    }

    public void setClient(Utilisateur client) {
        this.client = client;
    }

    public Utilisateur getFreelance() {
        return freelance;
    }

    public void setFreelance(Utilisateur freelance) {
        this.freelance = freelance;
    }

    public enum StatutTranche {
        /** Tranche créée mais le checkout Paymee n'a pas encore été généré. */
        EN_ATTENTE_DEPOT,

        /** Checkout Paymee généré – en attente que le client réalise le paiement. */
        EN_ATTENTE_PAIEMENT,

        /** Paymee a confirmé que les fonds sont bloqués (escrow). */
        FONDS_BLOQUES,

        /** Freelance a livré – attente de validation client. */
        EN_ATTENTE_VALIDATION,

        /** Client valide la livraison. */
        VALIDEE,

        /** Fonds libérés et versés au freelance. */
        VERSEE_FREELANCE,

        /** Client a rejeté la livraison. */
        REJETEE,

        /** Erreur lors de la capture Paymee – en attente de retry. */
        ERREUR_CAPTURE
    }
}
