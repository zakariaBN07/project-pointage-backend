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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    public ProjectMetricsDTO getMetricsForProject(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseGet(() ->
                        projectRepository.findFirstByAffaireNumero(projectId)
                                .orElseThrow(() -> new IllegalArgumentException("Project not found"))
                );

        // Planned hours


        // Consumed hours: compute from Employee attendance data
        List<Employee> employeesById = employeeRepository.findByProjectId(project.getId());
        List<Employee> employeesByAffaire = project.getAffaireNumero() != null 
                ? employeeRepository.findByAffaireNumero(project.getAffaireNumero())
                : java.util.Collections.emptyList();

        // Merge and deduplicate
        java.util.Set<String> seenIds = new java.util.HashSet<>();
        List<Employee> employees = new java.util.ArrayList<>();
        for (Employee e : employeesById) {
            if (seenIds.add(e.getId())) employees.add(e);
        }
        for (Employee e : employeesByAffaire) {
            // Greedy match: include if associated with this project's affaire number
            if (seenIds.add(e.getId())) {
                employees.add(e);
            }
        }

        // Planned hours: use project record, but fallback to employee data if project is 0
        BigDecimal planned = project.getPlannedHours() == null ? BigDecimal.ZERO : project.getPlannedHours();
        if (planned.compareTo(BigDecimal.ZERO) == 0 && !employees.isEmpty()) {
            for (Employee e : employees) {
                try {
                    String raw = e.getPlannedHours();
                    if (raw != null && !raw.isEmpty()) {
                        // Extract digits and decimal separators (handles "200.00 h", "200,00", etc.)
                        String cleaned = raw.replaceAll("[^0-9,. ]", "").trim();
                        cleaned = cleaned.replace(" ", "").replace(",", ".");
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
                .map(hours -> BigDecimal.valueOf(hours))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Remaining
        BigDecimal remaining = planned.subtract(consumed);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        // Progress percent: sum of weights of completed tasks
        List<Task> tasks = taskRepository.findByProjectId(project.getId());
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

        // gather superviseurIds from employees assigned to project
        Set<String> superviseurIds = employees.stream()
                .map(Employee::getSupervisorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Username fallback (Client name)
        String username = project.getUsername();
        if ((username == null || username.isEmpty()) && !employees.isEmpty()) {
            username = employees.stream()
                    .map(Employee::getClient)
                    .filter(c -> c != null && !c.isEmpty())
                    .findFirst()
                    .orElse(null);
        }

        return ProjectMetricsDTO.builder()
                .projectId(project.getId())
                .affaireNumero(project.getAffaireNumero())
                .name(project.getName())
                .username(username != null ? username : "Client Inconnu")
                .superviseurIds(superviseurIds.stream().collect(Collectors.toList()))
                .plannedHours(planned)
                .consumedHours(consumed)
                .remainingHours(remaining)
                .progressPercent(progress)
                .timePercent(timePercent)
                .timeExceedsProgress(alert)
                .build();
    }

    public List<ProjectMetricsDTO> listAllProjectsWithMetrics() {
        return projectRepository.findAll().stream()
                .map(p -> getMetricsForProject(p.getId()))
                .collect(Collectors.toList());
    }
}
