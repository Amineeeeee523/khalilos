package com.projet.freelencetinder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.projet.freelencetinder.models.Mission.*;

public class MissionDetailViewDTO {

    /* Header */
    private Long id;
    private String titre;
    private Categorie categorie;
    private Statut statut;
    private BigDecimal budget;
    private String devise;
    private LocalDate delaiLivraison;
    private String localisation;
    private boolean urgent;

    /* Aperçu */
    private ModaliteTravail modaliteTravail;
    private Gouvernorat gouvernorat;
    private LocalDate dateDebutSouhaitee;
    private Integer chargeHebdoJours;
    private Integer dureeEstimeeJours;
    private NiveauBrief qualiteBrief;
    private com.projet.freelencetinder.models.Utilisateur.NiveauExperience niveauExperienceMin;
    private Set<String> badges;
    private String freelanceNomComplet;
    private String clientNomComplet;

    /* Policy */
    private ClosurePolicy closurePolicy;
    private boolean closedByClient;
    private boolean closedByFreelancer;
    private BigDecimal contractTotalAmount;

    /* Paiements (mini) */
    private PaymentMiniDTO paiements;

    /* Livrables */
    private List<LivrableLiteDTO> livrables;

    /* Fichiers */
    private List<String> mediaUrls;
    private String videoBriefUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }
    public LocalDate getDelaiLivraison() { return delaiLivraison; }
    public void setDelaiLivraison(LocalDate delaiLivraison) { this.delaiLivraison = delaiLivraison; }
    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }
    public ModaliteTravail getModaliteTravail() { return modaliteTravail; }
    public void setModaliteTravail(ModaliteTravail modaliteTravail) { this.modaliteTravail = modaliteTravail; }
    public Gouvernorat getGouvernorat() { return gouvernorat; }
    public void setGouvernorat(Gouvernorat gouvernorat) { this.gouvernorat = gouvernorat; }
    public LocalDate getDateDebutSouhaitee() { return dateDebutSouhaitee; }
    public void setDateDebutSouhaitee(LocalDate dateDebutSouhaitee) { this.dateDebutSouhaitee = dateDebutSouhaitee; }
    public Integer getChargeHebdoJours() { return chargeHebdoJours; }
    public void setChargeHebdoJours(Integer chargeHebdoJours) { this.chargeHebdoJours = chargeHebdoJours; }
    public Integer getDureeEstimeeJours() { return dureeEstimeeJours; }
    public void setDureeEstimeeJours(Integer dureeEstimeeJours) { this.dureeEstimeeJours = dureeEstimeeJours; }
    public NiveauBrief getQualiteBrief() { return qualiteBrief; }
    public void setQualiteBrief(NiveauBrief qualiteBrief) { this.qualiteBrief = qualiteBrief; }
    public com.projet.freelencetinder.models.Utilisateur.NiveauExperience getNiveauExperienceMin() { return niveauExperienceMin; }
    public void setNiveauExperienceMin(com.projet.freelencetinder.models.Utilisateur.NiveauExperience niveauExperienceMin) { this.niveauExperienceMin = niveauExperienceMin; }
    public Set<String> getBadges() { return badges; }
    public void setBadges(Set<String> badges) { this.badges = badges; }
    public String getFreelanceNomComplet() { return freelanceNomComplet; }
    public void setFreelanceNomComplet(String freelanceNomComplet) { this.freelanceNomComplet = freelanceNomComplet; }
    public String getClientNomComplet() { return clientNomComplet; }
    public void setClientNomComplet(String clientNomComplet) { this.clientNomComplet = clientNomComplet; }
    public ClosurePolicy getClosurePolicy() { return closurePolicy; }
    public void setClosurePolicy(ClosurePolicy closurePolicy) { this.closurePolicy = closurePolicy; }
    public boolean isClosedByClient() { return closedByClient; }
    public void setClosedByClient(boolean closedByClient) { this.closedByClient = closedByClient; }
    public boolean isClosedByFreelancer() { return closedByFreelancer; }
    public void setClosedByFreelancer(boolean closedByFreelancer) { this.closedByFreelancer = closedByFreelancer; }
    public BigDecimal getContractTotalAmount() { return contractTotalAmount; }
    public void setContractTotalAmount(BigDecimal contractTotalAmount) { this.contractTotalAmount = contractTotalAmount; }
    public PaymentMiniDTO getPaiements() { return paiements; }
    public void setPaiements(PaymentMiniDTO paiements) { this.paiements = paiements; }
    public List<LivrableLiteDTO> getLivrables() { return livrables; }
    public void setLivrables(List<LivrableLiteDTO> livrables) { this.livrables = livrables; }
    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }
    public String getVideoBriefUrl() { return videoBriefUrl; }
    public void setVideoBriefUrl(String videoBriefUrl) { this.videoBriefUrl = videoBriefUrl; }
}


