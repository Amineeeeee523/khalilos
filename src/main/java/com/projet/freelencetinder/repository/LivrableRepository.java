package com.projet.freelencetinder.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projet.freelencetinder.models.Livrable;
import com.projet.freelencetinder.models.StatusLivrable;

@Repository
public interface LivrableRepository extends JpaRepository<Livrable, Long> {

    /* Tous les livrables d’une mission (tri configurable) */
    List<Livrable> findByMissionId(Long missionId, Sort sort);

    /* Tous les livrables d’un freelance (facultatif) */
    List<Livrable> findByFreelancerId(Long freelancerId, Sort sort);

    /* Filtrer par statut */
    List<Livrable> findByMissionIdAndStatus(Long missionId, StatusLivrable status, Sort sort);
}
