// src/main/java/com/projet/freelencetinder/controller/LivrableController.java
package com.projet.freelencetinder.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.projet.freelencetinder.dto.*;
import com.projet.freelencetinder.models.StatusLivrable;
import com.projet.freelencetinder.servcie.LivrableService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/livrables")
@Validated
public class LivrableController {

    private final LivrableService livrableService;
    private final SimpMessagingTemplate broker;   // WebSocket STOMP

    @Autowired
    public LivrableController(LivrableService livrableService,
                              SimpMessagingTemplate broker) {
        this.livrableService = livrableService;
        this.broker          = broker;
    }

    /* ---------------------------------------------------------- */
    /* 1. Upload d’un livrable (multipart) par le freelance       */
    /* ---------------------------------------------------------- */
    @PostMapping(
        path = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LivrableDto> upload(
            /* JSON simples (titre, desc, missionId, liensExternes[]) */
            @RequestPart("meta") @Valid CreateLivrableRequest meta,
            /* Fichiers physiques (zip, pdf, png…) */
            @RequestPart(value = "fichiers", required = false) List<MultipartFile> fichiers,
            /* ID du freelancer obtenu depuis le JWT ou Header custom */
            @RequestHeader("X-Freelancer-Id") Long freelancerId) {

        meta.setFichiers(fichiers);
        LivrableDto dto = livrableService.uploadLivrable(meta, freelancerId);

        /* Push temps réel aux abonnés de la mission (client + freelance) */
        broker.convertAndSend("/topic/missions/" + dto.getMissionId(), dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /* ---------------------------------------------------------- */
    /* 2. Listing des livrables pour une mission                  */
    /* ---------------------------------------------------------- */
    @GetMapping("/mission/{missionId}")
    public List<LivrableDto> listMission(
            @PathVariable Long missionId,
            @RequestParam(required = false) StatusLivrable status,
            @RequestParam(defaultValue = "dateEnvoi,desc") String sort) {

        Sort s = Sort.by(sort.split(",")[0])
                     .descending();                // dateEnvoi,desc ou autre
        return livrableService.getLivrablesForMission(missionId, status, s);
    }

    /* ---------------------------------------------------------- */
    /* 3. Listing des livrables pour un freelance                 */
    /* ---------------------------------------------------------- */
    @GetMapping("/freelancer/{freelancerId}")
    public List<LivrableDto> listFreelancer(
            @PathVariable Long freelancerId,
            @RequestParam(defaultValue = "dateEnvoi,desc") String sort) {

        Sort s = Sort.by(sort.split(",")[0]).descending();
        return livrableService.getLivrablesForFreelancer(freelancerId, s);
    }

    /* ---------------------------------------------------------- */
    /* 4. Validation par le client                                */
    /* ---------------------------------------------------------- */
    @PutMapping("/{livrableId}/valider")
    public ResponseEntity<Void> valider(
            @PathVariable Long livrableId,
            @RequestHeader("X-Client-Id") Long clientId) {

        livrableService.validerLivrable(livrableId, clientId);

        /* Push notif */
        broker.convertAndSend("/topic/livrables/" + livrableId, "VALIDATED");
        return ResponseEntity.noContent().build();
    }

    /* ---------------------------------------------------------- */
    /* 5. Rejet par le client                                     */
    /* ---------------------------------------------------------- */
    @PutMapping("/{livrableId}/rejeter")
    public ResponseEntity<Void> rejeter(
            @PathVariable Long livrableId,
            @RequestHeader("X-Client-Id") Long clientId,
            @RequestBody(required = false) String raison) {

        livrableService.rejeterLivrable(livrableId, clientId, raison);

        /* Push notif */
        broker.convertAndSend("/topic/livrables/" + livrableId, "REJECTED");
        return ResponseEntity.noContent().build();
    }
}
