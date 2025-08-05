package com.projet.freelencetinder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projet.freelencetinder.models.WithdrawalRequest;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {

    List<WithdrawalRequest> findByFreelanceIdOrderByDateDemandeDesc(Long freelanceId);
}