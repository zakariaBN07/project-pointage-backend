package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.ProjectMetricsDTO;
import com.example.pointage_backend.model.Project;
import com.example.pointage_backend.repository.ProjectRepository;
import com.example.pointage_backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectService projectService;

    @GetMapping
    public List<ProjectMetricsDTO> listProjects() {
        return projectService.listAllProjectsWithMetrics();
    }

    @GetMapping("/{id}/metrics")
    public ProjectMetricsDTO getMetrics(@PathVariable String id) {
        return projectService.getMetricsForProject(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Project createProject(@RequestBody Project project) {
        return projectRepository.save(project);
    }

    @PutMapping("/{id}")
    public Project updateProject(@PathVariable String id, @RequestBody Project project) {
        project.setId(id);
        return projectRepository.save(project);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable String id) {
        projectRepository.deleteById(id);
    }
}
