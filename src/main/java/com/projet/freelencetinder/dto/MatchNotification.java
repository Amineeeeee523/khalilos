package com.projet.freelencetinder.dto;

import java.time.Instant;

/** Payload envoyé sur /user/{id}/queue/matches */
public class MatchNotification {

    private Long   conversationId;
    private Long   missionId;
    private Long   clientId;
    private Long   freelanceId;
    private String missionTitre;
    private String clientNom;
    private String freelanceNom;
    private String clientPhotoUrl;    // AJOUT
    private String freelancePhotoUrl; // AJOUT
    private Instant sentAt = Instant.now();

    public MatchNotification() { }

    public MatchNotification(Long conversationId,
                             Long missionId,
                             Long clientId,
                             Long freelanceId,
                             String missionTitre,
                             String clientNom,
                             String freelanceNom,
                             String clientPhotoUrl,    // AJOUT
                             String freelancePhotoUrl) { // AJOUT
        this.conversationId = conversationId;
        this.missionId      = missionId;
        this.clientId       = clientId;
        this.freelanceId    = freelanceId;
        this.missionTitre   = missionTitre;
        this.clientNom      = clientNom;
        this.freelanceNom   = freelanceNom;
        this.clientPhotoUrl = clientPhotoUrl;         // AJOUT
        this.freelancePhotoUrl = freelancePhotoUrl;   // AJOUT
    }

    /* ---------- getters & setters ---------- */
    public Long    getConversationId() { return conversationId; }
    public void    setConversationId(Long conversationId){ this.conversationId = conversationId; }

    public Long    getMissionId()      { return missionId; }
    public void    setMissionId(Long missionId)           { this.missionId = missionId; }

    public Long    getClientId()       { return clientId; }
    public void    setClientId(Long clientId)             { this.clientId = clientId; }

    public Long    getFreelanceId()    { return freelanceId; }
    public void    setFreelanceId(Long freelanceId)       { this.freelanceId = freelanceId; }

    public String  getMissionTitre()   { return missionTitre; }
    public void    setMissionTitre(String missionTitre)   { this.missionTitre = missionTitre; }

    public String  getClientNom()      { return clientNom; }
    public void    setClientNom(String clientNom)         { this.clientNom = clientNom; }

    public String  getFreelanceNom()   { return freelanceNom; }
    public void    setFreelanceNom(String freelanceNom)   { this.freelanceNom = freelanceNom; }

    public Instant getSentAt()         { return sentAt; }
    public void    setSentAt(Instant sentAt)              { this.sentAt = sentAt; }

    // AJOUT
    public String getClientPhotoUrl() { return clientPhotoUrl; }
    public void setClientPhotoUrl(String clientPhotoUrl) { this.clientPhotoUrl = clientPhotoUrl; }

    // AJOUT
    public String getFreelancePhotoUrl() { return freelancePhotoUrl; }
    public void setFreelancePhotoUrl(String freelancePhotoUrl) { this.freelancePhotoUrl = freelancePhotoUrl; }
}