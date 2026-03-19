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

    @GetMapping("/{id}")
    public Project getProject(@PathVariable("id") Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    @GetMapping("/{id}/metrics")
    public ProjectMetricsDTO getMetrics(@PathVariable("id") Long id) {
        return projectService.getMetricsForProject(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Project createProject(@RequestBody Project project) {
        // Prevent duplicate projects with the same codeAffaire
        if (project.getCodeAffaire() != null) {
            return projectRepository.findByCodeAffaire(project.getCodeAffaire())
                    .orElseGet(() -> projectRepository.save(project));
        }
        return projectRepository.save(project);
    }

    @PutMapping("/{id}")
    public Project updateProject(@PathVariable("id") Long id, @RequestBody Project project) {
        project.setId(id);
        return projectRepository.save(project);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable("id") Long id) {
        projectRepository.deleteById(id);
    }
}
