package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.AffaireMetricsDTO;
import com.example.pointage_backend.model.Affaire;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.model.Pointage;
import com.example.pointage_backend.model.Task;
import com.example.pointage_backend.repository.AffaireRepository;
import com.example.pointage_backend.repository.EmployeeRepository;
import com.example.pointage_backend.repository.PointageRepository;
import com.example.pointage_backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffaireService {
    private final AffaireRepository affaireRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final PointageRepository pointageRepository;

    public AffaireMetricsDTO getMetricsForAffaire(String identifier) {
        return getMetricsForAffaire(identifier, null);
    }

    public AffaireMetricsDTO getMetricsForAffaire(String identifier, Long ingenieurId) {
        Affaire affaire = resolveAffaire(identifier);
        Long affaireId = affaire.getId();

        List<Employee> employees = getEmployeesForAffaire(affaire, ingenieurId);
        List<Long> scopedEmployeeIds = employees.stream()
                .map(Employee::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Pointage> scopedPointages = pointageRepository.findByAffaireIdOrderByDatePointageDesc(affaireId).stream()
                .filter(pointage -> scopedEmployeeIds.isEmpty() || scopedEmployeeIds.contains(pointage.getEmployeeId()))
                .toList();

        BigDecimal planned = ingenieurId != null
                ? resolveScopedPlannedHours(affaire, employees)
                : resolveGlobalPlannedHours(affaire, employees);

        BigDecimal totalConsumed = scopedPointages.stream()
                .map(Pointage::getHeuresTravaillees)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal validatedConsumed = scopedPointages.stream()
                .filter(pointage -> isValidated(pointage.getStatut()))
                .map(Pointage::getHeuresTravaillees)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = planned.subtract(validatedConsumed);
        BigDecimal progress = ingenieurId != null
                ? calculateScopedProgress(planned, validatedConsumed)
                : calculateGlobalProgress(affaire, affaireId);

        BigDecimal timePercent = BigDecimal.ZERO;
        if (planned.compareTo(BigDecimal.ZERO) > 0) {
            timePercent = totalConsumed.multiply(new BigDecimal("100"))
                    .divide(planned, 2, RoundingMode.HALF_UP);
        }

        boolean alert = timePercent.compareTo(progress) > 0;

        List<Long> chargeDAffaireIds = employees.stream()
                .map(Employee::getChargeDAffaireId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return AffaireMetricsDTO.builder()
                .id(affaire.getId())
                .codeAffaire(affaire.getCodeAffaire())
                .nomAffaire(affaire.getNomAffaire())
                .affairesCodeAffaireUnique(affaire.getAffairesCodeAffaireUnique())
                .tiersX3(affaire.getTiersX3())
                .devise(affaire.getDevise())
                .chargeAffaire(affaire.getChargeAffaire())
                .categorie(affaire.getCategorie())
                .dateAffaire(affaire.getDateAffaire())
                .statut(affaire.getStatut())
                .description(affaire.getDescription())
                .createdAt(affaire.getCreatedAt())
                .updatedAt(affaire.getUpdatedAt())
                .heuresEstimees(affaire.getHeuresEstimees())
                .chargeDAffaireIds(chargeDAffaireIds)
                .plannedHours(planned)
                .consumedHours(totalConsumed)
                .remainingHours(remaining)
                .progressPercent(progress)
                .timePercent(timePercent)
                .isMonitored(true)
                .timeExceedsProgress(alert)
                .build();
    }

    public List<AffaireMetricsDTO> listAllAffairesWithMetrics() {
        return listAllAffairesWithMetrics(null);
    }

    public List<AffaireMetricsDTO> listAllAffairesWithMetrics(Long ingenieurId) {
        return affaireRepository.findAll().stream()
                .filter(affaire -> ingenieurId == null || !getEmployeesForAffaire(affaire, ingenieurId).isEmpty())
                .map(affaire -> getMetricsForAffaire(String.valueOf(affaire.getId()), ingenieurId))
                .collect(Collectors.toList());
    }

    public Affaire getAffaire(String identifier) {
        return resolveAffaire(identifier);
    }

    private List<Employee> getEmployeesForAffaire(Affaire affaire, Long ingenieurId) {
        List<Employee> candidates = new ArrayList<>();

        if (ingenieurId != null) {
            candidates.addAll(employeeRepository.findByIngenieurId(ingenieurId));
        } else {
            candidates.addAll(employeeRepository.findByProjectId(affaire.getId()));
            if (affaire.getCodeAffaire() != null && !affaire.getCodeAffaire().isBlank()) {
                candidates.addAll(employeeRepository.findByAffaireNumero(affaire.getCodeAffaire()));
            }
        }

        String normalizedAffaireCode = normalizeAffaireCode(affaire.getCodeAffaire());

        return new ArrayList<>(candidates.stream()
                .filter(Objects::nonNull)
                .filter(employee -> matchesAffaire(employee, affaire.getId(), normalizedAffaireCode))
                .filter(employee -> employee.getId() != null)
                .collect(Collectors.toMap(
                        Employee::getId,
                        employee -> employee,
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values());
    }

    private boolean matchesAffaire(Employee employee, Long affaireId, String normalizedAffaireCode) {
        if (employee == null) {
            return false;
        }

        if (Objects.equals(employee.getProjectId(), affaireId)) {
            return true;
        }

        String employeeAffaireCode = normalizeAffaireCode(employee.getAffaireNumero());
        return employeeAffaireCode != null && employeeAffaireCode.equals(normalizedAffaireCode);
    }

    private String normalizeAffaireCode(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized.toLowerCase();
    }

    private BigDecimal resolveGlobalPlannedHours(Affaire affaire, List<Employee> employees) {
        BigDecimal planned = affaire.getHeuresEstimees() == null ? BigDecimal.ZERO : affaire.getHeuresEstimees();
        if (planned.compareTo(BigDecimal.ZERO) > 0) {
            return planned;
        }

        for (Employee employee : employees) {
            BigDecimal employeePlanned = parseEmployeePlannedHours(employee);
            if (employeePlanned.compareTo(BigDecimal.ZERO) > 0) {
                return employeePlanned;
            }
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal resolveScopedPlannedHours(Affaire affaire, List<Employee> employees) {
        BigDecimal employeePlannedSum = employees.stream()
                .map(this::parseEmployeePlannedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (employeePlannedSum.compareTo(BigDecimal.ZERO) > 0) {
            return employeePlannedSum;
        }

        return affaire.getHeuresEstimees() == null ? BigDecimal.ZERO : affaire.getHeuresEstimees();
    }

    private BigDecimal parseEmployeePlannedHours(Employee employee) {
        if (employee == null || employee.getPlannedHours() == null || employee.getPlannedHours().isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            String cleaned = employee.getPlannedHours()
                    .replaceAll("[^0-9,. ]", "")
                    .trim()
                    .replace(" ", "")
                    .replace(",", ".");

            if (cleaned.isEmpty()) {
                return BigDecimal.ZERO;
            }

            return new BigDecimal(cleaned);
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateGlobalProgress(Affaire affaire, Long affaireId) {
        if (affaire.getAffaireProgress() != null) {
            return BigDecimal.valueOf(affaire.getAffaireProgress());
        }

        return taskRepository.findByProjectId(affaireId).stream()
                .filter(task -> Boolean.TRUE.equals(task.getCompleted()))
                .map(Task::getWeightPercent)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateScopedProgress(BigDecimal planned, BigDecimal validatedConsumed) {
        if (planned.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return validatedConsumed.multiply(new BigDecimal("100"))
                .divide(planned, 2, RoundingMode.HALF_UP);
    }

    private boolean isValidated(String statut) {
        if (statut == null) {
            return false;
        }

        String normalized = statut
                .trim()
                .toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("Ã©", "e");

        return normalized.contains("valid");
    }

    private Affaire resolveAffaire(String identifier) {
        String normalizedIdentifier = String.valueOf(identifier).trim();
        if (normalizedIdentifier.isEmpty()) {
            throw new IllegalArgumentException("Affaire identifier is required");
        }

        try {
            Long id = Long.valueOf(normalizedIdentifier);
            Affaire byId = affaireRepository.findById(id).orElse(null);
            if (byId != null) {
                return byId;
            }
        } catch (NumberFormatException ignored) {
        }

        return affaireRepository.findByCodeAffaire(normalizedIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Affaire not found: " + normalizedIdentifier));
    }
}
