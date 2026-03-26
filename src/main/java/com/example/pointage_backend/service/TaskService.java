package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.TaskCreateDTO;
import com.example.pointage_backend.model.Task;
import com.example.pointage_backend.model.Affaire;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.repository.TaskRepository;
import com.example.pointage_backend.repository.AffaireRepository;
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
    private final AffaireRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    private Affaire findProjectByIdOrCodeAffaire(String projectIdOrCodeAffaire) {
        try {
            Long id = Long.valueOf(projectIdOrCodeAffaire);
            return projectRepository.findById(id)
                    .orElseGet(() -> projectRepository.findByCodeAffaire(projectIdOrCodeAffaire)
                            .orElseThrow(() -> new IllegalArgumentException("Affaire not found: " + projectIdOrCodeAffaire)));
        } catch (NumberFormatException e) {
            return projectRepository.findByCodeAffaire(projectIdOrCodeAffaire)
                    .orElseThrow(() -> new IllegalArgumentException("Affaire not found: " + projectIdOrCodeAffaire));
        }
    }

    public List<Task> getTasksForProject(String projectIdOrCodeAffaire) {
        Affaire project = findProjectByIdOrCodeAffaire(projectIdOrCodeAffaire);
        return taskRepository.findByProjectId(project.getId());
    }

    public List<Task> createTasksForProject(String projectIdOrCodeAffaire, List<TaskCreateDTO> tasks) {
        Affaire project = findProjectByIdOrCodeAffaire(projectIdOrCodeAffaire);
        Long projectId = project.getId();

        BigDecimal sum = tasks.stream()
                .map(t -> t.getWeightPercent() == null ? BigDecimal.ZERO : t.getWeightPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("DEBUG [TaskService]: Creating tasks for project " + projectIdOrCodeAffaire + " (ID: " + projectId + "). Task count: " + tasks.size() + ", Weighted sum: " + sum);

        if (sum.setScale(2, java.math.RoundingMode.HALF_UP).compareTo(new BigDecimal("100.00")) > 0) {
            String error = "Sum of task weights cannot exceed 100%. Current sum: " + sum;
            System.err.println("ERROR [TaskService]: " + error);
            throw new IllegalArgumentException(error);
        }

        List<Task> existingTasks = taskRepository.findByProjectId(projectId);

        List<Task> toSave = tasks.stream().map(dto -> {
            Task task;
            if (dto.getId() != null) {
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

    public Task completeTask(Long taskId, String chargeDAffaireId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setCompleted(true);
        task.setStatus("COMPLETED");
        task.setCompletedAt(java.time.LocalDateTime.now());
        Task saved = taskRepository.save(task);
        updateEmployeesProjectProgress(task.getProjectId());
        return saved;
    }

    public Task uncompleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setCompleted(false);
        task.setStatus("PENDING");
        task.setCompletedAt(null);
        Task saved = taskRepository.save(task);
        updateEmployeesProjectProgress(task.getProjectId());
        return saved;
    }

    private void updateEmployeesProjectProgress(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        BigDecimal progress = tasks.stream()
                .filter(t -> Boolean.TRUE.equals(t.getCompleted()))
                .map(t -> t.getWeightPercent() == null ? BigDecimal.ZERO : t.getWeightPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int progressInt = progress.intValue();

        Affaire project = projectRepository.findById(projectId)
                .orElse(null);
        String codeAffaire = project != null ? project.getCodeAffaire() : null;

        List<Employee> employeesById = employeeRepository.findByProjectId(projectId);
        List<Employee> employeesByAffaire = codeAffaire == null
                ? List.of()
                : employeeRepository.findByAffaireNumero(codeAffaire).stream()
                        .filter(e -> e.getProjectId() == null || projectId.equals(e.getProjectId()))
                        .collect(Collectors.toList());

        List<Employee> employees = new java.util.ArrayList<>();
        java.util.Set<Long> seen = new java.util.HashSet<>();
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
            if (emp.getProjectId() == null) {
                emp.setProjectId(projectId);
            }
            emp.setProjectProgress(progressInt);
        }
        if (!employees.isEmpty()) {
            employeeRepository.saveAll(employees);
        }
    }
}
