package com.projet.freelencetinder.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.models.Utilisateur.Disponibilite;
import com.projet.freelencetinder.models.Utilisateur.Langue;
import com.projet.freelencetinder.models.Utilisateur.NiveauExperience;
import com.projet.freelencetinder.models.Utilisateur.TypeUtilisateur;
import com.projet.freelencetinder.models.Mission.Categorie;
import com.projet.freelencetinder.servcie.UtilisateurService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;

/**
 * Contrôleur de gestion des utilisateurs.
 * Fournit création, listing filtré, mise à jour, patch profil, tokens push,
 * activation, infos “me”, etc.
 */
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @Autowired
    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    /* ================================================================
       1. Création (usage admin ou tests – normalement via /auth/register)
       ================================================================ */
    @PostMapping
    public ResponseEntity<Utilisateur> createUtilisateur(@RequestBody Utilisateur utilisateur) {
        Utilisateur created = utilisateurService.createUtilisateur(utilisateur);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* ================================================================
       2. Listing + filtres simples en mémoire
          (À optimiser plus tard en requêtes JPA + pagination)
       ================================================================ */
    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs(
            @RequestParam(required = false) TypeUtilisateur type,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) String competences,   // CSV
            @RequestParam(required = false) Double tarifMin,
            @RequestParam(required = false) Double tarifMax,
            @RequestParam(required = false) Double tarifJourMin,
            @RequestParam(required = false) Double tarifJourMax,
            @RequestParam(required = false) Disponibilite dispo,
            @RequestParam(required = false) NiveauExperience niveau,
            @RequestParam(required = false) Langue langue,
            @RequestParam(required = false) List<Categorie> categories
    ) {

        List<Utilisateur> liste = utilisateurService.getAllUtilisateurs();

        List<String> skillsFilter = (competences == null || competences.isBlank())
                ? Collections.emptyList()
                : Arrays.stream(competences.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

        List<Utilisateur> filtered = liste.stream()
            .filter(u -> type == null || u.getTypeUtilisateur() == type)
            .filter(u -> localisation == null ||
                    (u.getLocalisation() != null &&
                     u.getLocalisation().toLowerCase().contains(localisation.toLowerCase())))
            .filter(u -> skillsFilter.isEmpty()
                    || (u.getCompetences() != null && u.getCompetences().containsAll(skillsFilter)))
            .filter(u -> categories == null || categories.isEmpty() ||
                    (u.getCategories() != null &&
                        u.getCategories().stream().anyMatch(categories::contains)))
            .filter(u -> tarifMin == null ||
                    (u.getTarifHoraire() != null && u.getTarifHoraire() >= tarifMin))
            .filter(u -> tarifMax == null ||
                    (u.getTarifHoraire() != null && u.getTarifHoraire() <= tarifMax))
            .filter(u -> tarifJourMin == null ||
                    (u.getTarifJournalier() != null && u.getTarifJournalier() >= tarifJourMin))
            .filter(u -> tarifJourMax == null ||
                    (u.getTarifJournalier() != null && u.getTarifJournalier() <= tarifJourMax))
            .filter(u -> dispo  == null || u.getDisponibilite()    == dispo)
            .filter(u -> niveau == null || u.getNiveauExperience() == niveau)
            .filter(u -> langue == null || u.getLanguePref()       == langue)
            .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    /* ================================================================
       3. Lire un utilisateur par ID
       ================================================================ */
    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getUtilisateurById(id));
    }

    /* ================================================================
       4. Mise à jour complète (PUT)
       ================================================================ */
    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> updateUtilisateur(@PathVariable Long id,
                                                         @RequestBody Utilisateur utilisateur) {
        return ResponseEntity.ok(utilisateurService.updateUtilisateur(id, utilisateur));
    }

    /* ================================================================
       5. Suppression
       ================================================================ */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    /* ================================================================
       6. Profil connecté (via SecurityContext)
       ================================================================ */
    @GetMapping("/me")
    public ResponseEntity<Utilisateur> getCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Utilisateur u = utilisateurService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(u);
    }

    /* ================================================================
       7. Patch profil (freelance)
       ================================================================ */
    @PatchMapping("/{id}/profil")
    public ResponseEntity<Utilisateur> patchProfil(@PathVariable Long id,
                                                   @RequestBody PatchProfilRequest req) {
        Utilisateur updated = utilisateurService.patchProfil(
                id,
                req.getBio(),
                req.getLocalisation(),
                req.getCompetences(),
                req.getTarifHoraire(),
                req.getTarifJournalier(),
                req.getCategories(),
                req.getPhotoProfilUrl()
        );
        return ResponseEntity.ok(updated);
    }

    /* ================================================================
       8. Activation / désactivation
       ================================================================ */
    @PostMapping("/{id}/activation")
    public ResponseEntity<Void> setActivation(@PathVariable Long id,
                                              @RequestParam boolean actif) {
        utilisateurService.setActive(id, actif);
        return ResponseEntity.ok().build();
    }

    /* ================================================================
       9. Push tokens (add / remove)
       ================================================================ */
    @PostMapping("/{id}/push-tokens")
    public ResponseEntity<Void> addPushToken(@PathVariable Long id,
                                             @RequestParam @NotBlank String token) {
        utilisateurService.addPushToken(id, token);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/push-tokens")
    public ResponseEntity<Void> removePushToken(@PathVariable Long id,
                                                @RequestParam @NotBlank String token) {
        utilisateurService.removePushToken(id, token);
        return ResponseEntity.noContent().build();
    }

    /* ================================================================
       10. Incrément direct des compteurs (optionnel – usage interne)
       ================================================================ */
    @PostMapping("/{id}/counters/swipe")
    public ResponseEntity<Void> incSwipe(@PathVariable Long id) {
        utilisateurService.incrementSwipe(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/counters/like")
    public ResponseEntity<Void> incLike(@PathVariable Long id) {
        utilisateurService.incrementLikeRecu(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/counters/match")
    public ResponseEntity<Void> incMatch(@PathVariable Long id) {
        utilisateurService.incrementMatch(id);
        return ResponseEntity.ok().build();
    }

    /* ================================================================
       11. Enum metadata (facilite front pour listes)
       ================================================================ */
    @GetMapping("/enums")
    public ResponseEntity<Map<String, Object>> getEnums() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("types", TypeUtilisateur.values());
        map.put("disponibilites", Disponibilite.values());
        map.put("niveauxExperience", NiveauExperience.values());
        map.put("langues", Langue.values());
        map.put("categoriesMission", Categorie.values());
        return ResponseEntity.ok(map);
    }

    /* ================================================================
       Gestion locale des exceptions
       ================================================================ */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /* ================================================================
       DTO interne pour patch du profil
       ================================================================ */
    public static class PatchProfilRequest {
        private String bio;
        private String localisation;
        private Set<String> competences;
        private Double tarifHoraire;
        private Double tarifJournalier;
        private Set<Mission.Categorie> categories;
        private String photoProfilUrl;

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public String getLocalisation() { return localisation; }
        public void setLocalisation(String localisation) { this.localisation = localisation; }

        public Set<String> getCompetences() { return competences; }
        public void setCompetences(Set<String> competences) { this.competences = competences; }

        public Double getTarifHoraire() { return tarifHoraire; }
        public void setTarifHoraire(Double tarifHoraire) { this.tarifHoraire = tarifHoraire; }

        public Double getTarifJournalier() { return tarifJournalier; }
        public void setTarifJournalier(Double tarifJournalier) { this.tarifJournalier = tarifJournalier; }

        public Set<Mission.Categorie> getCategories() { return categories; }
        public void setCategories(Set<Mission.Categorie> categories) { this.categories = categories; }

        public String getPhotoProfilUrl() { return photoProfilUrl; }
        public void setPhotoProfilUrl(String photoProfilUrl) { this.photoProfilUrl = photoProfilUrl; }
    }
}
