package com.projet.freelencetinder.dto;

import java.time.LocalDateTime;

import com.projet.freelencetinder.models.Swipe.Decision;

/**
 * Représentation d’un swipe côté freelance.
 */
public class SwipeDTO {

    private Long id;
    private Long missionId;
    private Long freelanceId;
    private Decision decision;
    private LocalDateTime dateSwipe;

    /* Analytics optionnelles */
    private boolean generatedMatch;
    private Long dwellTimeMs;

    public SwipeDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public Long getFreelanceId() { return freelanceId; }
    public void setFreelanceId(Long freelanceId) { this.freelanceId = freelanceId; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public LocalDateTime getDateSwipe() { return dateSwipe; }
    public void setDateSwipe(LocalDateTime dateSwipe) { this.dateSwipe = dateSwipe; }

    public boolean isGeneratedMatch() { return generatedMatch; }
    public void setGeneratedMatch(boolean generatedMatch) { this.generatedMatch = generatedMatch; }

    public Long getDwellTimeMs() { return dwellTimeMs; }
    public void setDwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; }
}
