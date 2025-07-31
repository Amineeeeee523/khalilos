package com.projet.freelencetinder.models;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(
    name = "client_swipe",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_client_swipe_triplet",
        columnNames = { "client_id", "mission_id", "freelance_id" }
    ),
    indexes = {
        @Index(name = "idx_client_swipe_client", columnList = "client_id"),
        @Index(name = "idx_client_swipe_mission", columnList = "mission_id"),
        @Index(name = "idx_client_swipe_freelance", columnList = "freelance_id"),
        @Index(name = "idx_client_swipe_decision", columnList = "decision")
    }
)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ClientSwipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    /* ============== Relations ============== */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelance_id", nullable = false)
    private Utilisateur freelance;

    /* ============== Données ============== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Swipe.Decision decision;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateSwipe;

    @Column(name = "a_genere_match", nullable = false, columnDefinition = "boolean default false")
    private Boolean aGenereMatch = false;

    private Long dwellTimeMs;

    @PrePersist
    protected void onCreate() {
        this.dateSwipe = LocalDateTime.now();
        if (aGenereMatch == null) aGenereMatch = false;
    }

    /* ============== Getters / Setters ============== */
    public Long getId() { return id; }

    public long getVersion() { return version; }

    public Utilisateur getClient() { return client; }
    public void setClient(Utilisateur client) { this.client = client; }

    public Mission getMission() { return mission; }
    public void setMission(Mission mission) { this.mission = mission; }

    public Utilisateur getFreelance() { return freelance; }
    public void setFreelance(Utilisateur freelance) { this.freelance = freelance; }

    public Swipe.Decision getDecision() { return decision; }
    public void setDecision(Swipe.Decision decision) { this.decision = decision; }

    public LocalDateTime getDateSwipe() { return dateSwipe; }
    public void setDateSwipe(LocalDateTime dateSwipe) { this.dateSwipe = dateSwipe; }

    public Boolean getAGenereMatch() { return aGenereMatch; }
    public void setAGenereMatch(Boolean aGenereMatch) {
        this.aGenereMatch = (aGenereMatch == null ? false : aGenereMatch);
    }

    public Long getDwellTimeMs() { return dwellTimeMs; }
    public void setDwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; }
}
