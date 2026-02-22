package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.GestionnaireDTO;
import com.example.pointage_backend.model.Gestionnaire;
import com.example.pointage_backend.repository.GestionnaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestionnaireService {
    private final GestionnaireRepository gestionnaireRepository;

    public List<GestionnaireDTO> getAllGestionnaires() {
        return gestionnaireRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public GestionnaireDTO saveGestionnaire(GestionnaireDTO dto) {
        Gestionnaire gestionnaire = Gestionnaire.builder()
                .id(dto.getId())
                .name(dto.getName())
                .role(dto.getRole())
                .password(dto.getPassword())
                .build();
        
        return mapToDTO(gestionnaireRepository.save(gestionnaire));
    }

    public void deleteGestionnaire(String id) {
        gestionnaireRepository.deleteById(id);
    }

    private GestionnaireDTO mapToDTO(Gestionnaire gestionnaire) {
        return GestionnaireDTO.builder()
                .id(gestionnaire.getId())
                .name(gestionnaire.getName())
                .role(gestionnaire.getRole())
                .password(gestionnaire.getPassword())
                .build();
    }
}
