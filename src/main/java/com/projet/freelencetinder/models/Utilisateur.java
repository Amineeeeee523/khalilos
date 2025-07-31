package com.projet.freelencetinder.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Représente un utilisateur (freelance, client ou admin).
 * Optimisé pour le système de swipe/matching.
 */
@Entity
@Table(
    name = "utilisateur",
    indexes = {
        @Index(name = "idx_user_type",      columnList = "typeUtilisateur"),
        @Index(name = "idx_client_subtype", columnList = "typeClient"),
        @Index(name = "idx_user_email",     columnList = "email"),
        @Index(name = "idx_user_localisation", columnList = "localisation")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_utilisateur_email", columnNames = "email")
    }
)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Utilisateur {

    /* ===================== Identité & Version ===================== */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optimistic locking pour éviter écrasement concurrent. */
    @Version
    @Column(nullable = false)
    private Long version;

    /* ===================== Informations personnelles ===================== */
    @NotBlank @Size(max = 80)  private String nom;
    @NotBlank @Size(max = 80)  private String prenom;

    /* ===================== Authentification ===================== */
    @Email @NotBlank @Size(max = 160)
    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank @Size(min = 60, max = 100)
    @Column(nullable = false)
    private String motDePasse;

    /* ===================== Contact & profil ===================== */
    @Size(max = 30)   private String numeroTelephone;
    @Size(max = 500)  private String photoProfilUrl;

    /* ===================== Type d’utilisateur ===================== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TypeUtilisateur typeUtilisateur;

    /* Sous-catégorisation pour un client */
    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private TypeClient typeClient; // null sauf si CLIENT

    /* ===================== Audit ===================== */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    private LocalDateTime dateDerniereConnexion;
    @Column(nullable = false) private boolean estActif = true;
    private LocalDateTime derniereMiseAJour;

    /* ===================== Préférences ===================== */
    @Enumerated(EnumType.STRING) private Langue languePref;

    /* ===================== Dimensions FREELANCE ===================== */
    @ElementCollection
    @CollectionTable(name = "utilisateur_competences",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "competence", length = 120)
    private Set<String> competences = new HashSet<>();

    @Positive private Double tarifHoraire;
    @Positive private Double tarifJournalier;
    @Enumerated(EnumType.STRING) private Disponibilite disponibilite;
    @Size(max = 1000) private String bio;
    @Enumerated(EnumType.STRING) private NiveauExperience niveauExperience;
    @Size(max = 160) private String localisation;

    @ElementCollection(targetClass = Mission.Categorie.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "utilisateur_categories",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "categorie", nullable = false, length = 40)
    private Set<Mission.Categorie> categories = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "utilisateur_portfolio",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "url", length = 600)
    private List<String> portfolioUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "utilisateur_badges",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "badge", length = 120)
    private Set<String> listeBadges = new HashSet<>();

    @DecimalMin("0.0") @DecimalMax("5.0") private Double noteMoyenne;
    private Integer projetsTermines;

    /* ===================== Dimensions CLIENT ===================== */
    @Size(max = 200)  private String nomEntreprise;
    @Size(max = 300)  private String siteEntreprise;
    @Size(max = 1000) private String descriptionEntreprise;
    private Integer missionsPubliees;

    @ElementCollection
    @CollectionTable(name = "utilisateur_historique_missions",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "mission", length = 200)
    private List<String> historiqueMissions = new ArrayList<>();

    @DecimalMin("0.0") @DecimalMax("5.0") private Double noteDonneeMoy;

    /* ===================== Finance / Escrow ===================== */
    @Column(precision = 14, scale = 2) private BigDecimal soldeEscrow;

    /* ===================== Swiping & Gamification ===================== */
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer nombreSwipes = 0;
    private LocalDateTime dernierSwipeAt;
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer likesRecus = 0;
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer matchesObtenus = 0;

    /* ===================== Push notifications ===================== */
    @ElementCollection
    @CollectionTable(name = "utilisateur_tokens_push",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "token", length = 300)
    @JsonIgnore
    private Set<String> pushTokens = new HashSet<>();

    /* ===================== Associations ===================== */
    @OneToMany(mappedBy = "client")              @JsonIgnore
    private List<Mission> missionsPublieesList = new ArrayList<>();

    @OneToMany(mappedBy = "freelanceSelectionne") @JsonIgnore
    private List<Mission> missionsEnCours = new ArrayList<>();

    @OneToMany(mappedBy = "client")    @JsonIgnore
    private List<TranchePaiement> tranchesClient = new ArrayList<>();

    @OneToMany(mappedBy = "freelance") @JsonIgnore
    private List<TranchePaiement> tranchesFreelance = new ArrayList<>();

    @OneToMany(mappedBy = "freelancer") @JsonIgnore
    private List<Livrable> livrablesEnvoyes = new ArrayList<>();

    /* ===================== Hooks JPA ===================== */
    @PrePersist
    protected void onCreate() {
        this.dateCreation      = LocalDateTime.now();
        this.derniereMiseAJour = this.dateCreation;
        if (soldeEscrow     == null) soldeEscrow     = BigDecimal.ZERO;
        if (nombreSwipes    == null) nombreSwipes    = 0;
        if (likesRecus      == null) likesRecus      = 0;
        if (matchesObtenus  == null) matchesObtenus  = 0;
    }
    @PreUpdate
    protected void onUpdate() { this.derniereMiseAJour = LocalDateTime.now(); }

    /* ===================== Méthodes utilitaires ===================== */
    public void incrementNombreSwipes()   {
        if (nombreSwipes == null) nombreSwipes = 0;
        nombreSwipes++;
        this.dernierSwipeAt = LocalDateTime.now();
    }
    public void incrementLikesRecus() {
        if (likesRecus == null) likesRecus = 0;
        likesRecus++;
    }
    public void incrementMatchesObtenus() {
        if (matchesObtenus == null) matchesObtenus = 0;
        matchesObtenus++;
    }
    public String getNomComplet() {
        return (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
    }

    /* ===================== Getters & Setters ===================== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getNumeroTelephone() { return numeroTelephone; }
    public void setNumeroTelephone(String numeroTelephone) { this.numeroTelephone = numeroTelephone; }

    public String getPhotoProfilUrl() { return photoProfilUrl; }
    public void setPhotoProfilUrl(String photoProfilUrl) { this.photoProfilUrl = photoProfilUrl; }

    public TypeUtilisateur getTypeUtilisateur() { return typeUtilisateur; }
    public void setTypeUtilisateur(TypeUtilisateur typeUtilisateur) { this.typeUtilisateur = typeUtilisateur; }

    public TypeClient getTypeClient() { return typeClient; }
    public void setTypeClient(TypeClient typeClient) { this.typeClient = typeClient; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateDerniereConnexion() { return dateDerniereConnexion; }
    public void setDateDerniereConnexion(LocalDateTime dateDerniereConnexion) { this.dateDerniereConnexion = dateDerniereConnexion; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }

    public LocalDateTime getDerniereMiseAJour() { return derniereMiseAJour; }
    public void setDerniereMiseAJour(LocalDateTime derniereMiseAJour) { this.derniereMiseAJour = derniereMiseAJour; }

    public Langue getLanguePref() { return languePref; }
    public void setLanguePref(Langue languePref) { this.languePref = languePref; }

    public Set<String> getCompetences() { return competences; }
    public void setCompetences(Set<String> competences) { this.competences = competences; }

    public Double getTarifHoraire() { return tarifHoraire; }
    public void setTarifHoraire(Double tarifHoraire) { this.tarifHoraire = tarifHoraire; }

    public Double getTarifJournalier() { return tarifJournalier; }
    public void setTarifJournalier(Double tarifJournalier) { this.tarifJournalier = tarifJournalier; }

    public Disponibilite getDisponibilite() { return disponibilite; }
    public void setDisponibilite(Disponibilite disponibilite) { this.disponibilite = disponibilite; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public NiveauExperience getNiveauExperience() { return niveauExperience; }
    public void setNiveauExperience(NiveauExperience niveauExperience) { this.niveauExperience = niveauExperience; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public Set<Mission.Categorie> getCategories() { return categories; }
    public void setCategories(Set<Mission.Categorie> categories) { this.categories = categories; }

    public List<String> getPortfolioUrls() { return portfolioUrls; }
    public void setPortfolioUrls(List<String> portfolioUrls) { this.portfolioUrls = portfolioUrls; }

    public Set<String> getListeBadges() { return listeBadges; }
    public void setListeBadges(Set<String> listeBadges) { this.listeBadges = listeBadges; }

    public Double getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(Double noteMoyenne) { this.noteMoyenne = noteMoyenne; }

    public Integer getProjetsTermines() { return projetsTermines; }
    public void setProjetsTermines(Integer projetsTermines) { this.projetsTermines = projetsTermines; }

    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }

    public String getSiteEntreprise() { return siteEntreprise; }
    public void setSiteEntreprise(String siteEntreprise) { this.siteEntreprise = siteEntreprise; }

    public String getDescriptionEntreprise() { return descriptionEntreprise; }
    public void setDescriptionEntreprise(String descriptionEntreprise) { this.descriptionEntreprise = descriptionEntreprise; }

    public Integer getMissionsPubliees() { return missionsPubliees; }
    public void setMissionsPubliees(Integer missionsPubliees) { this.missionsPubliees = missionsPubliees; }

    public List<String> getHistoriqueMissions() { return historiqueMissions; }
    public void setHistoriqueMissions(List<String> historiqueMissions) { this.historiqueMissions = historiqueMissions; }

    public Double getNoteDonneeMoy() { return noteDonneeMoy; }
    public void setNoteDonneeMoy(Double noteDonneeMoy) { this.noteDonneeMoy = noteDonneeMoy; }

    public BigDecimal getSoldeEscrow() { return soldeEscrow; }
    public void setSoldeEscrow(BigDecimal soldeEscrow) { this.soldeEscrow = soldeEscrow; }

    public Integer getNombreSwipes() { return nombreSwipes; }
    public void setNombreSwipes(Integer nombreSwipes) { this.nombreSwipes = nombreSwipes; }

    public LocalDateTime getDernierSwipeAt() { return dernierSwipeAt; }
    public void setDernierSwipeAt(LocalDateTime dernierSwipeAt) { this.dernierSwipeAt = dernierSwipeAt; }

    public Integer getLikesRecus() { return likesRecus; }
    public void setLikesRecus(Integer likesRecus) { this.likesRecus = likesRecus; }

    public Integer getMatchesObtenus() { return matchesObtenus; }
    public void setMatchesObtenus(Integer matchesObtenus) { this.matchesObtenus = matchesObtenus; }

    public Set<String> getPushTokens() { return pushTokens; }
    public void setPushTokens(Set<String> pushTokens) { this.pushTokens = pushTokens; }

    public List<Mission> getMissionsPublieesList() { return missionsPublieesList; }
    public void setMissionsPublieesList(List<Mission> missionsPublieesList) { this.missionsPublieesList = missionsPublieesList; }

    public List<Mission> getMissionsEnCours() { return missionsEnCours; }
    public void setMissionsEnCours(List<Mission> missionsEnCours) { this.missionsEnCours = missionsEnCours; }

    public List<TranchePaiement> getTranchesClient() { return tranchesClient; }
    public void setTranchesClient(List<TranchePaiement> tranchesClient) { this.tranchesClient = tranchesClient; }

    public List<TranchePaiement> getTranchesFreelance() { return tranchesFreelance; }
    public void setTranchesFreelance(List<TranchePaiement> tranchesFreelance) { this.tranchesFreelance = tranchesFreelance; }

    public List<Livrable> getLivrablesEnvoyes() { return livrablesEnvoyes; }
    public void setLivrablesEnvoyes(List<Livrable> livrablesEnvoyes) { this.livrablesEnvoyes = livrablesEnvoyes; }

    /* ===================== Enums ===================== */
    public enum TypeUtilisateur { FREELANCE, CLIENT, ADMIN }

    /** Sous-catégorisation du client */
    public enum TypeClient {
        PME_STARTUP,
        ENTREPRENEUR,
        ETUDIANT_PARTICULIER,
        CLIENT_ETRANGER
    }

    public enum Disponibilite    { TEMPS_PLEIN, TEMPS_PARTIEL, PONCTUEL, INDISPONIBLE }
    public enum NiveauExperience { DEBUTANT, INTERMEDIAIRE, EXPERT }
    public enum Langue           { FR, AR, EN }
}
