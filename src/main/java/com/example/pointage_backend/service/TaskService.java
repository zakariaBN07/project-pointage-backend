package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.TaskCreateDTO;
import com.example.pointage_backend.model.Task;
import com.example.pointage_backend.model.Project;
import com.example.pointage_backend.repository.TaskRepository;
import com.example.pointage_backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public List<Task> createTasksForProject(String projectId, List<TaskCreateDTO> tasks) {
        // validate project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // sum weights
        BigDecimal sum = tasks.stream()
                .map(t -> t.getWeightPercent() == null ? BigDecimal.ZERO : t.getWeightPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(new BigDecimal("100")) != 0) {
            throw new IllegalArgumentException("Sum of task weights must equal 100. Current sum: " + sum);
        }

        List<Task> toSave = tasks.stream().map(t -> Task.builder()
                .projectId(projectId)
                .name(t.getName())
                .weightPercent(t.getWeightPercent())
                .status("PENDING")
                .completed(false)
                .build()).collect(Collectors.toList());

        return taskRepository.saveAll(toSave);
    }

    public Task completeTask(String taskId, String superviseurId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setCompleted(true);
        task.setStatus("COMPLETED");
        task.setCompletedAt(java.time.LocalDateTime.now());
        return taskRepository.save(task);
    }
}
