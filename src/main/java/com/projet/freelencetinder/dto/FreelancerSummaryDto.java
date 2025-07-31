package com.projet.freelencetinder.dto;

public class FreelancerSummaryDto {
    private Long id;
    private String nom;
    private String prenom;
    private String photoProfilUrl;

    public FreelancerSummaryDto() {}
    public FreelancerSummaryDto(Long id, String nom, String prenom, String photoProfilUrl) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.photoProfilUrl = photoProfilUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getPhotoProfilUrl() { return photoProfilUrl; }
    public void setPhotoProfilUrl(String photoProfilUrl) { this.photoProfilUrl = photoProfilUrl; }
}