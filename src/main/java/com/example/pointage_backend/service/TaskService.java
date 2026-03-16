package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.TaskCreateDTO;
import com.example.pointage_backend.model.Task;
import com.example.pointage_backend.model.Project;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.repository.TaskRepository;
import com.example.pointage_backend.repository.ProjectRepository;
import com.example.pointage_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    private Project findProjectByIdOrAffaire(String projectIdOrAffaire) {
        // Try finding by ID first
        return projectRepository.findById(projectIdOrAffaire)
                .map(p -> {
                    // Even if found by ID, if it has an affaireNumero, try to get the "first" one
                    // to ensure tasks are always attached to the primary record.
                    if (p.getAffaireNumero() != null) {
                        return projectRepository.findFirstByAffaireNumero(p.getAffaireNumero()).orElse(p);
                    }
                    return p;
                })
                .orElseGet(() ->
                // Fallback to finding by AffaireNumero
                projectRepository.findFirstByAffaireNumero(projectIdOrAffaire)
                        .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectIdOrAffaire)));
    }

    public List<Task> getTasksForProject(String projectIdOrAffaire) {
        // Ensure project exists before listing tasks to avoid returning tasks for
        // deleted projects
        Project project = findProjectByIdOrAffaire(projectIdOrAffaire);
        return taskRepository.findByProjectId(project.getId());
    }

    public List<Task> createTasksForProject(String projectIdOrAffaire, List<TaskCreateDTO> tasks) {
        // validate project exists
        Project project = findProjectByIdOrAffaire(projectIdOrAffaire);
        String projectId = project.getId();

        // 1. Validate weights
        BigDecimal sum = tasks.stream()
                .map(t -> t.getWeightPercent() == null ? BigDecimal.ZERO : t.getWeightPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.setScale(2, java.math.RoundingMode.HALF_UP).compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("Sum of task weights cannot exceed 100%. Current sum: " + sum);
        }

        List<Task> existingTasks = taskRepository.findByProjectId(projectId);

        // 4. Update existing or create new
        List<Task> toSave = tasks.stream().map(dto -> {
            Task task;
            if (dto.getId() != null && !dto.getId().isEmpty()) {
                // Find existing
                task = existingTasks.stream()
                        .filter(ext -> ext.getId().equals(dto.getId()))
                        .findFirst()
                        .orElse(new Task());
            } else {
                task = new Task();
                task.setCompleted(false);
                task.setStatus("PENDING");
            }

            task.setProjectId(projectId);
            task.setName(dto.getName());
            task.setWeightPercent(dto.getWeightPercent());

            if (dto.getStatus() != null)
                task.setStatus(dto.getStatus());
            if (dto.getCompleted() != null)
                task.setCompleted(dto.getCompleted());

            return task;
        }).collect(Collectors.toList());

        List<Task> saved = taskRepository.saveAll(toSave);
        updateEmployeesProjectProgress(projectId);
        return saved;
    }

    public Task completeTask(String taskId, String superviseurId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setCompleted(true);
        task.setStatus("COMPLETED");
        task.setCompletedAt(java.time.LocalDateTime.now());
        Task saved = taskRepository.save(task);
        updateEmployeesProjectProgress(task.getProjectId());
        return saved;
    }

    public Task uncompleteTask(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setCompleted(false);
        task.setStatus("PENDING");
        task.setCompletedAt(null);
        Task saved = taskRepository.save(task);
        updateEmployeesProjectProgress(task.getProjectId());
        return saved;
    }

    private void updateEmployeesProjectProgress(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        BigDecimal progress = tasks.stream()
                .filter(t -> Boolean.TRUE.equals(t.getCompleted()))
                .map(t -> t.getWeightPercent() == null ? BigDecimal.ZERO : t.getWeightPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int progressInt = progress.intValue();

        // fetch project to retrieve affaireNumero and re-link employees missing
        // projectId
        Project project = projectRepository.findById(projectId)
                .orElse(null);
        String affaireNumero = project != null ? project.getAffaireNumero() : null;

        List<Employee> employeesById = employeeRepository.findByProjectId(projectId);
        List<Employee> employeesByAffaire = affaireNumero == null
                ? List.of()
                : employeeRepository.findByAffaireNumero(affaireNumero).stream()
                        // Only apply progress to employees actually linked to this project,
                        // or those missing projectId (so we can link them).
                        .filter(e -> e.getProjectId() == null || e.getProjectId().isEmpty()
                                || projectId.equals(e.getProjectId()))
                        .collect(Collectors.toList());

        // merge and deduplicate
        List<Employee> employees = new java.util.ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (Employee e : employeesById) {
            if (seen.add(e.getId())) {
                employees.add(e);
            }
        }
        for (Employee e : employeesByAffaire) {
            if (seen.add(e.getId())) {
                employees.add(e);
            }
        }

        for (Employee emp : employees) {
            // ensure projectId is linked for future lookups
            if (emp.getProjectId() == null || emp.getProjectId().isEmpty()) {
                emp.setProjectId(projectId);
            }
            emp.setProjectProgress(progressInt);
        }
        if (!employees.isEmpty()) {
            employeeRepository.saveAll(employees);
        }
    }
}
