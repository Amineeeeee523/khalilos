/* ===== MissionPaiementSummaryDTO.java ===== */
package com.projet.freelencetinder.dto.paiement;

import java.math.BigDecimal;
import java.util.List;

public class MissionPaiementSummaryDTO {

    private Long missionId;
    private String titreMission;
    private BigDecimal totalBrut;
    private BigDecimal totalCommission;
    private BigDecimal totalNetFreelance;
    private List<TranchePaiementResponseDTO> tranches;

    /* ---------- getters / setters ---------- */
    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public String getTitreMission() { return titreMission; }
    public void setTitreMission(String titreMission) { this.titreMission = titreMission; }

    public BigDecimal getTotalBrut() { return totalBrut; }
    public void setTotalBrut(BigDecimal totalBrut) { this.totalBrut = totalBrut; }

    public BigDecimal getTotalCommission() { return totalCommission; }
    public void setTotalCommission(BigDecimal totalCommission) { this.totalCommission = totalCommission; }

    public BigDecimal getTotalNetFreelance() { return totalNetFreelance; }
    public void setTotalNetFreelance(BigDecimal totalNetFreelance) { this.totalNetFreelance = totalNetFreelance; }

    public List<TranchePaiementResponseDTO> getTranches() { return tranches; }
    public void setTranches(List<TranchePaiementResponseDTO> tranches) { this.tranches = tranches; }
}
