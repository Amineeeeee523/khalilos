package com.projet.freelencetinder.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.projet.freelencetinder.dto.CreateLivrableRequest;
import com.projet.freelencetinder.dto.LivrableDto;
import com.projet.freelencetinder.models.Livrable;
import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.StatusLivrable;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.LivrableRepository;
import com.projet.freelencetinder.repository.MissionRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.servcie.FileStorageService;
import com.projet.freelencetinder.servcie.LivrableService;

@Service
@Transactional
public class LivrableServiceImpl implements LivrableService {

    private final LivrableRepository livrableRepo;
    private final MissionRepository missionRepo;
    private final UtilisateurRepository userRepo;
    private final FileStorageService fileStorage;

    /**
     * Constructeur explicite pour injection des dépendances.
     */
    public LivrableServiceImpl(LivrableRepository livrableRepo,
                               MissionRepository missionRepo,
                               UtilisateurRepository userRepo,
                               FileStorageService fileStorage) {
        this.livrableRepo = livrableRepo;
        this.missionRepo   = missionRepo;
        this.userRepo      = userRepo;
        this.fileStorage   = fileStorage;
    }

    /* ========== Upload & création ========== */
    @Override
    public LivrableDto uploadLivrable(CreateLivrableRequest req, Long freelancerId) {
        Mission mission = missionRepo.findById(req.getMissionId())
            .orElseThrow(() -> new IllegalArgumentException("Mission introuvable"));

        Utilisateur freelance = userRepo.findById(freelancerId)
            .orElseThrow(() -> new IllegalArgumentException("Freelance introuvable"));

        if (!freelance.getId().equals(
                mission.getFreelanceSelectionne() != null
                  ? mission.getFreelanceSelectionne().getId()
                  : null))
            throw new IllegalStateException("Vous n’êtes pas autorisé à livrer sur cette mission");

        Livrable liv = new Livrable();
        liv.setTitre(req.getTitre());
        liv.setDescription(req.getDescription());
        liv.setMission(mission);
        liv.setFreelancer(freelance);

        List<String> chemins = new ArrayList<>();
        if (req.getFichiers() != null) {
            for (MultipartFile f : req.getFichiers()) {
                try {
                    String url = fileStorage.save(f);
                    chemins.add(url);
                } catch (IOException e) {
                    throw new RuntimeException(
                        "Erreur upload fichier : " + f.getOriginalFilename(), e);
                }
            }
        }
        liv.setCheminsFichiers(chemins);
        liv.setLiensExternes(req.getLiensExternes());

        livrableRepo.save(liv);
        return mapToDto(liv);
    }

    /* ========== Listing mission ========== */
    @Override
    @Transactional(readOnly = true)
    public List<LivrableDto> getLivrablesForMission(Long missionId,
                                                    StatusLivrable status,
                                                    Sort sort) {

        List<Livrable> list = (status == null)
            ? livrableRepo.findByMissionId(missionId, sort)
            : livrableRepo.findByMissionIdAndStatus(missionId, status, sort);

        return list.stream()
                   .map(this::mapToDto)
                   .collect(Collectors.toList());
    }

    /* ========== Listing freelance ========== */
    @Override
    @Transactional(readOnly = true)
    public List<LivrableDto> getLivrablesForFreelancer(Long freelancerId,
                                                       Sort sort) {
        return livrableRepo.findByFreelancerId(freelancerId, sort)
                           .stream()
                           .map(this::mapToDto)
                           .collect(Collectors.toList());
    }

    /* ========== Validation / refus ========== */
    @Override
    public void validerLivrable(Long livrableId, Long clientId) {
        Livrable liv = getAndCheckClientRights(livrableId, clientId);
        liv.setStatus(StatusLivrable.VALIDE);
        // Le paiement (EscrowService) déclenchera la mise en TERMINEE
    }

    @Override
    public void rejeterLivrable(Long livrableId, Long clientId, String raison) {
        Livrable liv = getAndCheckClientRights(livrableId, clientId);
        liv.setStatus(StatusLivrable.REJETE);
        // Mission reste EN_COURS pour permettre un nouvel upload éventuel
        if (raison != null) {
            String desc = liv.getDescription() == null ? "" : liv.getDescription() + "\n";
            liv.setDescription(desc + "Motif rejet : " + raison);
        }
    }

    /* ========== Helpers ========== */
    private Livrable getAndCheckClientRights(Long livrableId, Long clientId) {
        Livrable liv = livrableRepo.findById(livrableId)
            .orElseThrow(() -> new IllegalArgumentException("Livrable inconnu"));

        Long ownerId = liv.getMission().getClient().getId();
        if (!ownerId.equals(clientId)) {
            throw new IllegalStateException(
                "Vous n’êtes pas autorisé à valider/rejeter ce livrable");
        }
        return liv;
    }

    private LivrableDto mapToDto(Livrable l) {
        LivrableDto d = new LivrableDto();
        d.setId(l.getId());
        d.setTitre(l.getTitre());
        d.setDescription(l.getDescription());
        d.setDateEnvoi(l.getDateEnvoi());
        d.setStatus(l.getStatus());
        d.setLiensExternes(l.getLiensExternes());
        d.setCheminsFichiers(l.getCheminsFichiers());
        d.setMissionId(l.getMission().getId());
        d.setFreelancerId(l.getFreelancer().getId());
        return d;
    }
}
