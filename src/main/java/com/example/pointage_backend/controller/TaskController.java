package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.TaskCreateDTO;
import com.example.pointage_backend.model.Task;
import com.example.pointage_backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {
    private final TaskService taskService;

    // List all tasks for a given project
    @GetMapping("/{projectId}/tasks")
    public List<Task> listTasks(@PathVariable("projectId") String projectId) {
        return taskService.getTasksForProject(projectId);
    }

    // Create tasks for a project (validate sum of weights == 100)
    @PostMapping("/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Task> createTasks(@PathVariable("projectId") String projectId, @RequestBody List<TaskCreateDTO> tasks) {
        return taskService.createTasksForProject(projectId, tasks);
    }

    // Superviseur marks a task complete
    @PostMapping("/tasks/{taskId}/complete")
    public Task completeTask(@PathVariable("taskId") Long taskId, @RequestParam(name = "superviseurId", required = false) String superviseurId) {
        return taskService.completeTask(taskId, superviseurId);
    }

    @PostMapping("/tasks/{taskId}/uncomplete")
    public Task uncompleteTask(@PathVariable("taskId") Long taskId) {
        return taskService.uncompleteTask(taskId);
    }
}
