/* ===== TranchePaiementRepository.java ===== */
package com.projet.freelencetinder.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import com.projet.freelencetinder.models.TranchePaiement;
import com.projet.freelencetinder.models.TranchePaiement.StatutTranche;
import jakarta.persistence.LockModeType;

public interface TranchePaiementRepository extends JpaRepository<TranchePaiement, Long> {

    List<TranchePaiement> findByMissionIdOrderByOrdreAsc(Long missionId);

    List<TranchePaiement> findByFreelanceIdAndStatut(Long freelanceId, StatutTranche statut);

    List<TranchePaiement> findByClientIdAndStatut(Long clientId, StatutTranche statut);

    Optional<TranchePaiement> findByPaymeeCheckoutId(String paymeeCheckoutId);

    /* ----- Verrou pessimiste pour éviter les conditions de course ----- */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TranchePaiement t where t.id = :id")
    Optional<TranchePaiement> findByIdForUpdate(Long id);

    List<TranchePaiement> findByStatut(StatutTranche statut);

    @Query("""
        SELECT COALESCE(SUM(t.montantBrut),0) FROM TranchePaiement t
        WHERE t.mission.id = :missionId
    """)
    java.math.BigDecimal sumMontantBrutByMission(Long missionId);

    @Query("""
        SELECT COALESCE(SUM(t.commissionPlateforme),0) FROM TranchePaiement t
        WHERE t.mission.id = :missionId
    """)
    java.math.BigDecimal sumCommissionByMission(Long missionId);
}
