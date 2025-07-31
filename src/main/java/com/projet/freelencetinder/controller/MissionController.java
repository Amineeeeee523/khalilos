package com.projet.freelencetinder.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.servcie.MissionService;

import jakarta.persistence.EntityNotFoundException;

/**
 * Contrôleur REST focalisé sur la gestion CRUD des missions.
 * Toute la logique de swipe / matching / recommandations est déplacée dans SwipeController.
 */
@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    @Autowired
    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    /* ================================================================
       1. Création d’une mission
       ================================================================ */
    @PostMapping
    public ResponseEntity<Mission> createMission(@RequestBody Mission mission) {
        Mission created = missionService.createMission(mission);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* ================================================================
       2. Liste brute (prototype – à paginer plus tard)
       ================================================================ */
    @GetMapping
    public ResponseEntity<List<Mission>> getAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    /* ================================================================
       3. Détails mission
       ================================================================ */
    @GetMapping("/{id}")
    public ResponseEntity<Mission> getMissionById(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.getMissionById(id));
    }

    /* ================================================================
       4. Mise à jour mission (statut EN_ATTENTE)
       ================================================================ */
    @PutMapping("/{id}")
    public ResponseEntity<Mission> updateMission(@PathVariable Long id,
                                                 @RequestBody Mission mission) {
        return ResponseEntity.ok(missionService.updateMission(id, mission));
    }

    /* ================================================================
       5. Suppression mission
       ================================================================ */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        missionService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }

    /* ================================================================
       6. Missions d’un client
       ================================================================ */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<java.util.List<com.projet.freelencetinder.dto.MissionCardDto>> getMissionsByClient(@PathVariable Long clientId) {
        java.util.List<Mission> missions = missionService.getMissionsByClient(clientId);
        java.util.List<com.projet.freelencetinder.dto.MissionCardDto> dtos = missions.stream().map(this::toCardDto).toList();
        return ResponseEntity.ok(dtos);
    }

    private com.projet.freelencetinder.dto.MissionCardDto toCardDto(Mission m) {
        com.projet.freelencetinder.dto.MissionCardDto d = new com.projet.freelencetinder.dto.MissionCardDto();
        d.setId(m.getId());
        d.setTitre(m.getTitre());
        d.setDescription(m.getDescription());
        d.setBudget(m.getBudget());
        d.setDevise(m.getDevise());
        d.setStatut(m.getStatut());
        if (m.getFreelanceSelectionne() != null) {
            com.projet.freelencetinder.dto.FreelancerSummaryDto f = new com.projet.freelencetinder.dto.FreelancerSummaryDto(
                    m.getFreelanceSelectionne().getId(),
                    m.getFreelanceSelectionne().getNom(),
                    m.getFreelanceSelectionne().getPrenom(),
                    m.getFreelanceSelectionne().getPhotoProfilUrl());
            d.setFreelance(f);
        }
        return d;
    }

    /* ================================================================
       7. Missions où un freelance est sélectionné
       ================================================================ */
    @GetMapping("/freelance/{freelanceId}")
    public ResponseEntity<List<Mission>> getMissionsByFreelance(@PathVariable Long freelanceId) {
        return ResponseEntity.ok(missionService.getMissionsByFreelance(freelanceId));
    }

    /* ================================================================
       Gestion d’erreurs locale (optionnel – sinon @ControllerAdvice global)
       ================================================================ */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    
    
    
    
    
    @PutMapping("/{missionId}/assign/{freelanceId}")
    public ResponseEntity<Mission> assignMission(
            @PathVariable Long missionId,
            @PathVariable Long freelanceId) {
        Mission assigned = missionService.assignMissionToFreelancer(missionId, freelanceId);
        return ResponseEntity.ok(assigned);
    }

}
