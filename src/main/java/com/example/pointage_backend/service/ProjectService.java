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
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // Planned hours
        BigDecimal planned = project.getPlannedHours() == null ? BigDecimal.ZERO : project.getPlannedHours();

        // Consumed hours: compute from Employee attendance data (not from cached totals)
        List<Employee> employees = employeeRepository.findByProjectId(projectId);
        BigDecimal consumed = employees.stream()
                .mapToDouble(Employee::calculateHoursWorked)
                .boxed()
                .map(hours -> BigDecimal.valueOf(hours))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Remaining
        BigDecimal remaining = planned.subtract(consumed);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        // Progress percent: sum of weights of completed tasks
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        BigDecimal progress = tasks.stream()
                .filter(t -> Boolean.TRUE.equals(t.getCompleted()))
                .map(t -> t.getWeightPercent() == null ? BigDecimal.ZERO : t.getWeightPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Time percent
        BigDecimal timePercent = BigDecimal.ZERO;
        if (planned.compareTo(BigDecimal.ZERO) > 0) {
            timePercent = consumed.multiply(new BigDecimal("100")).divide(planned, 2, BigDecimal.ROUND_HALF_UP);
        }

        boolean alert = timePercent.compareTo(progress) > 0;

        // gather superviseurIds from employees assigned to project
        Set<String> superviseurIds = employees.stream()
                .map(Employee::getSupervisorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        return ProjectMetricsDTO.builder()
                .projectId(project.getId())
                .affaireNumero(project.getAffaireNumero())
                .name(project.getName())
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
