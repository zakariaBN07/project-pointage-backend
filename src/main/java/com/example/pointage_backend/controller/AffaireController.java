package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.AffaireMetricsDTO;
import com.example.pointage_backend.model.Affaire;
import com.example.pointage_backend.repository.AffaireRepository;
import com.example.pointage_backend.service.AffaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/affaires")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AffaireController {

    private final AffaireRepository affaireRepository;
    private final AffaireService affaireService;

    @GetMapping
    public List<AffaireMetricsDTO> listAffaires() {
        return affaireService.listAllAffairesWithMetrics();
    }

    @GetMapping("/{id}")
    public Affaire getAffaire(@PathVariable("id") String id) {
        try {
            return affaireService.getAffaire(id);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping("/{id}/metrics")
    public AffaireMetricsDTO getMetrics(@PathVariable("id") String id) {
        try {
            return affaireService.getMetricsForAffaire(id);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Affaire createAffaire(@RequestBody Affaire affaire) {
        System.out.println("DEBUG [AffaireController]: Creating/Retrieving affaire with codeAffaire=" + affaire.getCodeAffaire() + ", chrono=" + affaire.getAffairesCodeAffaireUnique());
        // Prevent duplicate affaires with the same codeAffaire
        if (affaire.getCodeAffaire() != null) {
            Affaire existing = affaireRepository.findByCodeAffaire(affaire.getCodeAffaire()).orElse(null);
            if (existing != null) {
                System.out.println("DEBUG [AffaireController]: Affaire already exists with codeAffaire=" + affaire.getCodeAffaire() + ". Returning existing ID: " + existing.getId());
                return existing;
            }
        }

        // 2. Prevent duplicate affaires with the same Chrono (internal reference)
        if (affaire.getAffairesCodeAffaireUnique() != null) {
            Affaire existing = affaireRepository.findByAffairesCodeAffaireUnique(affaire.getAffairesCodeAffaireUnique()).orElse(null);
            if (existing != null) {
                System.out.println("DEBUG [AffaireController]: Affaire already exists with chrono=" + affaire.getAffairesCodeAffaireUnique() + ". Conflict.");
                throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Ce 'Chrono' (Code Unique) existe déjà.");
            }
        }

        Affaire saved = affaireRepository.save(affaire);
        System.out.println("DEBUG [AffaireController]: Affaire successfully saved with ID: " + saved.getId());
        return saved;
    }

    @PutMapping("/{id}")
    public Affaire updateAffaire(@PathVariable("id") Long id, @RequestBody Affaire affaire) {
        affaire.setId(id);
        return affaireRepository.save(affaire);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAffaire(@PathVariable("id") Long id) {
        affaireRepository.deleteById(id);
    }
}
