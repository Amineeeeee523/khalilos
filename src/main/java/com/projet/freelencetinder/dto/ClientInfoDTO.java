package com.projet.freelencetinder.dto;

import com.projet.freelencetinder.models.Utilisateur.TypeClient;

/**
 * Informations publiques du client visibles par un freelance.
 */
public class ClientInfoDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String photoUrl;
    private String ville;

    /* Sous-type du client (PME, Entrepreneur, etc.) */
    private TypeClient typeClient;

    public ClientInfoDTO() {}

    /* ---------- Getters / Setters ---------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public TypeClient getTypeClient() { return typeClient; }
    public void setTypeClient(TypeClient typeClient) { this.typeClient = typeClient; }
}
