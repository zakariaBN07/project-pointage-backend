package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.PointageCreateDTO;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.model.Pointage;
import com.example.pointage_backend.repository.AffaireRepository;
import com.example.pointage_backend.repository.EmployeeRepository;
import com.example.pointage_backend.repository.PointageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointageService {

    private final PointageRepository pointageRepository;
    private final EmployeeRepository employeeRepository;
    private final AffaireRepository affaireRepository;

    public Pointage createPointage(PointageCreateDTO dto) {
        validateForeignKeys(dto.getEmployeeId(), dto.getAffaireId());

        Pointage pointage = Pointage.builder()
                .employeeId(dto.getEmployeeId())
                .affaireId(dto.getAffaireId())
                .moisPoste(dto.getMoisPoste())
                .datePointage(dto.getDatePointage())
                .heuresTravaillees(safeDecimal(dto.getHeuresTravaillees()))
                .typeActivite(dto.getTypeActivite())
                .description(dto.getDescription())
                .coutMad(safeDecimal(dto.getCoutMad()))
                .venteMad(safeDecimal(dto.getVenteMad()))
                .deviseAchat(dto.getDeviseAchat())
                .deviseVente(dto.getDeviseVente())
                .coutEur(safeDecimal(dto.getCoutEur()))
                .ventEur(safeDecimal(dto.getVentEur()))
                .statut(dto.getStatut())
                .validePar(dto.getValidePar())
                .valideAt(dto.getValidePar() != null ? LocalDateTime.now() : null)
                .build();

        return pointageRepository.save(pointage);
    }

    public List<Pointage> createPointagesBulk(List<PointageCreateDTO> dtos) {
        List<Pointage> created = new ArrayList<>();
        for (PointageCreateDTO dto : dtos) {
            created.add(createPointage(dto));
        }
        return created;
    }

    public List<Pointage> getPointages(Long affaireId, Long employeeId) {
        if (employeeId != null) {
            return pointageRepository.findByEmployeeIdOrderByDatePointageDesc(employeeId);
        }
        if (affaireId != null) {
            return pointageRepository.findByAffaireIdOrderByDatePointageDesc(affaireId);
        }
        return pointageRepository.findAll();
    }

    public List<Pointage> getPointagesForIngenieur(Long ingenieurId) {
        List<Employee> employees = employeeRepository.findByIngenieurId(ingenieurId);
        if (employees.isEmpty()) {
            return List.of();
        }
        List<Long> employeeIds = employees.stream().map(Employee::getId).toList();
        return pointageRepository.findByEmployeeIdInOrderByDatePointageDesc(employeeIds);
    }

    private void validateForeignKeys(Long employeeId, Long affaireId) {
        if (employeeId == null || !employeeRepository.existsById(employeeId)) {
            throw new IllegalArgumentException("Employee not found: " + employeeId);
        }
        if (affaireId == null || !affaireRepository.existsById(affaireId)) {
            throw new IllegalArgumentException("Affaire not found: " + affaireId);
        }
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
