package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.AffaireMetricsDTO;
import com.example.pointage_backend.model.Affaire;
import com.example.pointage_backend.model.Task;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.model.Pointage;
import com.example.pointage_backend.repository.AffaireRepository;
import com.example.pointage_backend.repository.TaskRepository;
import com.example.pointage_backend.repository.EmployeeRepository;
import com.example.pointage_backend.repository.PointageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffaireService {
    private final AffaireRepository affaireRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final PointageRepository pointageRepository;

    public AffaireMetricsDTO getMetricsForAffaire(String identifier) {
        Affaire affaire = resolveAffaire(identifier);
        Long id = affaire.getId();

        // Consumed hours: compute from Employee attendance data
        // Assuming findByProjectId was kept since entity relation might be projectId
        List<Employee> employees = employeeRepository.findByProjectId(id);

        // Planned hours: use affaire record (heuresEstimees), fallback to employee data if 0
        BigDecimal planned = affaire.getHeuresEstimees() == null ? BigDecimal.ZERO : affaire.getHeuresEstimees();
        if (planned.compareTo(BigDecimal.ZERO) == 0 && !employees.isEmpty()) {
            for (Employee e : employees) {
                try {
                    String raw = e.getPlannedHours();
                    if (raw != null && !raw.isEmpty()) {
                        String cleaned = raw.replaceAll("[^0-9,. ]", "").trim().replace(" ", "").replace(",", ".");
                        if (!cleaned.isEmpty()) {
                            BigDecimal empPlanned = new BigDecimal(cleaned);
                            if (empPlanned.compareTo(BigDecimal.ZERO) > 0) {
                                planned = empPlanned;
                                break; 
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        BigDecimal totalConsumed = pointageRepository.findByAffaireIdOrderByDatePointageDesc(id).stream()
                .map(Pointage::getHeuresTravaillees)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal validatedConsumed = pointageRepository.findByAffaireIdOrderByDatePointageDesc(id).stream()
                .filter(p -> "Validé".equalsIgnoreCase(p.getStatut()) || "Valide".equalsIgnoreCase(p.getStatut()))
                .map(Pointage::getHeuresTravaillees)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Remaining = Planned - Validated (Budget balance)
        BigDecimal remaining = planned.subtract(validatedConsumed);

        // Progress percent: sum of weights of completed tasks OR the explicit affaireProgress field
        BigDecimal progress = affaire.getAffaireProgress() != null 
            ? BigDecimal.valueOf(affaire.getAffaireProgress())
            : taskRepository.findByProjectId(id).stream()
                .filter(t -> Boolean.TRUE.equals(t.getCompleted()))
                .map(t -> t.getWeightPercent() == null ? BigDecimal.ZERO : t.getWeightPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Time percent based on ALL consumed hours vs planned
        BigDecimal timePercent = BigDecimal.ZERO;
        if (planned.compareTo(BigDecimal.ZERO) > 0) {
            timePercent = totalConsumed.multiply(new BigDecimal("100")).divide(planned, 2, java.math.RoundingMode.HALF_UP);
        }

        boolean alert = timePercent.compareTo(progress) > 0;

        List<Long> chargeDAffaireIds = employees.stream()
                .map(Employee::getChargeDAffaireId)
                .filter(sid -> sid != null)
                .map(sid -> {
                    try { return Long.valueOf(sid); } catch(Exception e) { return null; }
                })
                .filter(sid -> sid != null)
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
        List<Affaire> allAffaires = affaireRepository.findAll();
        return allAffaires.stream()
                .map(p -> getMetricsForAffaire(String.valueOf(p.getId())))
                .collect(Collectors.toList());
    }

    public Affaire getAffaire(String identifier) {
        return resolveAffaire(identifier);
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
