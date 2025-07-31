package com.projet.freelencetinder.dto;

import java.math.BigDecimal;

import com.projet.freelencetinder.models.Mission.Statut;

public class MissionCardDto {
    private Long id;
    private String titre;
    private String description;
    private BigDecimal budget;
    private String devise;
    private Statut statut;
    private FreelancerSummaryDto freelance;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public FreelancerSummaryDto getFreelance() { return freelance; }
    public void setFreelance(FreelancerSummaryDto freelance) { this.freelance = freelance; }
}