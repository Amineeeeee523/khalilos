package com.projet.freelencetinder.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projet.freelencetinder.models.WithdrawalMethod;

import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface WithdrawalMethodRepository extends JpaRepository<WithdrawalMethod, Long> {

    List<WithdrawalMethod> findByFreelanceId(Long freelanceId);

    Optional<WithdrawalMethod> findByFreelanceIdAndPrincipalTrue(Long freelanceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from WithdrawalMethod m where m.freelance.id = :freelanceId")
    List<WithdrawalMethod> findByFreelanceIdForUpdate(Long freelanceId);
}