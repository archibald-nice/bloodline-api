package com.bloodline.service.controller;

import com.bloodline.domain.entity.Project;
import com.bloodline.service.service.ProjectAppRelationService;
import com.bloodline.service.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectAppRelationService relationService;

    public ProjectController(ProjectService projectService, ProjectAppRelationService relationService) {
        this.projectService = projectService;
        this.relationService = relationService;
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

    @PostMapping("/{projectId}/apps/{appId}")
    public ResponseEntity<Void> bindApp(@PathVariable Long projectId, @PathVariable String appId) {
        relationService.bindApp("dept_01", projectId, appId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/apps/{appId}")
    public ResponseEntity<Void> unbindApp(@PathVariable Long projectId, @PathVariable String appId) {
        relationService.unbindApp("dept_01", projectId, appId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{projectId}/apps")
    public ResponseEntity<List<String>> listApps(@PathVariable Long projectId) {
        return ResponseEntity.ok(relationService.listAppIdsByProject("dept_01", projectId));
    }
}
