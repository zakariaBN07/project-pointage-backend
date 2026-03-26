package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.AffaireMetricsDTO;
import com.example.pointage_backend.model.Affaire;
import com.example.pointage_backend.model.Task;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.repository.AffaireRepository;
import com.example.pointage_backend.repository.TaskRepository;
import com.example.pointage_backend.repository.EmployeeRepository;
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

    public AffaireMetricsDTO getMetricsForAffaire(Long id) {
        Affaire affaire = affaireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Affaire not found: " + id));

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

        BigDecimal consumed = employees.stream()
                .mapToDouble(Employee::calculateHoursWorked)
                .boxed()
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Remaining
        BigDecimal remaining = planned.subtract(consumed);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        // Progress percent: sum of weights of completed tasks
        List<Task> tasks = taskRepository.findByProjectId(id);
        BigDecimal progress = tasks.stream()
                .filter(t -> Boolean.TRUE.equals(t.getCompleted()))
                .map(t -> t.getWeightPercent() == null ? BigDecimal.ZERO : t.getWeightPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Time percent
        BigDecimal timePercent = BigDecimal.ZERO;
        if (planned.compareTo(BigDecimal.ZERO) > 0) {
            timePercent = consumed.multiply(new BigDecimal("100")).divide(planned, 2, java.math.RoundingMode.HALF_UP);
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
                .consumedHours(consumed)
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
                .map(p -> getMetricsForAffaire(p.getId()))
                .collect(Collectors.toList());
    }
}
