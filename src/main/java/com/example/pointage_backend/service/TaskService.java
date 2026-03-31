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
    private final AffaireRepository affaireRepository;
    private final EmployeeRepository employeeRepository;

    private Affaire findAffaireByIdOrCodeAffaire(String affaireIdOrCodeAffaire) {
        try {
            Long id = Long.valueOf(affaireIdOrCodeAffaire);
            return affaireRepository.findById(id)
                    .orElseGet(() -> affaireRepository.findByCodeAffaire(affaireIdOrCodeAffaire)
                            .orElseThrow(() -> new IllegalArgumentException("Affaire not found: " + affaireIdOrCodeAffaire)));
        } catch (NumberFormatException e) {
            return affaireRepository.findByCodeAffaire(affaireIdOrCodeAffaire)
                    .orElseThrow(() -> new IllegalArgumentException("Affaire not found: " + affaireIdOrCodeAffaire));
        }
    }

    public List<Task> getTasksForAffaire(String affaireIdOrCodeAffaire) {
        Affaire affaire = findAffaireByIdOrCodeAffaire(affaireIdOrCodeAffaire);
        return taskRepository.findByProjectId(affaire.getId());
    }

    public List<Task> createTasksForAffaire(String affaireIdOrCodeAffaire, List<TaskCreateDTO> tasks) {
        Affaire affaire = findAffaireByIdOrCodeAffaire(affaireIdOrCodeAffaire);
        Long affaireDbId = affaire.getId();

        BigDecimal sum = tasks.stream()
                .map(t -> t.getWeightPercent() == null ? BigDecimal.ZERO : t.getWeightPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("DEBUG [TaskService]: Creating tasks for affaire " + affaireIdOrCodeAffaire + " (ID: " + affaireDbId + "). Task count: " + tasks.size() + ", Weighted sum: " + sum);

        if (sum.setScale(2, java.math.RoundingMode.HALF_UP).compareTo(new BigDecimal("100.00")) > 0) {
            String error = "Sum of task weights cannot exceed 100%. Current sum: " + sum;
            System.err.println("ERROR [TaskService]: " + error);
            throw new IllegalArgumentException(error);
        }

        List<Task> existingTasks = taskRepository.findByProjectId(affaireDbId);

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

            task.setProjectId(affaireDbId);
            task.setName(dto.getName());
            task.setWeightPercent(dto.getWeightPercent());

            if (dto.getStatus() != null)
                task.setStatus(dto.getStatus());
            if (dto.getCompleted() != null)
                task.setCompleted(dto.getCompleted());

            return task;
        }).collect(Collectors.toList());

        List<Task> saved = taskRepository.saveAll(toSave);
        updateEmployeesAffaireProgress(affaireDbId);
        return saved;
    }

    public Task completeTask(Long taskId, String chargeDAffaireId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setCompleted(true);
        task.setStatus("COMPLETED");
        task.setCompletedAt(java.time.LocalDateTime.now());
        Task saved = taskRepository.save(task);
        updateEmployeesAffaireProgress(task.getProjectId());
        return saved;
    }

    public Task uncompleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setCompleted(false);
        task.setStatus("PENDING");
        task.setCompletedAt(null);
        Task saved = taskRepository.save(task);
        updateEmployeesAffaireProgress(task.getProjectId());
        return saved;
    }

    private void updateEmployeesAffaireProgress(Long affaireDbId) {
        List<Task> tasks = taskRepository.findByProjectId(affaireDbId);
        BigDecimal progress = tasks.stream()
                .filter(t -> Boolean.TRUE.equals(t.getCompleted()))
                .map(t -> t.getWeightPercent() == null ? BigDecimal.ZERO : t.getWeightPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int progressInt = progress.intValue();

        Affaire affaire = affaireRepository.findById(affaireDbId)
                .orElse(null);
        String codeAffaire = affaire != null ? affaire.getCodeAffaire() : null;

        List<Employee> employeesById = employeeRepository.findByProjectId(affaireDbId);
        List<Employee> employeesByAffaire = codeAffaire == null
                ? List.of()
                : employeeRepository.findByAffaireNumero(codeAffaire).stream()
                        .filter(e -> e.getProjectId() == null || affaireDbId.equals(e.getProjectId()))
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
                emp.setProjectId(affaireDbId);
            }
            emp.setProjectProgress(progressInt);
        }
        if (!employees.isEmpty()) {
            employeeRepository.saveAll(employees);
        }
    }
}
