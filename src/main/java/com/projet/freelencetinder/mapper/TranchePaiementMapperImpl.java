package com.projet.freelencetinder.mapper;

import org.springframework.stereotype.Component;
import com.projet.freelencetinder.dto.paiement.*;
import com.projet.freelencetinder.models.TranchePaiement;

/**
 * Implémentation manuelle (temporaire) du mapper.
 * Quand MapStruct sera configuré, tu pourras supprimer ce fichier.
 */
@Component                 // <- Spring détecte le bean
public class TranchePaiementMapperImpl implements TranchePaiementMapper {

    /* --------- DTO -> Entity --------- */
    @Override
    public TranchePaiement toEntity(TranchePaiementCreateDTO dto) {
        if (dto == null) return null;

        TranchePaiement entity = new TranchePaiement();
        entity.setOrdre(dto.getOrdre());
        entity.setTitre(dto.getTitre());
        entity.setMontantBrut(dto.getMontantBrut());
        entity.setDevise(dto.getDevise());
        // mission / client / freelance seront posés par le service
        return entity;
    }

    /* --------- Entity -> DTO --------- */
    @Override
    public TranchePaiementResponseDTO toDto(TranchePaiement e) {
        if (e == null) return null;

        TranchePaiementResponseDTO dto = new TranchePaiementResponseDTO();
        dto.setId(e.getId());
        dto.setOrdre(e.getOrdre());
        dto.setTitre(e.getTitre());
        dto.setMontantBrut(e.getMontantBrut());
        dto.setCommissionPlateforme(e.getCommissionPlateforme());
        dto.setMontantNetFreelance(e.getMontantNetFreelance());
        dto.setDevise(e.getDevise());
        dto.setStatut(e.getStatut());
        dto.setDateCreation(e.getDateCreation());
        dto.setDateDepot(e.getDateDepot());
        dto.setDateValidation(e.getDateValidation());
        dto.setDateVersement(e.getDateVersement());
        dto.setPaymeePaymentUrl(e.getPaymeePaymentUrl());
        return dto;
    }
}
