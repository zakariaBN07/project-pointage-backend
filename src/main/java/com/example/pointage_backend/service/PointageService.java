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
                .venteEur(safeDecimal(dto.getVenteEur()))
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

    public List<Pointage> updatePointagesStatus(List<Long> ids, String status, Long managerId) {
        if (ids == null || ids.isEmpty()) return List.of();
        
        List<Pointage> pointages = pointageRepository.findAllById(ids);
        if (managerId != null) {
            // Filter to ensure we only update pointages that belong to THIS manager's team
            List<Long> allowedEmpIds = new ArrayList<>();
            employeeRepository.findByIngenieurId(managerId).forEach(e -> allowedEmpIds.add(e.getId()));
            employeeRepository.findByChargeDAffaireId(managerId).forEach(e -> allowedEmpIds.add(e.getId()));
            
            pointages = pointages.stream()
                .filter(p -> allowedEmpIds.contains(p.getEmployeeId()))
                .toList();
        }

        for (Pointage p : pointages) {
            p.setStatut(status);
            if ("Validé".equalsIgnoreCase(status) || "Valide".equalsIgnoreCase(status)) {
                // p.setValidePar(managerId); // Skipped to avoid FK conflict with non-existent users table
                p.setValideAt(LocalDateTime.now());
            }
        }
        List<Pointage> saved = pointageRepository.saveAll(pointages);

        // Recalculate progress for affected Affaires
        List<Long> affectedAffaireIds = pointages.stream().map(Pointage::getAffaireId).distinct().toList();
        for (Long affId : affectedAffaireIds) {
            affaireRepository.findById(affId).ifPresent(aff -> {
                List<Pointage> affPointages = pointageRepository.findByAffaireIdOrderByDatePointageDesc(affId);
                double validatedHours = affPointages.stream()
                    .filter(pt -> "Validé".equalsIgnoreCase(pt.getStatut()) || "Valide".equalsIgnoreCase(pt.getStatut()))
                    .mapToDouble(pt -> pt.getHeuresTravaillees() != null ? pt.getHeuresTravaillees().doubleValue() : 0.0)
                    .sum();
                
                double estimated = aff.getHeuresEstimees() != null ? aff.getHeuresEstimees().doubleValue() : 0.0;
                if (estimated > 0) {
                    aff.setAffaireProgress((validatedHours / estimated) * 100);
                    affaireRepository.save(aff);
                }
            });
        }
        
        return saved;
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
