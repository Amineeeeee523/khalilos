package com.projet.freelencetinder.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "payment_audit")
public class PaymentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long trancheId;

    @Column(nullable = false, length = 80)
    private String event;

    @Column(length = 400)
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    /* getters / setters */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTrancheId() { return trancheId; }
    public void setTrancheId(Long trancheId) { this.trancheId = trancheId; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
} 