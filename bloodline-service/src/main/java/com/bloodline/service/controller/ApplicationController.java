package com.bloodline.service.controller;

import com.bloodline.domain.entity.Application;
import com.bloodline.service.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Application> create(@RequestBody Application app) {
        app.setTenantId("dept_01");
        return ResponseEntity.ok(applicationService.create(app));
    }

    @GetMapping("/{appId}")
    public ResponseEntity<Application> get(@PathVariable String appId) {
        Application app = applicationService.findByAppId("dept_01", appId);
        return app != null ? ResponseEntity.ok(app) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Application>> list() {
        return ResponseEntity.ok(applicationService.listByTenant("dept_01"));
    }
}
