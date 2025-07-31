/* TranchePaiementMapper.java */
package com.projet.freelencetinder.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.projet.freelencetinder.dto.paiement.*;
import com.projet.freelencetinder.models.TranchePaiement;

@Mapper(componentModel = "spring")
public interface TranchePaiementMapper {

    TranchePaiement toEntity(TranchePaiementCreateDTO dto);

    @Mapping(source = "montantBrut", target = "montantBrut")
    @Mapping(source = "commissionPlateforme", target = "commissionPlateforme")
    TranchePaiementResponseDTO toDto(TranchePaiement entity);
}
