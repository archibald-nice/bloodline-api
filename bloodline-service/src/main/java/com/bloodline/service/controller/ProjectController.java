package com.bloodline.service.controller;

import com.bloodline.domain.entity.Project;
import com.bloodline.service.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Project> create(@RequestBody Project project) {
        project.setTenantId("dept_01");
        project.setStatus(0);
        return ResponseEntity.ok(projectService.create(project));
    }

    @GetMapping("/{projectCode}")
    public ResponseEntity<Project> get(@PathVariable String projectCode) {
        Project project = projectService.findByCode("dept_01", projectCode);
        return project != null ? ResponseEntity.ok(project) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Project>> list() {
        return ResponseEntity.ok(projectService.listByTenant("dept_01"));
    }
}
