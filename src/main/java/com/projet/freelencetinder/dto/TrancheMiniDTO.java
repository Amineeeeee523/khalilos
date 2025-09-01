package com.projet.freelencetinder.dto;

import java.math.BigDecimal;
import com.projet.freelencetinder.models.TranchePaiement.StatutTranche;

public class TrancheMiniDTO {

    private Long id;
    private Integer ordre;
    private String titre;
    private StatutTranche statut;
    private BigDecimal montantBrut;
    private boolean required;
    private boolean finale;
    private String paymentUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public StatutTranche getStatut() { return statut; }
    public void setStatut(StatutTranche statut) { this.statut = statut; }
    public BigDecimal getMontantBrut() { return montantBrut; }
    public void setMontantBrut(BigDecimal montantBrut) { this.montantBrut = montantBrut; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public boolean isFinale() { return finale; }
    public void setFinale(boolean finale) { this.finale = finale; }
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
}


