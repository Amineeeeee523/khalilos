package com.projet.freelencetinder.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Mission publiée par un client et potentiellement swipée par des freelances.
 */
@Entity
@Table(
    name = "mission",
    indexes = {
        @Index(name = "idx_mission_statut", columnList = "statut"),
        @Index(name = "idx_mission_cat_statut", columnList = "categorie, statut"),
        @Index(name = "idx_mission_date_pub", columnList = "datePublication")
    }
)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Mission {

    /* ===================== Identité & Version ===================== */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    /* ===================== Métadonnées ===================== */
    @NotBlank @Size(max = 180)
    private String titre;

    @NotBlank @Size(max = 4000)
    @Column(length = 4000, nullable = false)
    private String description;

    @ElementCollection
    @CollectionTable(name = "mission_competences_requises", joinColumns = @JoinColumn(name = "mission_id"))
    @Column(name = "competence", length = 120)
    private Set<String> competencesRequises = new HashSet<>();
    
    
    
    /* Mission.java : ajouter */
    @OneToMany(mappedBy = "mission")
    @JsonIgnore           // évite boucle JSON
    private List<TranchePaiement> tranches = new ArrayList<>();
    
 // Mission.java
    @OneToMany(mappedBy = "mission")
    @JsonIgnore
    private List<Livrable> livrables = new ArrayList<>();



    @NotNull
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal budget;

    @Size(max = 10)
    private String devise = "TND";

    @NotNull
    private LocalDate delaiLivraison;

    private Integer dureeEstimeeJours;
    private LocalDate dateLimiteCandidature;

    @Size(max = 160)
    private String localisation;

    @Enumerated(EnumType.STRING)
    private ModaliteTravail modaliteTravail = ModaliteTravail.NON_SPECIFIE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private Statut statut = Statut.EN_ATTENTE;

    /* ===================== Relations ===================== */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelance_selectionne_id")
    private Utilisateur freelanceSelectionne;

   

    /* ===================== Dates / audit ===================== */
    @Column(nullable = false, updatable = false)
    private LocalDateTime datePublication;

    private LocalDateTime dateAffectation;
    private LocalDateTime dateDerniereMiseAJour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Categorie categorie;

    /* ===================== Médias ===================== */
    @ElementCollection
    @CollectionTable(name = "mission_media", joinColumns = @JoinColumn(name = "mission_id"))
    @Column(name = "media_url", length = 600)
    private List<String> mediaUrls = new ArrayList<>();

    @Size(max = 600)
    private String videoBriefUrl;

    /* ===================== Match & Swipes ===================== */
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer swipesRecus = 0;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer likesRecus = 0;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean verrouillee = false;

    @Transient
    private Double scoreMatching;

    /* ===================== Hooks ===================== */
    @PrePersist
    protected void onCreate() {
        this.datePublication = LocalDateTime.now();
        this.dateDerniereMiseAJour = this.datePublication;
        if (budget == null) budget = BigDecimal.ZERO;
        if (devise == null) devise = "TND";
        if (swipesRecus == null) swipesRecus = 0;
        if (likesRecus == null) likesRecus = 0;
        // verrouillee déjà false par défaut
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateDerniereMiseAJour = LocalDateTime.now();
    }

    /* ===================== Méthodes utilitaires ===================== */
    public boolean estExpirée() {
        return dateLimiteCandidature != null &&
               LocalDate.now().isAfter(dateLimiteCandidature);
    }

    public void incrementSwipe() {
        if (swipesRecus == null) swipesRecus = 0;
        swipesRecus++;
    }

    public void incrementLike() {
        if (likesRecus == null) likesRecus = 0;
        likesRecus++;
    }

    public void affecterFreelance(Utilisateur f) {
        this.freelanceSelectionne = f;
        this.dateAffectation = LocalDateTime.now();
        this.statut = Statut.EN_COURS;
        this.verrouillee = true;
    }

    public boolean estDisponiblePourSwipe() {
        return statut == Statut.EN_ATTENTE && !verrouillee && !estExpirée();
    }

    /* ===================== Getters / Setters ===================== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<String> getCompetencesRequises() { return competencesRequises; }
    public void setCompetencesRequises(Set<String> competencesRequises) { this.competencesRequises = competencesRequises; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public LocalDate getDelaiLivraison() { return delaiLivraison; }
    public void setDelaiLivraison(LocalDate delaiLivraison) { this.delaiLivraison = delaiLivraison; }

    public Integer getDureeEstimeeJours() { return dureeEstimeeJours; }
    public void setDureeEstimeeJours(Integer dureeEstimeeJours) { this.dureeEstimeeJours = dureeEstimeeJours; }

    public LocalDate getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(LocalDate dateLimiteCandidature) { this.dateLimiteCandidature = dateLimiteCandidature; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public ModaliteTravail getModaliteTravail() { return modaliteTravail; }
    public void setModaliteTravail(ModaliteTravail modaliteTravail) { this.modaliteTravail = modaliteTravail; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public Utilisateur getClient() { return client; }
    public void setClient(Utilisateur client) { this.client = client; }

    public Utilisateur getFreelanceSelectionne() { return freelanceSelectionne; }
    public void setFreelanceSelectionne(Utilisateur freelanceSelectionne) { this.freelanceSelectionne = freelanceSelectionne; }

  

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public LocalDateTime getDateAffectation() { return dateAffectation; }
    public void setDateAffectation(LocalDateTime dateAffectation) { this.dateAffectation = dateAffectation; }

    public LocalDateTime getDateDerniereMiseAJour() { return dateDerniereMiseAJour; }
    public void setDateDerniereMiseAJour(LocalDateTime dateDerniereMiseAJour) { this.dateDerniereMiseAJour = dateDerniereMiseAJour; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    public String getVideoBriefUrl() { return videoBriefUrl; }
    public void setVideoBriefUrl(String videoBriefUrl) { this.videoBriefUrl = videoBriefUrl; }

    public Integer getSwipesRecus() { return swipesRecus; }
    public void setSwipesRecus(Integer swipesRecus) { this.swipesRecus = swipesRecus; }

    public Integer getLikesRecus() { return likesRecus; }
    public void setLikesRecus(Integer likesRecus) { this.likesRecus = likesRecus; }

    public boolean isVerrouillee() { return verrouillee; }
    public void setVerrouillee(boolean verrouillee) { this.verrouillee = verrouillee; }

    public Double getScoreMatching() { return scoreMatching; }
    public void setScoreMatching(Double scoreMatching) { this.scoreMatching = scoreMatching; }

    /* ===================== Enums ===================== */
    public enum Statut {
        EN_ATTENTE, EN_COURS, EN_ATTENTE_VALIDATION, TERMINEE, ANNULEE, EXPIREE
    }

    public enum Categorie {
        DEVELOPPEMENT_WEB, DEVELOPPEMENT_MOBILE, DESIGN_GRAPHIQUE, REDACTION_CONTENU,
        MARKETING_DIGITAL, VIDEO_MONTAGE, TRADUCTION, SUPPORT_TECHNIQUE, CONSULTING, AUTRE
    }

    public enum ModaliteTravail { DISTANCIEL, PRESENTIEL, HYBRIDE, NON_SPECIFIE }
}
