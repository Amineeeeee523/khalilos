package com.projet.freelencetinder.servcie;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.models.Utilisateur.TypeUtilisateur;
import com.projet.freelencetinder.repository.UtilisateurRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service de gestion des utilisateurs (freelances / clients / admin).
 * Comprend validation renforcée, mise à jour partielle, gestion tokens,
 * compteurs de swipes / likes / matches et sécurité de base.
 */
@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    /* Regex simples (adapter si besoin international) */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");

    @Autowired
    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /* =========================================================
       1. Création
       ========================================================= */
    @Transactional
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        validateUtilisateur(utilisateur, true);

        utilisateur.setDateCreation(LocalDateTime.now());
        utilisateur.setDerniereMiseAJour(LocalDateTime.now());
        utilisateur.setEstActif(true);

        // Initialisations sûres si null
        if (utilisateur.getSoldeEscrow() == null) utilisateur.setSoldeEscrow(BigDecimal.ZERO);
        if (utilisateur.getNombreSwipes() == null) utilisateur.setNombreSwipes(0);
        if (utilisateur.getLikesRecus() == null) utilisateur.setLikesRecus(0);
        if (utilisateur.getMatchesObtenus() == null) utilisateur.setMatchesObtenus(0);
        normalizeCollections(utilisateur);

        return utilisateurRepository.save(utilisateur);
    }

    /* =========================================================
       2. Lecture
       ========================================================= */
    @Transactional(readOnly = true)
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Utilisateur getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable avec l'id " + id));
    }

    @Transactional(readOnly = true)
    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable pour " + email));
    }

    /* =========================================================
       3. Mise à jour (remplacement “contrôlé”)
       ========================================================= */
    @Transactional
    public Utilisateur updateUtilisateur(Long id, Utilisateur payload) {
        Utilisateur existing = getUtilisateurById(id);

        // On valide ce qui est modifiable (email modifiable ici — tu peux le verrouiller si besoin)
        validateUtilisateur(payload, false);

        // Champs communs (éviter d'écraser des compteurs sensibles)
        existing.setNom(payload.getNom());
        existing.setPrenom(payload.getPrenom());

        // Autoriser le changement d'email *si* différent et non pris
        if (!existing.getEmail().equalsIgnoreCase(payload.getEmail())) {
            if (utilisateurRepository.existsByEmail(payload.getEmail()))
                throw new IllegalArgumentException("Nouvel email déjà utilisé.");
            existing.setEmail(payload.getEmail());
        }

        // Mot de passe : si on te l'envoie vide ou null -> ne pas modifier
        if (StringUtils.hasText(payload.getMotDePasse()) &&
            payload.getMotDePasse().length() >= 60) { // hash probable
            existing.setMotDePasse(payload.getMotDePasse());
        }

        existing.setNumeroTelephone(payload.getNumeroTelephone());
        existing.setPhotoProfilUrl(payload.getPhotoProfilUrl());
        existing.setLanguePref(payload.getLanguePref());

        // Rôle : optionnellement verrouiller. Ici on l'autorise uniquement si identique.
        if (existing.getTypeUtilisateur() != payload.getTypeUtilisateur()) {
            // Si tu veux autoriser un upgrade -> remplace par un check rôle admin
            throw new IllegalStateException("Changement de type utilisateur non autorisé.");
        }

        existing.setDateDerniereConnexion(payload.getDateDerniereConnexion());
        existing.setEstActif(payload.isEstActif());

        // Solde escrow (ne jamais laisser null)
        if (payload.getSoldeEscrow() != null) {
            existing.setSoldeEscrow(payload.getSoldeEscrow());
        }

        // Push tokens (remplacement complet ou merge ? Ici merge)
        if (payload.getPushTokens() != null) {
            existing.getPushTokens().clear();
            existing.getPushTokens().addAll(payload.getPushTokens());
        }

        // Ne pas écraser directement nombreSwipes / likesRecus / matchesObtenus ici
        // (ce sont des compteurs système). Si explicitement envoyés, on peut ignorer.
        // Sauf cas admin -> ajouter un flag si tu veux.

        /* -------- Spécifique FREELANCE -------- */
        if (existing.getTypeUtilisateur() == TypeUtilisateur.FREELANCE) {
            if (payload.getCompetences() != null) existing.setCompetences(payload.getCompetences());
            existing.setTarifHoraire(payload.getTarifHoraire());
            existing.setTarifJournalier(payload.getTarifJournalier());
            existing.setDisponibilite(payload.getDisponibilite());
            existing.setBio(payload.getBio());
            existing.setNiveauExperience(payload.getNiveauExperience());
            existing.setLocalisation(payload.getLocalisation());
            if (payload.getPortfolioUrls() != null) existing.setPortfolioUrls(payload.getPortfolioUrls());
            if (payload.getListeBadges() != null) existing.setListeBadges(payload.getListeBadges());
            existing.setNoteMoyenne(payload.getNoteMoyenne());
            existing.setProjetsTermines(payload.getProjetsTermines());
            if (payload.getCategories() != null) existing.setCategories(payload.getCategories());
        }

        /* -------- Spécifique CLIENT -------- */
        if (existing.getTypeUtilisateur() == TypeUtilisateur.CLIENT) {
            existing.setNomEntreprise(payload.getNomEntreprise());
            existing.setSiteEntreprise(payload.getSiteEntreprise());
            existing.setDescriptionEntreprise(payload.getDescriptionEntreprise());
            existing.setMissionsPubliees(payload.getMissionsPubliees());
            if (payload.getHistoriqueMissions() != null)
                existing.setHistoriqueMissions(payload.getHistoriqueMissions());
            existing.setNoteDonneeMoy(payload.getNoteDonneeMoy());
        }

        existing.setDerniereMiseAJour(LocalDateTime.now());
        return utilisateurRepository.save(existing);
    }

    /* =========================================================
       4. Mise à jour partielle (PATCH profil)
       ========================================================= */
    @Transactional
    public Utilisateur patchProfil(Long id,
                                   String bio,
                                   String localisation,
                                   Set<String> competences,
                                   Double tarifHoraire,
                                   Double tarifJournalier,
                                   Set<Mission.Categorie> categories,
                                   String photoProfilUrl) {

        Utilisateur u = getUtilisateurById(id);

        if (bio != null) u.setBio(bio);
        if (localisation != null) u.setLocalisation(localisation);
        if (photoProfilUrl != null) u.setPhotoProfilUrl(photoProfilUrl);
        if (tarifHoraire != null && tarifHoraire > 0) u.setTarifHoraire(tarifHoraire);
        if (tarifJournalier != null && tarifJournalier > 0) u.setTarifJournalier(tarifJournalier);
        if (competences != null && !competences.isEmpty()) u.setCompetences(competences);
        if (categories != null && !categories.isEmpty()) u.setCategories(categories);

        u.setDerniereMiseAJour(LocalDateTime.now());
        return utilisateurRepository.save(u);
    }

    /* =========================================================
       5. Activation / Désactivation
       ========================================================= */
    @Transactional
    public void setActive(Long id, boolean actif) {
        Utilisateur u = getUtilisateurById(id);
        u.setEstActif(actif);
        u.setDerniereMiseAJour(LocalDateTime.now());
        utilisateurRepository.save(u);
    }

    /* =========================================================
       6. Push tokens
       ========================================================= */
    @Transactional
    public void addPushToken(Long userId, String token) {
        if (!StringUtils.hasText(token)) return;
        Utilisateur u = getUtilisateurById(userId);
        u.getPushTokens().add(token);
        utilisateurRepository.save(u);
    }

    @Transactional
    public void removePushToken(Long userId, String token) {
        Utilisateur u = getUtilisateurById(userId);
        if (u.getPushTokens().remove(token)) {
            utilisateurRepository.save(u);
        }
    }

    /* =========================================================
       7. Suppression
       ========================================================= */
    @Transactional
    public void deleteUtilisateur(Long id) {
        if (!utilisateurRepository.existsById(id))
            throw new EntityNotFoundException("Impossible de supprimer, utilisateur introuvable avec l'id " + id);
        utilisateurRepository.deleteById(id);
    }

    /* =========================================================
       8. Compteurs (utilisés par d’autres services)
       ========================================================= */
    @Transactional
    public void incrementSwipe(Long userId) {
        Utilisateur u = getUtilisateurById(userId);
        u.incrementNombreSwipes();
        utilisateurRepository.save(u);
    }

    @Transactional
    public void incrementLikeRecu(Long freelanceId) {
        Utilisateur u = getUtilisateurById(freelanceId);
        u.incrementLikesRecus();
        utilisateurRepository.save(u);
    }

    @Transactional
    public void incrementMatch(Long userId) {
        Utilisateur u = getUtilisateurById(userId);
        u.incrementMatchesObtenus();
        utilisateurRepository.save(u);
    }

    /* =========================================================
       9. Validation interne
       ========================================================= */
    private void validateUtilisateur(Utilisateur utilisateur, boolean isNew) {

        if (!StringUtils.hasText(utilisateur.getNom()))
            throw new IllegalArgumentException("Le nom est obligatoire.");
        if (!StringUtils.hasText(utilisateur.getPrenom()))
            throw new IllegalArgumentException("Le prénom est obligatoire.");

        if (!StringUtils.hasText(utilisateur.getEmail()) ||
                !EMAIL_PATTERN.matcher(utilisateur.getEmail()).matches())
            throw new IllegalArgumentException("Email invalide.");

        if (isNew && utilisateurRepository.existsByEmail(utilisateur.getEmail()))
            throw new IllegalArgumentException("Email déjà utilisé.");

        // Mot de passe : ici on suppose hash (BCrypt ~60 chars) – ne pas re‑valider longueur stricte si update partiel
        if (isNew) {
            if (!StringUtils.hasText(utilisateur.getMotDePasse()) ||
                    utilisateur.getMotDePasse().length() < 40) // hash attendu (simple garde‑fou)
                throw new IllegalArgumentException("Mot de passe (hash) invalide.");
        }

        if (StringUtils.hasText(utilisateur.getNumeroTelephone()) &&
            !PHONE_PATTERN.matcher(utilisateur.getNumeroTelephone()).matches())
            throw new IllegalArgumentException("Numéro de téléphone invalide.");

        if (utilisateur.getTypeUtilisateur() == null)
            throw new IllegalArgumentException("Le type d'utilisateur est requis.");

        /* -------- FREELANCE -------- */
        if (utilisateur.getTypeUtilisateur() == TypeUtilisateur.FREELANCE) {
            if (utilisateur.getCompetences() == null || utilisateur.getCompetences().isEmpty())
                throw new IllegalArgumentException("Au moins une compétence est requise pour un freelance.");

            if (utilisateur.getTarifHoraire() == null || utilisateur.getTarifHoraire() <= 0)
                throw new IllegalArgumentException("Le tarif horaire doit être positif.");

            if (utilisateur.getTarifJournalier() == null || utilisateur.getTarifJournalier() <= 0)
                throw new IllegalArgumentException("Le tarif journalier doit être positif.");

            if (utilisateur.getDisponibilite() == null)
                throw new IllegalArgumentException("Disponibilité requise.");

            if (utilisateur.getNiveauExperience() == null)
                throw new IllegalArgumentException("Niveau d'expérience requis.");

            if (!StringUtils.hasText(utilisateur.getLocalisation()))
                throw new IllegalArgumentException("Localisation requise.");

            if (utilisateur.getCategories() == null || utilisateur.getCategories().isEmpty())
                throw new IllegalArgumentException("Au moins une catégorie requise.");
        }

        /* -------- CLIENT -------- */
        if (utilisateur.getTypeUtilisateur() == TypeUtilisateur.CLIENT) {
            if (!StringUtils.hasText(utilisateur.getNomEntreprise()))
                throw new IllegalArgumentException("Le nom de l'entreprise est requis.");
        }
    }

    /* =========================================================
       10. Normalisation collections
       ========================================================= */
    private void normalizeCollections(Utilisateur u) {
        if (u.getCompetences() == null) u.setCompetences(new HashSet<>());
        if (u.getListeBadges() == null) u.setListeBadges(new HashSet<>());
        if (u.getPortfolioUrls() == null) u.setPortfolioUrls(new ArrayList<>());
        if (u.getCategories() == null) u.setCategories(new HashSet<>());
        if (u.getPushTokens() == null) u.setPushTokens(new HashSet<>());
        if (u.getHistoriqueMissions() == null) u.setHistoriqueMissions(new ArrayList<>());
    }
}
