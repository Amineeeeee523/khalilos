package com.projet.freelencetinder.models;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(
    name = "swipe",
    uniqueConstraints = @UniqueConstraint(name = "uk_swipe_freelance_mission",
        columnNames = { "freelance_id", "mission_id" }),
    indexes = {
        @Index(name = "idx_swipe_freelance", columnList = "freelance_id"),
        @Index(name = "idx_swipe_mission", columnList = "mission_id"),
        @Index(name = "idx_swipe_decision", columnList = "decision")
    }
)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Swipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optimistic locking */
    @Version
    private long version; // primitive => jamais null

    /* ============== Relations ============== */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelance_id", nullable = false)
    private Utilisateur freelance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    /* ============== Données ============== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Decision decision;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateSwipe;

    private Long dwellTimeMs;

    @Column(name = "a_genere_match", nullable = false, columnDefinition = "boolean default false")
    private Boolean aGenereMatch = false;

    @PrePersist
    protected void onCreate() {
        this.dateSwipe = LocalDateTime.now();
        if (aGenereMatch == null) aGenereMatch = false;
    }

    /* ============== Getters / Setters ============== */
    public Long getId() { return id; }

    public long getVersion() { return version; } // pas de setter public

    public Utilisateur getFreelance() { return freelance; }
    public void setFreelance(Utilisateur freelance) { this.freelance = freelance; }

    public Mission getMission() { return mission; }
    public void setMission(Mission mission) { this.mission = mission; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public LocalDateTime getDateSwipe() { return dateSwipe; }
    public void setDateSwipe(LocalDateTime dateSwipe) { this.dateSwipe = dateSwipe; }

    public Long getDwellTimeMs() { return dwellTimeMs; }
    public void setDwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; }

    public Boolean getAGenereMatch() { return aGenereMatch; }
    public void setAGenereMatch(Boolean aGenereMatch) {
        this.aGenereMatch = (aGenereMatch == null ? false : aGenereMatch);
    }

    public enum Decision { LIKE, DISLIKE }
}
