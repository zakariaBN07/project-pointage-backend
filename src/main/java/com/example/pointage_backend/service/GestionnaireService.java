package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.GestionnaireDTO;
import com.example.pointage_backend.model.Gestionnaire;
import com.example.pointage_backend.repository.GestionnaireRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestionnaireService {
    private static final Logger logger = LoggerFactory.getLogger(GestionnaireService.class);
    private final GestionnaireRepository gestionnaireRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @PostConstruct
    public void migratePasswords() {
        logger.info("Checking for unhashed passwords...");
        List<Gestionnaire> gestionnaires = gestionnaireRepository.findAll();
        int migratedCount = 0;

        for (Gestionnaire g : gestionnaires) {
            String pwd = g.getPassword();
            // BCrypt hashes start with $2a$, $2y$, or $2b$
            if (pwd != null && !pwd.startsWith("$2")) {
                logger.info("Hashing plain text password for user: {}", g.getName());
                g.setPassword(passwordEncoder.encode(pwd));
                gestionnaireRepository.save(g);
                migratedCount++;
            }
        }

        if (migratedCount > 0) {
            logger.info("Successfully migrated {} passwords to BCrypt hashing.", migratedCount);
        } else {
            logger.info("No unhashed passwords found.");
        }
    }

    public List<GestionnaireDTO> getAllGestionnaires() {
        return gestionnaireRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public GestionnaireDTO saveGestionnaire(GestionnaireDTO dto) {
        Gestionnaire gestionnaire;
        String rawPassword = dto.getPassword();
        boolean isNew = dto.getId() == null;

        if (isNew) {
            String encodedPassword = (rawPassword != null && !rawPassword.isEmpty()) 
                    ? passwordEncoder.encode(rawPassword) 
                    : passwordEncoder.encode("defaultPassword"); // Or handle as error
            
            gestionnaire = Gestionnaire.builder()
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .role(dto.getRole())
                    .password(encodedPassword)
                    .siege(dto.getSiege())
                    .build();
        } else {
            gestionnaire = gestionnaireRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Gestionnaire not found"));
            
            gestionnaire.setName(dto.getName());
            gestionnaire.setEmail(dto.getEmail());
            gestionnaire.setRole(dto.getRole());
            gestionnaire.setSiege(dto.getSiege());
            
            if (rawPassword != null && !rawPassword.isEmpty()) {
                gestionnaire.setPassword(passwordEncoder.encode(rawPassword));
            }
        }
        
        Gestionnaire saved = gestionnaireRepository.save(gestionnaire);

        if (isNew && rawPassword != null && saved.getEmail() != null) {
            emailService.sendNewAccountEmail(saved.getEmail(), saved.getName(), rawPassword);
        }
        
        return mapToDTO(saved);
    }

    public void deleteGestionnaire(String id) {
        gestionnaireRepository.deleteById(id);
    }

    public void resetPassword(String id) {
        Gestionnaire gestionnaire = gestionnaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gestionnaire not found"));
        
        // Generate a simple 8-character temporary password
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
        
        gestionnaire.setPassword(passwordEncoder.encode(tempPassword));
        gestionnaireRepository.save(gestionnaire);
        
        if (gestionnaire.getEmail() != null) {
            emailService.sendNewAccountEmail(gestionnaire.getEmail(), gestionnaire.getName(), tempPassword);
        }
    }

    private GestionnaireDTO mapToDTO(Gestionnaire gestionnaire) {
        return GestionnaireDTO.builder()
                .id(gestionnaire.getId())
                .name(gestionnaire.getName())
                .email(gestionnaire.getEmail())
                .role(gestionnaire.getRole())
                .siege(gestionnaire.getSiege())
                .build();
    }
}
