package com.projet.freelencetinder.servcie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Mission.Statut;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.MissionRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service focalisé sur la gestion "pure" des missions (CRUD & requêtes).
 * Toute la logique de swipe / matching / scoring est déportée dans SwipeService.
 */
@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public MissionService(MissionRepository missionRepository,
                          UtilisateurRepository utilisateurRepository) {
        this.missionRepository     = missionRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /* ------------------------------------------------------------------
       1. Création d’une mission
       ------------------------------------------------------------------ */
    public Mission createMission(Mission mission) {
        if (mission.getClient() == null || mission.getClient().getId() == null) {
            throw new IllegalArgumentException("Client requis pour créer une mission");
        }

        // 🔥 Recharger le client à partir de la base pour qu’il soit "attaché"
        Utilisateur clientAttaché = utilisateurRepository.findById(mission.getClient().getId())
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable (id=" + mission.getClient().getId() + ")"));

        mission.setClient(clientAttaché); // ✅ maintenant c'est un managed entity

        if (mission.getBudget() == null) mission.setBudget(BigDecimal.ZERO);

        return missionRepository.save(mission);
    }

    /* ------------------------------------------------------------------
       2. Listing global (prototype) – à remplacer par pagination
       ------------------------------------------------------------------ */
    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    /* ------------------------------------------------------------------
       3. Détails
       ------------------------------------------------------------------ */
    public Mission getMissionById(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Mission introuvable avec l’id " + id));
    }

    /* ------------------------------------------------------------------
       4. Mise à jour (client propriétaire)
       ------------------------------------------------------------------ */
    @Transactional
    public Mission updateMission(Long id, Mission dto) {
        Mission existing = getMissionById(id);

        if (existing.getStatut() != Statut.EN_ATTENTE) {
            throw new IllegalStateException("La mission ne peut plus être modifiée (statut=" + existing.getStatut() + ")");
        }

        existing.setTitre(dto.getTitre());
        existing.setDescription(dto.getDescription());
        existing.setCompetencesRequises(dto.getCompetencesRequises());
        existing.setBudget(dto.getBudget());
        existing.setDelaiLivraison(dto.getDelaiLivraison());
        existing.setLocalisation(dto.getLocalisation());
        existing.setCategorie(dto.getCategorie());
        existing.setStatut(dto.getStatut());

        /* ---------- nouveaux champs ---------- */
        existing.setDureeEstimeeJours(dto.getDureeEstimeeJours());
        existing.setDateLimiteCandidature(dto.getDateLimiteCandidature());
        existing.setMediaUrls(dto.getMediaUrls());
        existing.setVideoBriefUrl(dto.getVideoBriefUrl());

        return missionRepository.save(existing);
    }

    /* ------------------------------------------------------------------
       5. Suppression
       ------------------------------------------------------------------ */
    @Transactional
    public void deleteMission(Long id) {
        Mission m = getMissionById(id);
        if (m.getStatut() != Statut.EN_ATTENTE && m.getStatut() != Statut.ANNULEE) {
            throw new IllegalStateException("Impossible de supprimer une mission déjà engagée");
        }
        missionRepository.delete(m);
    }

    /* ------------------------------------------------------------------
       9. Missions d’un client
       ------------------------------------------------------------------ */
    public List<Mission> getMissionsByClient(Long clientId) {
        return missionRepository.findAll().stream()
                .filter(m -> m.getClient() != null && m.getClient().getId().equals(clientId))
                .collect(Collectors.toList());
    }

    /* ------------------------------------------------------------------
       10. Missions d’un freelance sélectionné
       ------------------------------------------------------------------ */
    public List<Mission> getMissionsByFreelance(Long freelanceId) {
        return missionRepository.findAll().stream()
                .filter(m -> m.getFreelanceSelectionne() != null
                        && m.getFreelanceSelectionne().getId().equals(freelanceId))
                .collect(Collectors.toList());
    }

    /* ------------------------------------------------------------------
       Utilitaires potentiellement réutilisables
       ------------------------------------------------------------------ */
    public Utilisateur getUtilisateurOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable (id=" + id + ")"));
    }
    
    
    
    
    
    /**
     * Assigne la mission au freelance spécifié et passe la mission en statut EN_COURS.
     * @param missionId    L’ID de la mission à affecter
     * @param freelanceId  L’ID du freelance à qui on l’affecte
     * @return             La mission mise à jour
     */
    @Transactional
    public Mission assignMissionToFreelancer(Long missionId, Long freelanceId) {
        Mission mission = getMissionById(missionId);

        if (mission.getStatut() != Statut.EN_ATTENTE) {
            throw new IllegalStateException(
              "Impossible d’assigner la mission (statut=" + mission.getStatut() + ")");
        }

        Utilisateur freelance = getUtilisateurOrThrow(freelanceId);
        // Optionnel : vérifier que c’est bien un freelance
        // if (freelance.getTypeUtilisateur() != TypeUtilisateur.FREELANCE) { ... }

        mission.setFreelanceSelectionne(freelance);
        mission.setStatut(Statut.EN_COURS);

        return missionRepository.save(mission);
    }
}
