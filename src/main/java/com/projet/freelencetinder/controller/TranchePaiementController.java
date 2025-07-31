/* TranchePaiementController.java */
package com.projet.freelencetinder.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.dto.paiement.*;
import com.projet.freelencetinder.servcie.EscrowService;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/v1/paiement")
public class TranchePaiementController {

    private final EscrowService escrow;
    private final UtilisateurRepository userRepo;

    public TranchePaiementController(EscrowService escrow,
                                     UtilisateurRepository userRepo) {
        this.escrow = escrow;
        this.userRepo = userRepo;
    }

    /* ------ création d’une tranche ------ */
    @PostMapping("/tranches")
    public ResponseEntity<TranchePaiementResponseDTO> create(
            @Valid @RequestBody TranchePaiementCreateDTO dto) {
        System.out.println("DTO reçu : " + dto);
        Long clientId = 67L; // Utilisateur client existant
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(escrow.createTranche(dto, clientId));
    }


    /* ------ génération lien Paymee ------ */
    /* ------ génération lien Paymee ------ */
    @PostMapping("/tranches/{id}/checkout")
    public TranchePaiementResponseDTO checkout(@PathVariable Long id) {
        Long clientId = 67L; // 🔁 client test sans authentification
        return escrow.initPaiement(id, clientId);
    }

    /* ------ validation livrable ------ */
    @PostMapping("/tranches/{id}/valider")
    
    public TranchePaiementResponseDTO valider(@PathVariable Long id) {
        return escrow.validerLivrable(id, getCurrentUserId());
    }

    /* ------ récap mission ------ */
    @GetMapping("/missions/{id}/summary")
    public MissionPaiementSummaryDTO summary(@PathVariable Long id) {
        return escrow.summary(id, getCurrentUserId());
    }

    /* ----------- helpers ----------- */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepo.findByEmail(email)
            .map(com.projet.freelencetinder.models.Utilisateur::getId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    /* ---- Handler temporaire pour tracer toutes les exceptions ---- */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.badRequest().body("Erreur : " + ex.getMessage());
    }
}
