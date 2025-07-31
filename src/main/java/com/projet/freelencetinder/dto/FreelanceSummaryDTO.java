package com.projet.freelencetinder.dto;

import java.util.Set;

/**
 * Résumé des informations d’un freelance à afficher dans la carte côté client.
 */
public class FreelanceSummaryDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String photoUrl;
    private String localisation;
    private String niveauExperience;
    private String disponibilite;
    private Double tarifHoraire;
    private Double noteMoyenne;
    private Set<String> competences;
    private String badgePrincipal; // ex. Top Talent, Expert…

    /* ===== Getters / Setters ===== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public String getNiveauExperience() { return niveauExperience; }
    public void setNiveauExperience(String niveauExperience) { this.niveauExperience = niveauExperience; }

    public String getDisponibilite() { return disponibilite; }
    public void setDisponibilite(String disponibilite) { this.disponibilite = disponibilite; }

    public Double getTarifHoraire() { return tarifHoraire; }
    public void setTarifHoraire(Double tarifHoraire) { this.tarifHoraire = tarifHoraire; }

    public Double getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(Double noteMoyenne) { this.noteMoyenne = noteMoyenne; }

    public Set<String> getCompetences() { return competences; }
    public void setCompetences(Set<String> competences) { this.competences = competences; }

    public String getBadgePrincipal() { return badgePrincipal; }
    public void setBadgePrincipal(String badgePrincipal) { this.badgePrincipal = badgePrincipal; }
}
