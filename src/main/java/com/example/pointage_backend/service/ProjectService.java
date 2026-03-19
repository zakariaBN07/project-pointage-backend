package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.ProjectMetricsDTO;
import com.example.pointage_backend.model.Project;
import com.example.pointage_backend.model.Task;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.repository.ProjectRepository;
import com.example.pointage_backend.repository.TaskRepository;
import com.example.pointage_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    public ProjectMetricsDTO getMetricsForProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));

        // Consumed hours: compute from Employee attendance data
        List<Employee> employees = employeeRepository.findByProjectId(id);

        // Planned hours: use project record (heuresEstimees), fallback to employee data if 0
        BigDecimal planned = project.getHeuresEstimees() == null ? BigDecimal.ZERO : project.getHeuresEstimees();
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

        List<Long> superviseurIds = employees.stream()
                .map(Employee::getSupervisorId)
                .filter(sid -> sid != null)
                .map(sid -> {
                    try { return Long.valueOf(sid); } catch(Exception e) { return null; }
                })
                .filter(sid -> sid != null)
                .distinct()
                .collect(Collectors.toList());

        return ProjectMetricsDTO.builder()
                .id(project.getId())
                .codeAffaire(project.getCodeAffaire())
                .nomAffaire(project.getNomAffaire())
                .affairesCodeAffaireUnique(project.getAffairesCodeAffaireUnique())
                .tiersX3(project.getTiersX3())
                .devise(project.getDevise())
                .chargeAffaire(project.getChargeAffaire())
                .categorie(project.getCategorie())
                .dateAffaire(project.getDateAffaire())
                .statut(project.getStatut())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .heuresEstimees(project.getHeuresEstimees())
                .superviseurIds(superviseurIds)
                .plannedHours(planned)
                .consumedHours(consumed)
                .remainingHours(remaining)
                .progressPercent(progress)
                .timePercent(timePercent)
                .isMonitored(true)
                .timeExceedsProgress(alert)
                .build();
    }

    public List<ProjectMetricsDTO> listAllProjectsWithMetrics() {
        List<Project> allProjects = projectRepository.findAll();
        return allProjects.stream()
                .map(p -> getMetricsForProject(p.getId()))
                .collect(Collectors.toList());
    }
}
