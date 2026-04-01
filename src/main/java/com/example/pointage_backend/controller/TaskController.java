package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.TaskCreateDTO;
import com.example.pointage_backend.model.Task;
import com.example.pointage_backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/affaires"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {
    private final TaskService taskService;

    // List all tasks for a given affaire
    @GetMapping("/{affaireId}/tasks")
    public List<Task> listTasks(@PathVariable("affaireId") String affaireId) {
        return taskService.getTasksForAffaire(affaireId);
    }

    // Create tasks for an affaire (validate sum of weights == 100)
    @PostMapping("/{affaireId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Task> createTasks(@PathVariable("affaireId") String affaireId, @RequestBody List<TaskCreateDTO> tasks) {
        return taskService.createTasksForAffaire(affaireId, tasks);
    }

    // Chargé d'affaire marks a task complete
    @PostMapping("/tasks/{taskId}/complete")
    public Task completeTask(
            @PathVariable("taskId") Long taskId,
            @RequestParam(name = "chargeDAffaireId", required = false) String chargeDAffaireId,
            @RequestParam(name = "completionDescription", required = false) String completionDescription
    ) {
        return taskService.completeTask(taskId, chargeDAffaireId, completionDescription);
    }

    @PostMapping("/tasks/{taskId}/uncomplete")
    public Task uncompleteTask(@PathVariable("taskId") Long taskId) {
        return taskService.uncompleteTask(taskId);
    }
}
